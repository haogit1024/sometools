#!/usr/bin/env python3
# -*- coding: utf-8 -*-

""" 一个搭建编程环境脚本脚本 """

from downloader import Downloader
import platform
import os


def java():
    """
    根据操作系统类型搭建java开发环境, fuck oracle
    """
    pass


def node_vue():
    """
    根据操作系统类型搭建vue开发环境
    """
    downloader = Downloader()
    node_path = downloader.download_node(platform.system())
    # 运行node
    os.system(r'start ' + node_path)
    # 安装cnpm, vue
    os.system(r'npm install -g cnpm --registry=https://registry.npm.taobao.org')
    os.system(r'npm install -g @vue/cli')
    


def main():
    """
    根据命令行参数搭建需要的环境
    """
    pass


if __name__ == '__main__':
    main()
