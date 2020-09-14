from downloader import Downloader
import platform
import os
import shutil
import tempfile
import time
import json
import uuid


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
    json_str = r"""
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
    json_dict = json.loads(json_str)
    print(json_dict)
    download_url = json_dict['TBA'][0]['downloads']['linux']['link']
    # print(json_dict['TBA'][0]['downloads']['linux']['link'])
    print(download_url)
    # print(download_url[download_url.rfind(r'/'):])
    print(download_url.rfind(r'/'))
    print(uuid.uuid1())
    print(type(uuid.uuid1()))
    print(uuid.uuid1())
    print(str(uuid.uuid1()))
    # 73502768-74679344/103157351
    print(int(73502768 / 103157351 * 100))
    print("%.2f" % (73502768 / 103157351 * 100))
