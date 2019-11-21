#!/usr/bin/env python3
# -*- coding: utf-8 -*-

""" 一个搭建编程环境脚本脚本 """

import os
from HttpClient import WindowsChrome

def java():
    """
    ubuntu安装openjdk, 去他妈的orcalejdk
    """
    java_cmd: str = r'sudo apt-get install openjdk-8-jdk,openjdk-8-jre'
    print("开始安装openjdk8")
    os.system(java_cmd)
    print('开始安装maven')
    # http://mirror.bit.edu.cn/apache/maven/maven-3/3.6.2/binaries/apache-maven-3.6.2-bin.tar.gz
    # TODO 获取版本号和下载文件 合并 python util
    pass

def node_vue():
    """
    根据操作系统类型搭建vue开发环境
    """
    node_cmds: [] = [r'sudo apt install nodejs', r'sudo apt install npm', r'npm intall -g n', r'sudo n stable']
    print('开始安装node')
    for cmd in node_cmds:
        os.system(cmd)
    cnpm_cmd = r'sudo npm install -g cnpm --registry=https://registry.npm.taobao.org'
    vue_cmd = r'sudo npm install -g @vue/cli'
    print('开始安装cnpm')
    os.system(cnpm_cmd)
    print('开始安装vue/cli')
    os.system(vue_cmd)

def main():
    """
    根据命令行参数搭建需要的环境
    """
    pass

if __name__ == '__main__':
    main()
    