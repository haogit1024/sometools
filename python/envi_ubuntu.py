#!/usr/bin/env python3
# -*- coding: utf-8 -*-

""" 一个搭建编程环境脚本脚本 """

import os
import sys
import shutil
from downloader import Downloader

def java():
    """
    ubuntu安装openjdk, fuck orcale
    """
    java_cmd: str = r'sudo apt-get install openjdk-8-jdk openjdk-8-jre'
    print("开始安装openjdk8")
    os.system(java_cmd)
    print('开始安装maven')
    downloader = Downloader()
    # TODO 把返回的文件路径复制到对应的地方并解压
    maven_file_path = downloader.download_maven()
    print('下载maven完成')
    print('开始解压')
    # TODO 创建 java_tools 文件夹, 获取 maven tar 包文件名, 调用 tar 命令解压
    maven_tar_file_name = os.path.basename(maven_file_path)
    # if not os.path.exists()
    shutil.copy(maven_file_path, '~/java_tools/')
    print('把maven软连接到 /usr/bin/maven 下')


def node_vue():
    """
    根据操作系统类型搭建vue开发环境
    """
    node_cmds: [] = [r'sudo apt install npm', r'npm intall -g n']
    print('开始安装node')
    for cmd in node_cmds:
        os.system(cmd)
    vue_cmd = r'sudo npm install -g @vue/cli'
    print('开始安装vue/cli')
    os.system(vue_cmd)


def thefuck():
    os.system(r'sudo pip3 install thefuck')


def ohmyzsh():
    os.system('sudo apt install wget')
    os.system(r'sh -c "$(fetch -o - https://raw.githubusercontent.com/ohmyzsh/ohmyzsh/master/tools/install.sh)"')


def main():
    """
    根据命令行参数搭建需要的环境
    """
    envi_type = sys.argv[1]
    if envi_type == 'vue':
        node_vue()
    elif envi_type == 'java':
        java()
    else:
        print('请输入要生成的环境类型')


if __name__ == '__main__':
    main()
