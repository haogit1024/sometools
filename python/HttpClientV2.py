import requests
from requests.adapters import HTTPAdapter
from urllib3.util.retry import Retry
import logging
import os
import json
import time
import hashlib
import re
from abc import ABCMeta, abstractmethod
from multiprocessing import Pool, Manager
from urllib.parse import urlsplit
from typing import Optional, Tuple, Dict, List, Any

logging.basicConfig(level=logging.INFO)
log = logging.getLogger("WindowsChrome")

DEFAULT_USER_AGENT = (
    "Mozilla/5.0 (Windows NT 10.0; Win64; x64) "
    "AppleWebKit/537.36 (KHTML, like Gecko) "
    "Chrome/131.0.0.0 Safari/537.36"
)

CONTENT_RANGE_RE = re.compile(r"bytes\s+(\d+)-(\d+)/(\d+)")


class BaseHttpClient(object, metaclass=ABCMeta):
    @abstractmethod
    def get(self, url: str, encoding: Optional[str] = None) -> Optional[bytes]:
        pass

    @abstractmethod
    def post(self, url: str, data: dict, encoding: Optional[str] = None) -> Optional[bytes]:
        pass

    @abstractmethod
    def download(self, http_url: str, save_path: str, alias: Optional[str] = None) -> None:
        pass


