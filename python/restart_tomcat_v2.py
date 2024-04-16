import os
import sys
import platform
import shutil
import argparse


"""重启tomcat脚本"""


if __name__ == "__main__":
    parse = argparse.ArgumentParser(description="重启tomcat脚本")
    parse.add_argument("-tomcat_home", type=str, help="tomcat home目录")
    args = parse.parse_args()

    tomcat_home: str = args.tomcat_home

    # 执行停止脚本
    bin_path = os.path.join(tomcat_home, "bin")
    start_script_path: str = os.path.join(bin_path, "startup.sh")
    shutdown_script_path: str = os.path.join(bin_path, "shutdown.sh")
    os.system(start_script_path)
