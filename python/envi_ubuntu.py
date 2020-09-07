#!/usr/bin/python3
# -*- coding: utf-8 -*-

""" 一个搭建编程环境脚本脚本 """

import os
import sys
import shutil
import tempfile
import platform
from downloader import Downloader


# 当前登录用户主目录, 例如: /home/czh
home_dir = os.path.expanduser('~')
temp_dir = tempfile.gettempdir()
# 创建 develop_tools_dir 文件夹, 把 maven_tar 解压到 develop_tools_dir 目录下
develop_tools_dir = home_dir + r'/develop_tools'
if not os.path.exists(develop_tools_dir):
    os.mkdir(develop_tools_dir)
downloader = Downloader()


def java():
    """
    ubuntu安装openjdk, fuck orcale
    """
    java_cmd: str = r'sudo apt-get install openjdk-8-jdk openjdk-8-jre'
    print("开始安装openjdk8")
    os.system(java_cmd)
    print('开始安装maven')
    # 把返回的文件路径复制到对应的地方并解压
    maven_file_path = downloader.download_maven()
    print('下载maven完成')
    print('开始解压')
    # 换个思路, 先解压到临时文件夹, 然后把复制到 develop_tools_dir 中重命名
    maven_temp_dir = temp_dir + "/czhmaven"
    # 如果不存在就创建, 存在则清空文件下的所有文件
    if os.path.exists(maven_temp_dir):
        os.removedirs(maven_temp_dir)
    os.mkdir(maven_temp_dir)
    shutil.unpack_archive(maven_file_path, maven_temp_dir)
    # 获取临时文件的第一个文件名
    maven_untar_name = os.listdir(maven_temp_dir)[0]
    # 移动到 develop_tools_dir 下并重命名, 和 mv 命令一样的效果
    maven_temp_path = maven_temp_dir + r"/" + maven_untar_name
    maven_final_dir = develop_tools_dir + r"/maven"
    shutil.move(maven_temp_path, maven_final_dir)
    # 删除临时文件夹
    os.removedirs(maven_temp_dir)


def node_vue():
    """
    根据操作系统类型搭建vue开发环境
    TODO 改为下载最新的安装包, 然后配置系统参数
    """
    node_cmds: [] = [r'sudo apt install npm', r'sudo npm --registry=https://registry.npm.taobao.org intall -g n']
    print('开始安装node')
    for cmd in node_cmds:
        os.system(cmd)
    vue_cmd = r'sudo npm --registry=https://registry.npm.taobao.org install -g @vue/cli'
    print('开始安装vue/cli')
    os.system(vue_cmd)


def thefuck():
    os.system(r'sudo pip3 install thefuck')


def ohmyzsh():
    os.system('sudo apt install wget')
    os.system(r'sh -c "$(fetch -o - https://raw.githubusercontent.com/ohmyzsh/ohmyzsh/master/tools/install.sh)"')


def vim():
    os.system('sudo apt install vim neovim')


def toolbox():
    toolbox_path = downloader.download_toolbox(platform.system())
    shutil.copy(toolbox_path, develop_tools_dir)


def main():
    """
    根据命令行参数搭建需要的环境
    """
    envi_type = sys.argv[1]
    if envi_type == 'vue':
        node_vue()
    elif envi_type == 'java':
        java()
    elif envi_type == 'all':
        java()
        node_vue()
        ohmyzsh()
        thefuck()
    elif envi_type == 'dev':
        java()
        node_vue()
    else:
        print('请输入要生成的环境类型')


if __name__ == '__main__':
    try:
        main()
    except Exception as e:
        print(e)
    finally:
        # 关闭下载器
        downloader.close()
