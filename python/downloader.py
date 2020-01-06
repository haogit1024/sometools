from HttpClient import WindowsChrome
from bs4 import BeautifulSoup
import os
import logging


class Downloader(object):
    def __init__(self):
        self.browser = WindowsChrome()
        self.download_dir = 'download_file'

    def __get_maven_version(self):
        default_version = r'3.6.2'
        try:
            download_page_url = r'http://maven.apache.org/download.cgi'
            download_page_html = self.browser.get(download_page_url)
            soup = BeautifulSoup(download_page_html, 'lxml')
            # demo Apache Maven 3.6.2 is the latest release and recommended version for all users.
            p_version = soup.p.text
            p_version = p_version.replace("Apache Maven ", "")
            p_version = p_version.replace(" is the latest release and recommended version for all users.", "")
            default_version = p_version
            logging.info('获取到的maven版本号:' + default_version)
        except Exception as e:
            logging.exception(e)
        return default_version

    def download_maven(self):
        """
        下载maven
        :return: maven压缩包地址
        """
        base_url = r'http://mirror.bit.edu.cn/apache/maven/maven-3/%s/binaries/apache-maven-%s-bin.tar.gz'
        version = self.__get_maven_version()
        version ="3.6.2"
        download_url = base_url % (version, version)
        print('获取到maven的下载地址: ', download_url)
        print('开始下载maven')
        file_name = 'apache-maven-%s-bin.tar.gz' % version
        download_path = os.path.join(self.download_dir, file_name)
        self.browser.download(download_url, download_path)
        return download_path

    def download_node(self, platform_system):
        """
        下载node安装包
        :param platform_system: 操作系统类型: Windows Linux
        :return: 安装包地址
        :raise 不支持该操作系统
        """
        # windows https://nodejs.org/dist/v12.13.1/node-v12.13.1-x64.msi
        # linux https://nodejs.org/dist/v12.13.1/node-v12.13.1-linux-x64.tar.xz
        if platform_system == 'Windows':
            base_url = r'https://nodejs.org/dist/%s/node-%s-x64.msi'
        elif platform_system == 'Linux':
            base_url = r'https://nodejs.org/dist/%s/node-%s-linux-x64.tar.xz'
        else:
            print('暂不支持该系统')
            raise RuntimeError('暂不支持该系统')
        version = self.__get_node_version()
        download_url = base_url % (version, version)
        file_name = download_url[download_url.rfind(r'/') + 1:]
        file_path = os.path.join(r'download_file', file_name)
        self.browser.download(download_url, file_path)
        return file_path

    def __get_node_version(self):
        default_version = r'v12.13.1'
        try:
            node_html = self.browser.get('https://nodejs.org/en/')
            soup = BeautifulSoup(node_html, 'lxml')
            download_button = soup.select('.home-downloadbutton')[0]
            default_version = download_button.attrs['data-version']
            logging.info('获取到的node版本号:' + default_version)
        except Exception as e:
            logging.exception(e)
        return default_version


if __name__ == '__main__':
    downloader = Downloader()
    print(downloader.download_maven())
