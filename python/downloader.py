from HttpClient import WindowsChrome
from bs4 import BeautifulSoup
import os


class Downloader(object):
    def __init__(self):
        self.browser = WindowsChrome()
        self.download_dir = 'download_file'

    def __get_maven_version(self):
        download_page_url = r'http://maven.apache.org/download.cgi'
        download_page_html = self.browser.get(download_page_url)
        soup = BeautifulSoup(download_page_html, 'lxml')
        # Apache Maven 3.6.2 is the latest release and recommended version for all users.
        p_version = soup.p.text
        p_version = p_version.replace("Apache Maven ", "")
        p_version = p_version.replace(" is the latest release and recommended version for all users.", "")
        return p_version

    def download_maven(self):
        """
        下载maven
        :return: maven压缩包地址
        """
        base_url = r'http://mirror.bit.edu.cn/apache/maven/maven-3/%s/binaries/apache-maven-%s-bin.tar.gz'
        default_version = r'3.6.2'
        try:
            version = self.__get_maven_version()
        except Exception as e:
            print(e)
            version = default_version
        download_url = base_url % (version, version)
        print('获取到maven的下载地址: ', download_url)
        print('开始下载maven')
        file_name = 'apache-maven-%s-bin.tar.gz' % (version)
        download_path = os.path.join(self.download_dir, file_name)
        self.browser.download(download_url, download_path)
        return download_path

    def download_node(self, os_type):
        """
        下载node安装包
        :param os_type: 操作系统类型: Windows Linux
        :return: 安装包地址
        """
        # windows https://nodejs.org/dist/v12.13.1/node-v12.13.1-x64.msi
        # linux https://nodejs.org/dist/v12.13.1/node-v12.13.1-linux-x64.tar.xz
        pass

    def __get_node_version(self):
        pass


if __name__ == '__main__':
    downloader = Downloader()
    print(downloader.download_maven())
