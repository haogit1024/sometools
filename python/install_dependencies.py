#!/usr/bin python3
# -*- coding: utf-8 -*-

""" 安装python依赖 """

import os

def main():
    cmds = [r'sudo apt install python3-pip python3-setuptools', r'pip3 install requests', r'pip3 install beautifulsoup4']
    for cmd in cmds:
        os.system(cmd)

if __name__ == "__main__":
    main()
