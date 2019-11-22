import platform
from logging import log
from HttpClient import WindowsChrome
from bs4 import BeautifulSoup
from downloader import download_maven, __get_maven_version

if __name__ == "__main__":
    __get_maven_version()
