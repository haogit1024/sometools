import requests
import logging
import os
import json
import time
import hashlib
from abc import ABC, abstractmethod
from multiprocessing import Pool, Manager
from urllib.parse import urlsplit

# 配置HttpClient专用日志
logger = logging.getLogger("HttpClient")
logger.setLevel(logging.INFO)

# 添加控制台处理器
if not logger.handlers:
    console_handler = logging.StreamHandler()
    console_handler.setLevel(logging.INFO)
    formatter = logging.Formatter('%(asctime)s - %(name)s - %(levelname)s - %(message)s')
    console_handler.setFormatter(formatter)
    logger.addHandler(console_handler)


class BaseHttpClient(ABC):
    @abstractmethod
    def get(self, url, encoding=None):
        pass

    @abstractmethod
    def post(self, url, data, encoding=None):
        pass

    @abstractmethod
    def download(self, http_url, save_path, alias=None):
        pass


class DownloadUtil(object):
    """
    去他妈的python的processes模块和pickled模块不支持序列化当前对象和lambda，所以新建一个类做并发下载
    """
    def __init__(self, download_cache_path: str = r'.download_cache'):
        self.download_cache_path = download_cache_path
        m = Manager()
        self.__download_status_dict = m.dict()
        requests.DEFAULT_RETRIES = 5
        self.__session = requests.session()
        self.__session.keep_alive = False

    def download(self, http_url: str, save_path: str, alias=None):
        # 创建缓存文件夹
        logger.info("download file, " + http_url)
        cache_file_path = self.__create_cache_file(http_url, save_path)
        self.__download_status_dict[cache_file_path] = True
        self.__download_file(cache_file_path)

    def contains_download(self, cache_name: str = None, cache_path: str = None):
        """
        继续下载单个文件
        :param cache_name: 缓存文件名
        :param cache_path: 缓存文件路径
        :return:
        """
        if cache_name is None and cache_path is None:
            logger.error("cache_name and cache_path不能同时为空")
            return
        if cache_name is not None:
            rel_cache_path = os.path.join(self.download_cache_path, cache_name)
        else:
            rel_cache_path = cache_path
        self.__download_file(rel_cache_path, is_continue=True)

    def __download_file(self, cache_path: str, download_size: int = 10, is_continue=False):
        """
        下载文件方法，每次只下载一部分，可以计算网络速度
        :param cache_path: 缓存文件路径
        :param download_size: 下载块大小(KB)
        :param is_continue: 是否断开后重新下载
        :return: None
        """
        try:
            cache_data = self.__read_cache(cache_path)
            http_url = cache_data['http_url']
            save_path = cache_data['save_path']
            
            # 如果是重新下载，start_byte应该根据文件大小来算
            if is_continue:
                try:
                    already_download_size = os.path.getsize(save_path)
                    cache_data['index_byte'] = already_download_size
                except OSError as e:
                    logger.error(f'获取文件大小失败, 文件路径: {save_path}, 错误: {str(e)}')
                    return
            
            # 初始化下载块大小
            current_download_size = download_size
            
            # 循环下载直到完成
            while True:
                start_byte = cache_data['index_byte']
                
                # 判断是否下载完成
                if cache_data['length'] != 0 and start_byte >= cache_data['length']:
                    # 下载完成
                    cache_data['finish_time'] = int(time.time())
                    cache_data['status'] = 'finish'
                    self.__write_cache(cache_path, cache_data)
                    logger.info("下载完成: %s", http_url)
                    break
                
                # 检查是否需要停止下载
                if not self.__download_status_dict.get(cache_path, True):
                    logger.info("手动停止下载: %s", http_url)
                    break
                
                # 一次下载指定大小的块
                end_byte = start_byte + current_download_size * 1024
                range_header = 'bytes={}-{}'.format(start_byte, end_byte)
                logger.debug("Request Range: %s, URL: %s", range_header, http_url)
                headers = {"Range": range_header}
                now_time = time.time()
                
                # 设置每次下载耗时接近2秒
                req_time_util = 2000
                req_start_time = int(round(now_time * 1000))
                
                try:
                    response = self.__session.get(http_url, headers=headers, stream=True, timeout=30)
                    response.raise_for_status()  # 检查HTTP错误
                except requests.exceptions.RequestException as e:
                    logger.error('下载请求失败, URL: %s, 错误: %s', http_url, str(e))
                    cache_data['fail_num'] = cache_data.get('fail_num', 0) + 1
                    cache_data['status'] = 'fail'
                    cache_data['error_msg'] = str(e)
                    self.__write_cache(cache_path, cache_data)
                    time.sleep(5)  # 等待5秒后重试
                    continue
                
                # 计算下载需要的时间
                req_end_time = int(round(time.time() * 1000))
                use_time = req_end_time - req_start_time
                
                # 解析响应头
                try:
                    response_content_range = response.headers['Content-Range']
                    logger.debug("Response Content-Range: %s, URL: %s", response_content_range, http_url)
                    file_length, rel_start_byte, rel_end_byte = self.__parse_content_range(response_content_range)
                except (KeyError, ValueError) as e:
                    logger.error(f'解析响应头失败, URL: {http_url}, 错误: {str(e)}')
                    time.sleep(5)
                    continue
                    
                # 更新缓存中的文件总大小
                cache_data['length'] = file_length
                
                # 计算下载速度和进度
                downloaded_bytes = rel_end_byte - rel_start_byte + 1  # Content-Range是包含两端的
                logger.debug("URL: %s, 本次下载大小: %d KB, 耗时: %d ms", http_url, current_download_size, use_time)
                
                if use_time > 0:
                    download_speed = int((downloaded_bytes / 1024) / (use_time / 1000))
                    if download_speed > 0:
                        download_remaining_time = int((file_length - rel_end_byte) / 1024 / download_speed)
                    else:
                        download_remaining_time = -1
                        
                    download_rate = (rel_end_byte / file_length) * 100
                    logger.info("URL: %s, 下载速度: %d KB/s, 进度: %.2f%%, 剩余时间: %d 秒", 
                              http_url, download_speed, download_rate, download_remaining_time)
                else:
                    logger.debug("URL: %s, 下载耗时过短, 无法计算速度", http_url)
                    
                # 动态调整下载块大小
                if use_time < req_time_util:
                    # 提速
                    speed = req_time_util / max(use_time, 1)  # 避免除以0
                    current_download_size = int(current_download_size * speed)
                elif use_time > req_time_util:
                    # 降速
                    speed = use_time / req_time_util
                    current_download_size = int(current_download_size / speed)
                    
                # 确保下载块大小不会过小或过大
                current_download_size = max(1, min(current_download_size, 1024))  # 限制在1KB到1MB之间
                
                # 写入文件
                try:
                    with open(save_path, cache_data['mode'] if 'mode' in cache_data else 'wb') as f:
                        for chunk in response.iter_content(chunk_size=8192):
                            if chunk:
                                f.write(chunk)
                    # 下载完一部分后修改文件写入状态
                    cache_data['mode'] = 'ab'
                except OSError as e:
                    logger.error(f'写入文件失败, 文件路径: {save_path}, 错误: {str(e)}')
                    time.sleep(5)
                    continue
                    
                # 更新缓存文件
                cache_data['index_byte'] = rel_end_byte + 1
                cache_data['last_req_time'] = int(now_time)
                cache_data['status'] = 'running'
                self.__write_cache(cache_path, cache_data)
                
        except Exception as e:
            logger.error(f'下载文件时发生未处理的错误, 缓存路径: {cache_path}, 错误类型: {type(e).__name__}, 错误: {str(e)}')
            # 更新缓存状态
            try:
                cache_data = self.__read_cache(cache_path)
                cache_data['status'] = 'error'
                cache_data['error_msg'] = str(e)
                self.__write_cache(cache_path, cache_data)
            except Exception:
                pass

    def stop_download(self, cache_file_name: str = None, cache_path: str = None):
        if cache_path is None and cache_file_name is None:
            logger.error("停止下载文件出错")
            return
        if cache_path is None:
            cache_path = os.path.join(self.download_cache_path, cache_file_name)
        self.__download_status_dict[cache_path] = False

    def display_dict(self):
        logger.info(r"cache_dict", self.__download_status_dict)

    def list_cache_file(self):
        """
        返回所有缓存文件
        :return: list[file]
        """
        pass

    def __create_cache_file(self, http_url: str, save_path: str) -> str:
        """
        创建缓存文件，并返回缓存文件路径
        :param http_url:
        :param save_path:
        :return:
        """
        if not os.path.exists(self.download_cache_path):
            os.makedirs(self.download_cache_path)
        # 根据save_path创建文件夹
        save_dir = os.path.dirname(save_path)
        if not os.path.exists(save_dir):
            os.makedirs(save_dir)
        #           /xxx.xx            xxx.xx
        file_name = urlsplit(http_url).path[1:]
        cache_file_path = os.path.join(self.download_cache_path, md5(file_name))
        with open(cache_file_path, 'w') as f:
            cache_data = {
                'http_url': http_url,  # 资源下载连接
                'save_path': save_path,  # 保存地址
                "index_byte": 0,  # 开始下载的字节索引
                "finish_time": 0,  # 下载完成时间
                "last_req_time": 0,  # 最后请求下载时间
                "length": 0,  # 文件总大小单位byte
                "mode": r"wb",  # python文件写入模式
                "fail_num": 0,
                "status": "begin"
            }
            f.write(json.dumps(cache_data))
        return cache_file_path

    def __read_cache(self, cache_path: str) -> dict:
        """
        读取缓存文件
        :param cache_path: 缓存文件路径
        :return: dict 配置文件
        """
        with open(cache_path, 'r') as f:
            return json.load(f)

    def __write_cache(self, cache_path: str, cache_data: dict):
        """
        写入配置文件
        :param cache_path: 缓存文件路径
        :param cache_data: 配置字典
        :return: None
        """
        with open(cache_path, 'w') as f:
            f.write(json.dumps(cache_data))

    def __parse_content_range(self, content_range: str) -> (int):
        """
        解析http的Content-Range响应头
        :param content_range: bytes 0-10/1560323
        :return: file_length, start_byte, end_byte
        """
        content_arr = content_range.split(r'/')
        file_length = int(content_arr[1])
        bytes_arr = content_arr[0].split(r' ')
        index_arr = bytes_arr[1].split(r'-')
        return int(file_length), int(index_arr[0]), int(index_arr[1])