class DownloadUtil(object):
    """
    分段下载工具，支持断点续传。

    注意: 在多进程场景下，download_status_dict 必须传入 multiprocessing.Manager().dict()，
    否则跨进程状态同步将失效。
    """

    def __init__(
        self,
        download_cache_path: str = r".download_cache",
        session: Optional[requests.Session] = None,
        download_status_dict: Optional[Dict[str, Any]] = None,
    ):
        self.download_cache_path = download_cache_path
        self.__session = session or self._create_session()
        self.__download_status_dict = download_status_dict if download_status_dict is not None else {}

    @staticmethod
    def _create_session() -> requests.Session:
        session = requests.Session()
        retry_strategy = Retry(total=5, backoff_factor=0.5)
        adapter = HTTPAdapter(max_retries=retry_strategy)
        session.mount("http://", adapter)
        session.mount("https://", adapter)
        return session

    def download(self, http_url: str, save_path: str, alias: Optional[str] = None):
        log.info(f"download file, {http_url}")
        cache_file_path = self.__create_cache_file(http_url, save_path)
        self.__download_status_dict[cache_file_path] = True
        self.__download_file(cache_file_path)

    def contains_download(self, cache_file_name: Optional[str] = None, cache_path: Optional[str] = None):
        if cache_file_name is None and cache_path is None:
            log.error("cache_file_name and cache_path不能同时为空")
            return
        if cache_file_name is not None:
            rel_cache_path = os.path.join(self.download_cache_path, cache_file_name)
        else:
            rel_cache_path = cache_path
        self.__download_file(rel_cache_path, is_continue=True)

    def __download_file(self, cache_path: str, download_size: int = 64, is_continue: bool = False):
        cache_data = self.__read_cache(cache_path)
        http_url = cache_data["http_url"]
        save_path = cache_data["save_path"]

        if is_continue and os.path.exists(save_path):
            start_byte = os.path.getsize(save_path)
            cache_data["index_byte"] = start_byte
        else:
            start_byte = cache_data["index_byte"]

        min_chunk_size = 32
        max_chunk_size = 2048

        while True:
            start_byte = max(start_byte, cache_data.get("index_byte", start_byte))

            total_length = cache_data.get("length", 0)
            index_byte = cache_data.get("index_byte", 0)

            if total_length != 0 and index_byte >= total_length:
                cache_data["finish_time"] = int(time.time())
                cache_data["status"] = "finish"
                self.__write_cache(cache_path, cache_data)
                log.info(f"下载完成 {http_url}")
                return

            if self.__download_status_dict.get(cache_path) is None:
                self.__download_status_dict[cache_path] = True
            if not self.__download_status_dict[cache_path]:
                log.info("手动停止下载")
                return

            end_byte = start_byte + download_size * 1024 - 1
            range_header = f"bytes={start_byte}-{end_byte}"
            log.debug(f"Request Range: {range_header}")
            headers = {"Range": range_header}
            now_time = time.time()

            response = None
            try:
                req_time_util = 2000
                req_start_time = int(round(now_time * 1000))
                try:
                    response = self.__session.get(http_url, headers=headers, timeout=30)
                except requests.RequestException as e:
                    log.error(f"下载出错, url: {http_url}, error: {e}")
                    cache_data["fail_num"] = cache_data["fail_num"] + 1
                    cache_data["status"] = "fail"
                    cache_data["error_msg"] = repr(e)
                    self.__write_cache(cache_path, cache_data)
                    return

                req_end_time = int(round(time.time() * 1000))
                use_time = req_end_time - req_start_time

                response_content_range = response.headers.get("Content-Range", "")
                if not response_content_range:
                    log.error(f"服务器未返回Content-Range, url: {http_url}")
                    cache_data["status"] = "fail"
                    self.__write_cache(cache_path, cache_data)
                    return

                parse_result = self.__parse_content_range(response_content_range)
                if parse_result is None:
                    log.error(f"Content-Range解析失败: {response_content_range}, url: {http_url}")
                    cache_data["status"] = "fail"
                    self.__write_cache(cache_path, cache_data)
                    return

                file_length, rel_start_byte, rel_end_byte = parse_result

                download_rate = (rel_end_byte / file_length) * 100 if file_length > 0 else 0
                cache_data["length"] = file_length

                download_speed = int(((rel_end_byte - rel_start_byte) / 1024) / (use_time / 1000)) if use_time > 0 else 0
                download_remaining_time = int((file_length - rel_end_byte) / 1024 / download_speed) if download_speed > 0 else 0

                log.info(f"{http_url}, download_rate: {download_rate:.2f} %, speed: {download_speed} k/s, remaining: {download_remaining_time}s")

                if use_time < req_time_util and use_time > 0:
                    speed = req_time_util / use_time
                    download_size = int(download_size * speed)
                elif use_time > req_time_util:
                    speed = use_time / req_time_util
                    download_size = int(download_size / speed)

                download_size = max(min_chunk_size, min(download_size, max_chunk_size))

                if download_size == 0:
                    download_size = min_chunk_size

            except Exception as e:
                log.exception(e)
                log.error(f"下载错误 {http_url}")
                return

            if response is None or response.status_code != 206:
                status = response.status_code if response else "No response"
                log.error(f"下载错误, url: {http_url}, status_code: {status}")
                return

            with open(save_path, cache_data["mode"]) as f:
                f.write(response.content)
                cache_data["mode"] = "ab"

            next_byte = rel_end_byte + 1
            cache_data["index_byte"] = next_byte
            cache_data["last_req_time"] = int(now_time)
            cache_data["status"] = "running"
            self.__write_cache(cache_path, cache_data)

            start_byte = next_byte

    def stop_download(self, cache_file_name: Optional[str] = None, cache_path: Optional[str] = None):
        if cache_path is None and cache_file_name is None:
            log.error("停止下载文件出错")
            return
        if cache_path is None:
            cache_path = os.path.join(self.download_cache_path, cache_file_name)
        self.__download_status_dict[cache_path] = False

    def get_download_status(self) -> Dict[str, Dict[str, Any]]:
        """
        获取所有下载任务的详细状态信息。

        Returns:
            字典，key 为缓存文件路径，value 包含:
                - http_url: 下载链接
                - save_path: 保存路径
                - status: 状态 (begin/running/finish/fail)
                - progress: 进度百分比 (0-100)
                - index_byte: 已下载字节数
                - total_length: 文件总字节数
        """
        status_info = {}
        for cache_path in list(self.__download_status_dict.keys()):
            if not os.path.exists(cache_path):
                continue
            try:
                cache_data = self.__read_cache(cache_path)
                total_length = cache_data.get("length", 0)
                index_byte = cache_data.get("index_byte", 0)
                progress = (index_byte / total_length * 100) if total_length > 0 else 0
                status_info[cache_path] = {
                    "http_url": cache_data.get("http_url", ""),
                    "save_path": cache_data.get("save_path", ""),
                    "status": cache_data.get("status", "unknown"),
                    "progress": round(progress, 2),
                    "index_byte": index_byte,
                    "total_length": total_length,
                    "fail_num": cache_data.get("fail_num", 0),
                }
            except (json.JSONDecodeError, KeyError, FileNotFoundError):
                status_info[cache_path] = {"status": "corrupted"}
        return status_info

    def display_dict(self):
        status = self.get_download_status()
        for path, info in status.items():
            log.info(f"[{info['status']}] {info['http_url']} - {info['progress']}%")

    def list_cache_file(self) -> List[str]:
        if not os.path.exists(self.download_cache_path):
            return []
        return [
            os.path.join(self.download_cache_path, f)
            for f in os.listdir(self.download_cache_path)
        ]

    def __create_cache_file(self, http_url: str, save_path: str) -> str:
        os.makedirs(self.download_cache_path, exist_ok=True)
        save_dir = os.path.dirname(save_path)
        if save_dir:
            os.makedirs(save_dir, exist_ok=True)
        file_name = urlsplit(http_url).path.lstrip("/")
        cache_file_path = os.path.join(self.download_cache_path, self.__md5(file_name))
        cache_data = {
            "http_url": http_url,
            "save_path": save_path,
            "index_byte": 0,
            "finish_time": 0,
            "last_req_time": 0,
            "length": 0,
            "mode": "wb",
            "fail_num": 0,
            "status": "begin",
        }
        with open(cache_file_path, "w") as f:
            f.write(json.dumps(cache_data))
        return cache_file_path

    def __read_cache(self, cache_path: str) -> dict:
        with open(cache_path, "r") as f:
            return json.load(f)

    def __write_cache(self, cache_path: str, cache_data: dict):
        with open(cache_path, "w") as f:
            f.write(json.dumps(cache_data))

    def __parse_content_range(self, content_range: str) -> Optional[Tuple[int, int, int]]:
        """
        解析 HTTP Content-Range 响应头。

        Args:
            content_range: 如 "bytes 0-99/1000"

        Returns:
            (file_length, rel_start_byte, rel_end_byte) 或 None 如果解析失败
        """
        match = CONTENT_RANGE_RE.match(content_range.strip())
        if not match:
            return None
        rel_start = int(match.group(1))
        rel_end = int(match.group(2))
        file_length = int(match.group(3))
        return file_length, rel_start, rel_end

    @staticmethod
    def __md5(content: str) -> str:
        return hashlib.md5(content.encode("utf-8")).hexdigest()


