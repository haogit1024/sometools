#!/usr/bin/env python3
# -*- coding: utf-8 -*-

""" 一个搭建编程环境脚本脚本 """

from downloader import Downloader
import platform
import os


downloader = Downloader()


def java():
    """
    根据操作系统类型搭建java开发环境, fuck oracle
    """
    jdk_path = downloader.download_adopt_open_jdk(r'8', r'jdk', r'Windows')
    os.system('start ' + jdk_path)
    maven_path = downloader.download_maven()
    


def node_vue():
    """
    根据操作系统类型搭建vue开发环境
    """
    node_path = downloader.download_node(platform.system())
    # 运行node
    os.system(r'start ' + node_path)


def main():
    """
    根据命令行参数搭建需要的环境
    """
    pass


if __name__ == '__main__':
    main()
