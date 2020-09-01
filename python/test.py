from downloader import Downloader
import platform
import os

if __name__ == "__main__":
    # downloader = Downloader()
    # downloader.download_node('Linux')
    path = "/home/czh/java_tools/test.txt"
    dir, file = os.path.split(path)
    print(dir)
    print(file)
    print(os.path.basename(path))