def _download_task_wrapper(download_cache_path: str, http_url: str, save_path: str, alias: Optional[str], status_dict: Dict[str, Any]):
    util = DownloadUtil(download_cache_path=download_cache_path, download_status_dict=status_dict)
    util.download(http_url, save_path, alias)


def _continue_download_task_wrapper(download_cache_path: str, cache_name: Optional[str], cache_path: Optional[str], status_dict: Dict[str, Any]):
    util = DownloadUtil(download_cache_path=download_cache_path, download_status_dict=status_dict)
    util.contains_download(cache_file_name=cache_name, cache_path=cache_path)


class WindowsChrome(BaseHttpClient):
    def __init__(
        self,
        max_download_num: int = 4,
        download_cache_path: str = r".download_cache",
        is_enable_request_cache: bool = False,
        request_cache_path: str = r".request_cache",
        request_cache_effective_time: int = 3600,
        user_agent: str = DEFAULT_USER_AGENT,
    ):
        self.headers = {
            "user-agent": user_agent,
            "Upgrade-Insecure-Requests": "1",
            "accept": "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8",
        }
        self.session: Optional[requests.Session] = None
        self.download_cache_path = download_cache_path
        self.is_enable_request_cache = is_enable_request_cache
        self.request_cache_path = request_cache_path
        self.request_cache_effective_time = request_cache_effective_time

        self._manager = None
        self.__download_status_dict = None
        self.__download_pool = None
        self.download_util = None
        self._closed = False

        try:
            self._manager = Manager()
            self.__download_status_dict = self._manager.dict()
            self.__download_pool = Pool(processes=max_download_num)
            self.download_util = DownloadUtil(
                download_cache_path=download_cache_path,
                download_status_dict=self.__download_status_dict,
            )
        except Exception:
            self._cleanup_resources()
            raise

    def _cleanup_resources(self):
        try:
            if self.__download_pool is not None:
                self.__download_pool.terminate()
                self.__download_pool.join()
        except Exception:
            pass
        try:
            if self._manager is not None:
                self._manager.shutdown()
        except Exception:
            pass

    def __enter__(self):
        return self

    def __exit__(self, exc_type, exc_val, exc_tb):
        try:
            self.close(is_join=True)
        except Exception as e:
            log.error(f"关闭资源时出错: {e}")
        return False

    def init_session(self, url: str) -> bool:
        try:
            self.session = requests.Session()
            for key, value in self.headers.items():
                self.session.headers[key] = value
            self.session.get(url, timeout=30)
            return True
        except requests.RequestException as e:
            log.error(f"初始化session失败, url={url}, error={e}")
            self.session = None
            return False

    def get(self, url: str, encoding: Optional[str] = None) -> Optional[bytes]:
        if self.is_enable_request_cache:
            cache_content = self.__get_cache(url)
            if cache_content is not None:
                if encoding is not None:
                    cache_content = cache_content.decode(encoding=encoding)
                return cache_content
        try:
            if self.session is None:
                log.info("WindowsChrome requesting...")
                response = requests.get(url, headers=self.headers, timeout=30)
                log.info("WindowsChrome request finish...")
            else:
                log.info("WindowsChrome session requesting...")
                response = self.session.get(url, timeout=30)
                log.info("WindowsChrome session request finish...")
            if response.status_code == 200:
                if self.is_enable_request_cache and cache_content is None:
                    self.__save_cache(url, response.content)
                if encoding is not None:
                    return response.content.decode(encoding=encoding)
                else:
                    return response.content
            else:
                log.error("访问url失败")
                log.info(f"url={url}, statusCode={response.status_code}")
                return None
        except requests.RequestException as e:
            log.error(f"http访问出错, url={url}, error={e}")
            return None

    def post(self, url: str, data: dict, encoding: Optional[str] = None) -> Optional[bytes]:
        try:
            if self.session is None:
                response = requests.post(url, data=data, headers=self.headers, timeout=30)
            else:
                response = self.session.post(url, data=data, timeout=30)
            if response.status_code == 200:
                if encoding is not None:
                    return response.content.decode(encoding=encoding)
                return response.content
            else:
                log.error(f"POST请求失败, url={url}, statusCode={response.status_code}")
                return None
        except requests.RequestException as e:
            log.error(f"POST请求出错, url={url}, error={e}")
            return None

    def __get_cache(self, url: str) -> Optional[bytes]:
        cache_file = os.path.join(self.request_cache_path, self._md5(url))
        if os.path.exists(cache_file):
            now = time.time()
            cache_create_time = os.path.getctime(cache_file)
            if now - cache_create_time < self.request_cache_effective_time:
                with open(cache_file, "rb") as f:
                    log.debug(f"读取缓存文件, url: {url}, cache_file: {cache_file}")
                    return f.read()
        return None

    def __save_cache(self, url: str, content: bytes):
        os.makedirs(self.request_cache_path, exist_ok=True)
        cache_file = os.path.join(self.request_cache_path, self._md5(url))
        with open(cache_file, "wb") as f:
            log.debug(f"写入缓存文件, url: {url}, cache_file: {cache_file}")
            f.write(content)

    def download(self, http_url: str, save_path: str, alias: Optional[str] = None, sync: bool = False):
        if sync:
            log.info("异步下载")
            self.__download_pool.apply_async(
                _download_task_wrapper,
                args=(self.download_cache_path, http_url, save_path, alias, self.__download_status_dict),
            )
            log.info("添加到进程池完成")
        else:
            log.info("同步下载")
            self.download_util.download(http_url, save_path, alias)

    def all_continue_download(self, cache_file_dir: Optional[str] = None):
        cache_dir = cache_file_dir or self.download_util.download_cache_path
        if not os.path.exists(cache_dir):
            return
        cache_files = os.listdir(cache_dir)
        for cache_file in cache_files:
            cache_file_path = os.path.join(cache_dir, cache_file)
            try:
                cache_data = json.load(open(cache_file_path, "r"))
                if cache_data.get("status") in ("finish",):
                    continue
            except (json.JSONDecodeError, FileNotFoundError):
                pass
            self.__download_pool.apply_async(
                _continue_download_task_wrapper,
                args=(self.download_cache_path, None, cache_file_path, self.__download_status_dict),
            )

    def contains_download(self, cache_file_name: Optional[str] = None, cache_path: Optional[str] = None):
        self.download_util.contains_download(cache_file_name=cache_file_name, cache_path=cache_path)

    def stop_download(self, cache_file_name: Optional[str] = None, cache_path: Optional[str] = None):
        self.download_util.stop_download(cache_file_name=cache_file_name, cache_path=cache_path)

    def close(self, is_join: bool = True):
        if self._closed:
            return
        self._closed = True
        try:
            if is_join and self.__download_pool is not None:
                self.__download_pool.close()
                self.__download_pool.join()
        except Exception as e:
            log.error(f"关闭进程池时出错: {e}")
        finally:
            try:
                if self._manager is not None:
                    self._manager.shutdown()
            except Exception as e:
                log.error(f"关闭Manager时出错: {e}")

    @staticmethod
    def _md5(content: str) -> str:
        return hashlib.md5(content.encode("utf-8")).hexdigest()


if __name__ == "__main__":
    with WindowsChrome() as http_client:
        html = http_client.get("http://fanyi.youdao.com/")
        if html:
            print(f"html = {html[:200]}...")
