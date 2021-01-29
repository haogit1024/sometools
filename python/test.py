from downloader import Downloader
import platform
import os
import shutil
import tempfile
import time
import json
import uuid
from HttpClient import WindowsChrome


def hello(a='a', b='b', c='c'):
    print(a)
    print(b)
    print(c)


def main():
    content = 'hello world'
    with open(r'test.cache', 'wb') as f:
        f.write(bytes(content, encoding='UTF=8'))
    with open(r'test.cache', 'rb') as f:
        print(bytes.decode(f.read(), encoding='UTF-8'))


if __name__ == "__main__":
    # main()
    chrome = WindowsChrome(is_enable_request_cache=True)
    # chrome.get(r'https://www.baidu.com')
    # chrome.get(r'https://www.baidu.com')
    print(platform.system())
    # 47109/21390448
    print(47109/21390448)
    print('%s %%' % 'a')
