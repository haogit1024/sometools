import requests
import logging
import os
import json
import time
import hashlib
from abc import ABCMeta, abstractmethod
from multiprocessing import Pool, Manager
from urllib.parse import urlsplit

logging.basicConfig(level=logging.INFO)
log = logging.getLogger("WindowsChrome")


class BaseHttpClient(object, metaclass=ABCMeta):
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

    def download(self, http_url: str, save_path: str, alias=None):
        # 创建缓存文件夹
        log.info("download file, " + http_url)
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
            log.error("cache_name and cache_path不能同时为空")
            return
        if cache_name is not None:
            rel_cache_path = os.path.join(self.download_cache_path, cache_name)
        else:
            rel_cache_path = cache_path
        self.__download_file(rel_cache_path, is_continue=True)

    def __download_file(self, cache_path: str, download_size: int = 10, is_continue=False):
        """
        下载文件方法，每次只下载一部分，递归执行，可以计算网络速度
        :param cache_path: 缓存文件路径
        :param download_size: 这次下载速度
        :param is_continue: 是否断开后重新下载
        :return: None
        """
        cache_data = self.__read_cache(cache_path)
        http_url = cache_data['http_url']
        save_path = cache_data['save_path']
        start_byte = cache_data['index_byte']

        # 如果是重新下载，start_byte应该根据文件大小来算
        if is_continue:
            already_download_size = os.path.getsize(save_path)
            start_byte = already_download_size

        # 判断是否下载完成
        if cache_data['length'] != 0 and cache_data['index_byte'] >= cache_data['length']:
            # 下载完成
            cache_data['finish_time'] = int(time.time())
            self.__write_cache(cache_path, cache_data)
            log.info("下载完成 " + http_url)
            return

        if self.__download_status_dict.get(cache_path) is None:
            self.__download_status_dict[cache_path] = True
        if not self.__download_status_dict[cache_path]:
            log.info("手动停止下载")
            return

        # 一次下载10k
        end_byte = start_byte + download_size * 1024
        range = 'bytes={start}-{end}'.format(start=start_byte, end=end_byte)
        headers = {"Range": range}
        now_time = time.time()
        try:
            # 设置每次下载耗时接近2秒
            req_time_util = 2000
            req_start_time = int(round(now_time * 1000))
            response = requests.get(http_url, headers=headers)
            # 计算下载需要的时间，适当提高下载速度。目前以2秒为一个下载区间
            req_end_time = int(round(time.time() * 1000))
            use_time = req_end_time - req_start_time
            # 根据响应头计算download_size
            #                         bytes 0-10/1560323
            response_content_range = response.headers['Content-Range']
            log.info("Content-Range: " + response_content_range)
            file_length, rel_start_byte, rel_end_byte = self.__parse_content_range(response_content_range)
            #                           bytes 0-10/1560323
            cache_data['length'] = file_length
            log.info(http_url + ", download_size: " + str(download_size))
            log.info(http_url + ", download_use_time: " + str(use_time))
            download_speed = int(((rel_end_byte - rel_start_byte) / 1000) / (use_time / 1000))
            log.info("%s, download_speed: %d k/s" % (http_url, download_speed))
            if use_time < req_time_util:
                # 提速
                speed = req_time_util / use_time
                download_size = int(download_size * speed)
            elif use_time > req_time_util:
                # 降速
                speed = use_time / req_time_util
                download_size = int(download_size / speed)
        except Exception as e:
            print(e)
            log.error('下载错误' + http_url)
            return

        if response.status_code != 206:
            log.error('下载错误, url: %s, status_code: %d' % (http_url, response.status_code))
            return

        # 写入文件
        with open(save_path, cache_data['mode']) as f:
            f.write(response.content)
            # 下载完一部分后修改文件写入状态
            cache_data['mode'] = r'ab'
        # 更新缓存文件
        cache_data['index_byte'] = end_byte + 1
        cache_data['last_req_time'] = int(now_time)
        self.__write_cache(cache_path, cache_data)
        self.__download_file(cache_path, download_size)

    def stop_download(self, cache_file_name: str = None, cache_path: str = None):
        if cache_path is None and cache_file_name is None:
            log.error("停止下载文件出错")
            return
        if cache_path is None:
            cache_path = os.path.join(self.download_cache_path, cache_file_name)
        self.__download_status_dict[cache_path] = False

    def display_dict(self):
        print(r"cache_dict", self.__download_status_dict)

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
        cache_file_path = os.path.join(self.download_cache_path, self.__md5(file_name))
        with open(cache_file_path, 'w') as f:
            cache_data = {
                'http_url': http_url,  # 资源下载连接
                'save_path': save_path,  # 保存地址
                "index_byte": 0,  # 开始下载的字节索引
                "finish_time": 0,  # 下载完成使劲啊
                "last_req_time": 0,  # 最后请求下载时间
                "length": 0,  # 文件总大小单位byte
                "mode": r"wb"  # python文件写入模式
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

    def __parse_content_range(self, content_range: str) -> (int, int, int):
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

    def __md5(self, content: str):
        md5 = hashlib.md5()
        md5.update(content.encode(r"utf-8"))
        return md5.hexdigest()


class WindowsChrome(BaseHttpClient):
    def __init__(self, max_download_num: int = None, download_cache_path: str = r'.download_cache'):
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

    def init_session(self, url: str):
        requests.get(url, headers=self.headers)
        self.session = requests.session()

    def get(self, url: str, encoding: str = None) -> str:
        """
        获取http请求response
        :param url: 请求url
        :param encoding: encoding:需要解析成指定的字符编码
        :return:
        """
        try:
            if self.session is None:
                log.info("WindowsChrome requesting...")
                response = requests.get(url, headers=self.headers)
                log.info("WindowsChrome request finish...")
            else:
                log.info("WindowsChrome session requesting...")
                response = self.session.get(url, headers=self.headers)
                log.info("WindowsChrome session request finish...")
            if response.status_code == 200:
                if encoding is not None:
                    # log.info('return encoding string')
                    return response.content.decode(encoding=encoding)
                else:
                    # log.info('return binary object')
                    return response.content
            else:
                log.error('访问url失败')
                log.info('url=%s, statusCode=%s' % (url, response.status_code))
                return None
        except RuntimeError as e:
            log.error('http访问出错,url=%s' % url)
            print(e)

    def post(self, url: str, data: dict, encoding=None):
        pass

    def download(self, http_url: str, save_path: str, alias=None, sync=False):
        """
        :source_url: 下载文件的url
        :save_path: 文件保存路径
        :alias: 文件别称
        :sync: 是否是开启异步下载, 利用进程池实现, 为True时, 用户必须要等进程池的进程都执行完之后才能结束主(用户)进程,
        调用 close(True)方法可以达到效果
        """
        if sync:
            log.info("异步下载")
            # 获取进程池和往进程池添加一个task
            # p = Process(target=self.download_util.download, args=(http_url, save_path, alias,))
            # p.start()
            # p.join()
            self.__download_pool.apply_async(self.download_util.download, args=(http_url, save_path, alias,))
            log.info("添加到进程池完成")
        else:
            log.info("同步下载")
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
        用户必须手动调用该方法
        :param is_join: 是否等待子进程结束
        :return:
        """
        # 阻止其他任务提交到进程池
        self.__download_pool.close()
        if is_join:
            self.__download_pool.join()
        else:
            self.__download_pool.terminate()

    def test(self):
        log.info('url=%s, statusCode=%s' % (r'url', r'response.status_code'))


def fuck_python(chrome: WindowsChrome):
    print(chrome.download_cache_path)


if __name__ == '__main__':
    http_client = WindowsChrome()
    html = http_client.get('http://fanyi.youdao.com/')
    print('html = ' + html)