class WindowsChrome(BaseHttpClient):
    def __init__(self, max_download_num: int = None, download_cache_path: str = r'.download_cache',
                 is_enable_request_cache: bool = False, request_cache_path: str = r'.request_cache',
                 request_cache_effective_time: int = 3600, timeout: int = 30, verify: bool = True):
        """
        初始化浏览器
        :param max_download_num: 最大下载进程数
        :param download_cache_path: 断点续传缓存文件
        :param is_enable_request_cache: 是否开启请求缓存
        :param request_cache_path: 请求缓存文件路径
        :param request_cache_effective_time: 请求缓存有效时间
        :param timeout: 请求超时时间（秒）
        """
        # windows google chrome http request header
        self.headers = {
            'user-agent': 'Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/71.0.3551.3 Safari/537.36',
            'Upgrade-Insecure-Requests': '1',
            'accept': 'text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8',
        }
        self.session = None
        self.__download_pool = Pool(processes=max_download_num)
        self.__download_status_dict = {}
        self.download_cache_path = download_cache_path
        self.download_util = DownloadUtil(download_cache_path)
        self.is_enable_request_cache = is_enable_request_cache
        self.request_cache_path = request_cache_path
        self.request_cache_effective_time = request_cache_effective_time
        self.timeout = timeout  # 请求超时时间
        self.verify = verify  # 是否验证HTTPS证书

    def init_session(self, url: str):
        requests.get(url, headers=self.headers)
        self.session = requests.session()

    def get(self, url: str, encoding: str = None):
        """
        获取http请求response
        :param url: 请求url
        :param encoding: encoding:需要解析成指定的字符编码
        :return: 响应内容或None
        """
        # 读取缓存文件
        if self.is_enable_request_cache:
            cache_content = self.__get_cache(url)
            if cache_content is not None:
                if encoding is not None:
                    cache_content = cache_content.decode(encoding=encoding)
                return cache_content
        try:
            if self.session is None:
                logger.info("HttpClient GET request: %s", url)
                response = requests.get(url, headers=self.headers, timeout=self.timeout, verify=self.verify)
                logger.info("HttpClient GET request finished: %s", url)
            else:
                logger.info("HttpClient session GET request: %s", url)
                response = self.session.get(url, headers=self.headers, timeout=self.timeout, verify=self.verify)
                logger.info("HttpClient session GET request finished: %s", url)
            
            response.raise_for_status()  # 自动处理4xx和5xx错误
            
            content = response.content
            if self.is_enable_request_cache and cache_content is None:
                self.__save_cache(url, content)
                
            if encoding is not None:
                return content.decode(encoding=encoding)
            return content
            
        except requests.exceptions.RequestException as e:
            logger.error(f'HTTP请求出错, URL={url}, 错误: {str(e)}')
            return None
        except Exception as e:
            logger.error(f'其他错误, URL={url}, 错误类型: {type(e).__name__}, 错误: {str(e)}')
            return None

    def post(self, url: str, data: dict, encoding=None):
        """
        发送POST请求
        :param url: 请求URL
        :param data: POST数据
        :param encoding: 需要解析成指定的字符编码
        :return: 响应内容或None
        """
        # 读取缓存文件
        if self.is_enable_request_cache:
            # 使用URL和数据的MD5作为缓存键
            cache_key = md5(url + json.dumps(data, sort_keys=True))
            cache_content = self.__get_cache(cache_key)
            if cache_content is not None:
                if encoding is not None:
                    cache_content = cache_content.decode(encoding=encoding)
                return cache_content
        try:
            if self.session is None:
                logger.info("HttpClient POST request: %s", url)
                response = requests.post(url, data=data, headers=self.headers, timeout=self.timeout, verify=self.verify)
                logger.info("HttpClient POST request finished: %s", url)
            else:
                logger.info("HttpClient session POST request: %s", url)
                response = self.session.post(url, data=data, headers=self.headers, timeout=self.timeout, verify=self.verify)
                logger.info("HttpClient session POST request finished: %s", url)
            
            response.raise_for_status()  # 自动处理4xx和5xx错误
            
            content = response.content
            if self.is_enable_request_cache:
                self.__save_cache(cache_key, content)
                
            if encoding is not None:
                return content.decode(encoding=encoding)
            return content
            
        except requests.exceptions.RequestException as e:
            logger.error(f'HTTP POST请求出错, URL={url}, 错误: {str(e)}')
            return None
        except Exception as e:
            logger.error(f'其他错误, URL={url}, 错误类型: {type(e).__name__}, 错误: {str(e)}')
            return None

    def __get_cache(self, url: str) -> bytes:
        cache_file = os.path.join(self.request_cache_path, md5(url))

        if os.path.exists(cache_file):
            now = time.time()
            cache_create_time = os.path.getctime(cache_file)
            # logger.info("now %s, cache_create_time %s, diff %s", str(now), str(cache_create_time), str(now - cache_create_time))
            if now - cache_create_time < self.request_cache_effective_time:
                with open(cache_file, 'rb') as f:
                    logger.info(f'开始读取缓存文件, url：{url}, cache_file: {cache_file}')
                    return f.read()

    def __save_cache(self, url: str, content: bytes):
        # 创建缓存文件夹
        if not os.path.exists(self.request_cache_path):
            os.makedirs(self.request_cache_path)
        cache_file = os.path.join(self.request_cache_path, md5(url))
        with open(cache_file, 'wb') as f:
            logger.info(f'开始写入缓存文件, url：{url}, cache_file: {cache_file}')
            f.write(content)

    def download(self, http_url: str, save_path: str, alias=None, sync=False):
        """
        :source_url: 下载文件的url
        :save_path: 文件保存路径
        :alias: 文件别称
        :sync: 是否是开启异步下载, 利用进程池实现, 为True时, 用户必须要等进程池的进程都执行完之后才能结束主(用户)进程,
        调用 close(True)方法可以达到效果
        """
        if sync:
            logger.info("异步下载")
            # 获取进程池和往进程池添加一个task
            # p = Process(target=self.download_util.download, args=(http_url, save_path, alias,))
            # p.start()
            # p.join()
            self.__download_pool.apply_async(self.download_util.download, args=(http_url, save_path, alias,))
            logger.info("添加到进程池完成")
        else:
            logger.info("同步下载")
            self.download_util.download(http_url, save_path, alias)

    def all_continue_download(self, cache_file_dir=None):
        """
        未完成的文件全部自动下载
        必须等进程池执行完后才能终止主(用户)进程, 调用 close(True)方法可以达到效果
        :return:
        """
        # cache_dir = self.download_cache_path
        cache_dir = self.download_util.download_cache_path
        if cache_file_dir is not None:
            cache_dir = cache_file_dir
        cache_files = os.listdir(cache_dir)
        for cache_file in cache_files:
            cache_file_path = os.path.join(cache_dir, cache_file)
            # 打开一个进程调用下载
            # p = Process(target=self.__download_file(cache_file_path))
            # p = Process(target=self.download_util.contains_download, args=(None, cache_file_path))
            # p.start()
            self.__download_pool.apply_async(self.download_util.contains_download, args=(None, cache_file_path,))

    def contains_download(self, cache_name: str = None, cache_path: str = None):
        """
        继续下载单个文件
        :param cache_name: 缓存文件名
        :param cache_path: 缓存文件路径
        :return:
        """
        self.download_util.contains_download(cache_name, cache_path)

    def stop_download(self, cache_file_name: str = None, cache_path: str = None):
        self.download_util.stop_download(cache_file_name, cache_path)

    def close(self, is_join: bool):
        """
        关闭HTTP客户端，释放资源
        :param is_join: 是否等待所有下载任务完成
        :return: None
        """
        if is_join:
            self.__download_pool.close()
            self.__download_pool.join()
    
    def __enter__(self):
        """支持上下文管理器"""
        return self
    
    def __exit__(self, exc_type, exc_val, exc_tb):
        """退出上下文时自动关闭客户端"""
        self.close(True)
    
    def clean_request_cache(self, force: bool = False):
        """
        清理请求缓存
        :param force: 是否强制清理所有缓存，否则只清理过期的
        :return: 清理的文件数量
        """
        cleaned_count = 0
        if os.path.exists(self.request_cache_path):
            now = time.time()
            for cache_file in os.listdir(self.request_cache_path):
                cache_file_path = os.path.join(self.request_cache_path, cache_file)
                if force or (now - os.path.getctime(cache_file_path) > self.request_cache_effective_time):
                    try:
                        os.remove(cache_file_path)
                        cleaned_count += 1
                        logger.info(f'清理请求缓存文件: {cache_file_path}')
                    except OSError as e:
                        logger.error(f'清理请求缓存文件失败: {cache_file_path}, 错误: {str(e)}')
        logger.info(f'共清理 {cleaned_count} 个请求缓存文件')
        return cleaned_count
    
    def clean_download_cache(self, force: bool = False):
        """
        清理下载缓存
        :param force: 是否强制清理所有缓存，否则只清理已完成的
        :return: 清理的文件数量
        """
        cleaned_count = 0
        if os.path.exists(self.download_cache_path):
            for cache_file in os.listdir(self.download_cache_path):
                cache_file_path = os.path.join(self.download_cache_path, cache_file)
                if force:
                    try:
                        os.remove(cache_file_path)
                        cleaned_count += 1
                        logger.info(f'清理下载缓存文件: {cache_file_path}')
                    except OSError as e:
                        logger.error(f'清理下载缓存文件失败: {cache_file_path}, 错误: {str(e)}')
                else:
                    try:
                        with open(cache_file_path, 'r') as f:
                            cache_data = json.load(f)
                            if cache_data.get('status') == 'finish':
                                os.remove(cache_file_path)
                                cleaned_count += 1
                                logger.info(f'清理已完成的下载缓存文件: {cache_file_path}')
                    except (OSError, json.JSONDecodeError) as e:
                        logger.error(f'读取下载缓存文件失败: {cache_file_path}, 错误: {str(e)}')
        logger.info(f'共清理 {cleaned_count} 个下载缓存文件')
        return cleaned_count
    
    def clean_all_cache(self, force: bool = False):
        """
        清理所有缓存
        :param force: 是否强制清理所有缓存
        :return: 清理的文件总数
        """
        request_cleaned = self.clean_request_cache(force)
        download_cleaned = self.clean_download_cache(force)
        total_cleaned = request_cleaned + download_cleaned
        logger.info(f'共清理 {total_cleaned} 个缓存文件')
        return total_cleaned




def md5(content):
    md5 = hashlib.md5()
    md5.update(content.encode(r"utf-8"))
    return md5.hexdigest()


if __name__ == '__main__':
    # 使用上下文管理器
    with WindowsChrome() as http_client:
        html = http_client.get('http://fanyi.youdao.com/')
        if html:
            print(f'html length = {len(html)}')
            print('html = ' + html.decode('utf-8')[:500] + '...')
