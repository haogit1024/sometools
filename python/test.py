import platform
from logging import log
from HttpClient import WindowsChrome
from bs4 import BeautifulSoup
import requests

if __name__ == "__main__":
    # https://nodejs.org/en/
    # https://nodejs.org/dist/v12.13.1/node-v12.13.1-x64.msi
    # https://nodejs.org/dist/v12.13.1/node-v12.13.1-linux-x64.tar.xz
    node_html = requests.get('https://nodejs.org/en/').text
    soup = BeautifulSoup(node_html, 'lxml')
    download_button = soup.select('.home-downloadbutton')[0]
    print(download_button.attrs['data-version'])
