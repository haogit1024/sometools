#!/usr/bin python3
# -*- coding: utf-8 -*-

""" 安装python依赖 """

import os
import platform

def main():
    system = platform.system()
    if system == 'Windows':
        cmds = [r'pip install requests', r'pip install beautifulsoup4']
    elif system == 'Linux':
        cmds = [r'sudo apt install python3-pip python3-setuptools', r'pip3 install requests', r'pip3 install beautifulsoup4']
    else:
        print('操作系统 %s 在知识盲区内，暂时安装不了依赖' % system)
        return
    for cmd in cmds:
        os.system(cmd)


if __name__ == "__main__":
    main()
