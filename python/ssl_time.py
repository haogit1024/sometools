import re
import subprocess
from datetime import datetime
import sys

import requests

"""
python3 python/ssl_time.py https://open.feishu.cn/open-apis/bot/v2/hook/xxxxxxxxxxxxxxxxxxxxxxxx aaa.com
第一个参数是飞书的web_hook
第二个以后得参数是域名列表
"""


def get_re_match_result(pattern: str, string: str) -> str:
    match = re.search(pattern, string)
    return match.group(1)


def parse_time(date_str: str) -> datetime:
    gmt_format = r'%b  %d %H:%M:%S %Y GMT'
    return datetime.strptime(date_str, gmt_format)


def get_cert_info(domain: str) -> tuple[datetime, datetime, int]:
    cmd = f'curl -Ivs https://{domain}'
    exitcode, output = subprocess.getstatusoutput(cmd)
    # print(f'exitcode={exitcode}')
    # 正则匹配
    start_date = get_re_match_result('start date: (.*)', output)
    expire_date = get_re_match_result('expire date: (.*)', output)
    # 解析匹配结果
    start_date = parse_time(start_date)
    expire_date = parse_time(expire_date)

    expire_days = (expire_date - datetime.now()).days
    return start_date, expire_date, expire_days


def test():
    domain = sys.argv[1]
    start_date, expire_date, expire_days = get_cert_info(domain)
    print(f'start date: {start_date}, expire date: {expire_days}, expire days: {expire_days}')
    if len(sys.argv) > 2:
        feishu_webhook = sys.argv[2]
        # 发送飞书消息通知
        content = f"""
    域名：{domain}
    SSL正式有效期: {start_date} - {expire_date}
    过期天数：{expire_days}
            """
        content = content.strip()
        payload = {
            'msg_type': 'text',
            'content': {'text': content}
        }
        headers = {'Content-Type': 'application/json'}
        requests.post(url=feishu_webhook, headers=headers, json=payload)


if __name__ == "__main__":
    feishu_webhook = sys.argv[1]
    domains = sys.argv[2:]
    # print(feishu_webhook)
    # print(domains)
    content = ""
    will_expire_domains = []
    expire_domains = []
    for domain in domains:
        start_date, expire_date, expire_days = get_cert_info(domain)
        content = content + f'域名：{domain}\nSSL证书有效期：: {start_date} - {expire_date}\n有效期剩余天数：{expire_days}\n'
        if expire_days < 0:
            expire_domains.append(domain)
        elif expire_days < 5:
            will_expire_domains.append(domain)
    if len(will_expire_domains) > 0:
        content = content + "\n"
        content = content + "以下域名将要过期，建议更换\n"
        for domain in will_expire_domains:
            content = content + domain + "，"
        content = content[:-1]
        content = content + "\n"
    if len(expire_domains) > 0:
        content = content + "\n"
        content = content + "以下域名已过期，请尽快更换\n"
        for domain in expire_domains:
            content = content + domain + "，"
        content = content[:-1]
    payload = {
        'msg_type': 'text',
        'content': {'text': content}
    }
    headers = {'Content-Type': 'application/json'}
    requests.post(url=feishu_webhook, headers=headers, json=payload)