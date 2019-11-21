#!/usr/bin/env python3
# -*- coding: utf-8 -*-

""" 一个tomcat重启脚本，会删除缓存文件 """


__author__ = 'chenzh'

import os, sys, platform, shutil


def delete_tomcat_cache(tomcat_home):
    """
    delete tomcat cache: delete tomcat_home/work/Catalina and tomcat_home/conf/Catalina
    """
    # 用os的函数屏蔽系统差异
    # work_dir_cache_path = tomcat_home + r"\work\Catalina"
    # conf_dir_cache_path = tomcat_home + r"\conf\Catalina"
    work_dir_cache_path = os.path.join(tomcat_home, 'work', 'Catalina')
    conf_dir_cache_path = os.path.join(tomcat_home, 'conf', 'Catalina')
    if os.path.exists(work_dir_cache_path):
        shutil.rmtree(work_dir_cache_path)
    if os.path.exists(conf_dir_cache_path):
        shutil.rmtree(conf_dir_cache_path)


def stop_tomcat_script(tomcat_home, os_type):
    """
    run tomcat_home/bin/shutdown.bat / tomcat_home/bin/shutdown.sh
    """
    cmd = ''
    if os_type == 'Windows':
        cmd = tomcat_home + r"\bin\shutdown.bat"
    elif os_type == 'Linux':
        cmd = tomcat_home + "/bin/shutdown.sh"
    # os.popen(cmd)
    os.system(cmd)
    print(cmd)


def start_tomcat_script(tomcat_home, os_type):
    """
    run tomcat_home/bin/startup.bat / tomcat_home/bin/startup.sh
    """
    cmd = ''
    if os_type == 'Windows':
        cmd = tomcat_home + r'\bin\startup.bat'
    elif os_type == 'Linux':
        cmd = tomcat_home + "/bin/startup.sh"
    # os.popen(cmd)
    os.system(cmd)
    print(cmd)


def start_tomcat_server(tomcat_server, os_type):
    """
    run net start server_name / service server_name start
    """
    cmd = ''
    if os_type == 'Windows':
        cmd = 'net start {}'.format(tomcat_server)
    elif os_type == 'Linux':
        cmd = 'service {} start'.format(tomcat_server)
    print(cmd)
    os.system(cmd)


def stop_tomcat_server(tomcat_server, os_type):
    """
    run net stop server_name / service server_name stop
    """
    cmd = ''
    if os_type == 'Windows':
        cmd = 'net stop {}'.format(tomcat_server)
    elif os_type == 'Linux':
        cmd = 'service {} stop'.format(tomcat_server)
    print(cmd)
    os.system(cmd)


def delete_wabapp_cache(tomcat_home: str):
    webapp_path = os.path.join(tomcat_home, r'webapps')
    war_file_list = []
    file_list = os.listdir(webapp_path)
    print(file_list)
    war_file_list = []
    if len(file_list) > 0:
        for f in file_list:
            if r'.' in f:
                file_section = f.split(r'.')
                if file_section[1] == 'war':
                    war_file_list.append(file_section[0])
                    war_file_cache = os.path.join(webapp_path, file_section[0])
                    if os.path.exists(war_file_cache) and os.path.isdir(war_file_cache):
                        shutil.rmtree(war_file_cache)


if __name__ == "__main__":
    # TODO 去掉服务启动，方法代码可以保留，但是main里去掉
    args_length = len(sys.argv)
    # 参数默认值
    # 脚本默认放在webapps目录下, 获取tomcat_home目录
    curr_path = os.path.dirname(os.path.abspath(__file__))
    tomcat_home = os.path.dirname(curr_path)
    # 类型 1.脚本启动 2.服务启动, 默认是脚本启动
    tocmat_type = 1
    # tomcat服务名，默认为tomcat
    tomcat_server_name = 'tomcat'
    # tomcat_home默认路径 默认脚本放在webapps目录下
    # 第一个参数为操作类型
    if args_length > 1:
        tocmat_type = sys.argv[1]
    # 第二个参数为tomcat_home目录路径或者服务名
    if args_length > 2:
        tomcat_home = sys.argv[2]
    # 第三个参数为tomcat服务名
    if args_length > 3:
        tomcat_server_name = sys.argv[3]
    # 获取操作系统类型 Windows / Linux
    os_type = platform.system()
    print('os_type=', os_type)
    if os_type != 'Windows' and os_type != 'Linux':
        print('运行失败，目前只支持Windows和Linux操作系统')
        exit(0)
    # """
    if tocmat_type == 2:
        # !服务启动需要获取管理员权限,Linux可以在base shell脚本里获取和Windows可以在bat脚本获取,用服务会暴露管理员账号密码,强烈建议不要用这个
        # 服务启动
        stop_tomcat_server(tomcat_server_name, os_type)
        delete_tomcat_cache(tomcat_home)
        delete_wabapp_cache(tomcat_home)
        start_tomcat_server(tomcat_server_name, os_type)
    else:
        # 脚本启动
        stop_tomcat_script(tomcat_home, os_type)
        delete_tomcat_cache(tomcat_home)
        delete_wabapp_cache(tomcat_home)
        start_tomcat_script(tomcat_home, os_type)
    # """
    print("tomcat_home: " + tomcat_home)
    print("重新启动完成")
