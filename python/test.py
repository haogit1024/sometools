from downloader import Downloader
import platform
import os
import shutil
import tempfile
import time


if __name__ == "__main__":
    # downloader = Downloader()
    # downloader.download_node('Linux')
    print(os.environ['HOME'])
    print(os.path.expandvars('$HOME'))
    print(os.path.expanduser('~'))
    print(tempfile.gettempdir())
    print('-----------')
    files = os.listdir(r'/home/czh/maven')
    print(files)
    print(os.path.exists(r'/usr/bin/java'))
    print(time.time())
