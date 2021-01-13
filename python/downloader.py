from HttpClient import WindowsChrome
from bs4 import BeautifulSoup
import os
import logging
import time
import json


class Downloader(object):
    def __init__(self):
        self.browser = WindowsChrome(is_enable_request_cache=True)
        self.download_dir = 'download_file'

    def __get_maven_version(self):
        default_version = r'3.6.2'
        try:
            download_page_url = r'http://maven.apache.org/download.cgi'
            download_page_html = self.browser.get(download_page_url)
            # soup = BeautifulSoup(download_page_html, 'lxml')
            soup = BeautifulSoup(download_page_html, 'html.parser')
            # demo Apache Maven 3.6.2 is the latest release and recommended version for all users.
            p_version = soup.p.text
            print(r'p_version: ', p_version)
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
        # version ="3.6.2"
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
            soup = BeautifulSoup(node_html, 'html.parser')
            download_button = soup.select('.home-downloadbutton')[0]
            default_version = download_button.attrs['data-version']
            logging.info('获取到的node版本号:' + default_version)
        except Exception as e:
            logging.exception(e)
        return default_version

    def download_toolbox(self, platform_system: str):
        """
        : platform_system: 操作系统类型, 为 platform.system() 获取到的值
        """
        # https://data.services.jetbrains.com/products/releases?code=TBA&latest=true&type=release&build=&_=1599128169044
        try:
            url = r'https://data.services.jetbrains.com/products/releases?code=TBA&latest=true&type=release&build=&_=' + str(int(time.time() * 1000))
            res_json = self.browser.get(url, encoding='UTF-8')
            """
{
    "TBA": [
        {
            "date": "2020-09-01",
            "type": "release",
            "downloads": {
                "linux": {
                    "link": "https://download.jetbrains.com/toolbox/jetbrains-toolbox-1.18.7455.tar.gz",
                    "size": 93224317,
                    "checksumLink": "https://download.jetbrains.com/toolbox/jetbrains-toolbox-1.18.7455.tar.gz.sha256"
                },
                "windows": {
                    "link": "https://download.jetbrains.com/toolbox/jetbrains-toolbox-1.18.7455.exe",
                    "size": 73197984,
                    "checksumLink": "https://download.jetbrains.com/toolbox/jetbrains-toolbox-1.18.7455.exe.sha256"
                },
                "mac": {
                    "link": "https://download.jetbrains.com/toolbox/jetbrains-toolbox-1.18.7455.dmg",
                    "size": 95727142,
                    "checksumLink": "https://download.jetbrains.com/toolbox/jetbrains-toolbox-1.18.7455.dmg.sha256"
                }
            },
            "patches": {},
            "notesLink": null,
            "licenseRequired": null,
            "version": "1.18",
            "majorVersion": "1.18",
            "build": "1.18.7455",
            "whatsnew": "<p>Check out the <a href=\"https://blog.jetbrains.com/blog/2020/09/01/dark-theme-in-toolbox-app-1-18/\">blog post.</a></p>",
            "uninstallFeedbackLinks": null,
            "printableReleaseType": null
        }
    ]
}
            """
            print(res_json)
            json_dict = json.loads(res_json)
            download_url = json_dict['TBA'][0]['downloads'][platform_system.lower()]['link']
            print(download_url)
            file_name = download_url[download_url.rfind(r'/') + 1:]
            file_path = os.path.join(r'download_file', file_name)
            self.browser.download(download_url, file_path)
            return file_path
        except Exception as e:
            logging.exception(e)

    def download_adopt_open_jdk(self, version: str, type: str, platform_system: str):
        """下载AdoptOpenJdk方法

        Args:
            version (str): jdk版本号, 8, 9, 10, 11, 12, 13, 14
            type (str): jdk/jre
            platform_system (str): platform模块获取的系统名称, Windows, Linux
        """
        platform_system = platform_system.lower()
        # demo https://mirrors.tuna.tsinghua.edu.cn/AdoptOpenJDK/11/jdk/x64/linux/OpenJDK11U-jdk_x64_linux_hotspot_11.0.8_10.tar.gz
        base_url = r'https://mirrors.tuna.tsinghua.edu.cn/AdoptOpenJDK/%s/%s/x64/%s'
        base_url = base_url % (version, type, platform_system)
        download_page = self.browser.get(base_url, r'utf-8')
        soup = BeautifulSoup(download_page, 'html.parser')
        # table = soup.find('table', attrs={'id': 'list'})
        # print(table)
        # tr_list = table.find('tbody').findAll('tr')
        # if (len(tr_list) >= 2):
        #     tr = tr_list[1]
        #     td_list = tr.find('td', attrs={'class': 'link'})
        td_list = soup.findAll('td', attrs={'class': 'link'})
        if len(td_list) >= 2:
            td = td_list[1]
            a = td.find('a')
            jdk_file_name = a['href']
            download_url = base_url + r'/' + jdk_file_name
            file_path = os.path.join(r'download_file', jdk_file_name)
            self.browser.download(download_url, file_path)
            return file_path
        else:
            return None

    def close(self):
        """
        调用方必须手动调用该方法关闭下载器
        """
        self.browser.close(True)


if __name__ == '__main__':
    downloader = Downloader()
    # downloader.download_node('Linux')
    # downloader.download_node('Windows')
    downloader.download_maven()
    # downloader.download_toolbox('linux')
    # downloader.download_toolbox('Windows')
    # downloader.download_adopt_open_jdk(r'8', r'jdk', r'Linux')
    # downloader.download_adopt_open_jdk(r'8', r'jdk', r'Windows')
    print('运行完成')
