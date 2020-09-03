#!/usr/bin python3
# -*- coding: utf-8 -*-

""" python处理压缩文件的工具, 暂时作废 """

import tarfile
import shutil


def untar(tar_file_path: str, untar_dir: str):
    """
    :tar_file_path: tar压缩文件路径, 文件必须存在, 否则报错
    :untar_dir: 解压文件路径, 路径必须存在, 否则报错
    """
    tar = tarfile.open(tar_file_path)
    tar.extractall(path=untar_dir)
    tar.close()


def test():
    tar_file_path = r'download_file/apache-maven-3.6.3-bin.tar.gz'
    # untar(tar_file_path, r'/home/czh/test')
    shutil.unpack_archive(tar_file_path, r'~/test')


if __name__ == '__main__':
    test()
