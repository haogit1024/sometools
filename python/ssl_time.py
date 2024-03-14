import re
import subprocess
from datetime import datetime
import sys
import argparse

import requests

"""
检查SSL证书是否过期脚本
python3 -webhook http:xxxx -hosts xxx.com aaa.com bbb.com
"""


def get_re_match_result(pattern: str, string: str) -> str:
    match = re.search(pattern, string)
    return match.group(1)


def parse_time(date_str: str) -> datetime:
    gmt_format = r'%b  %d %H:%M:%S %Y GMT'
    return datetime.strptime(date_str, gmt_format)


def get_cert_info(domain: str) -> tuple[datetime, datetime, int]:
    cmd = f'curl -Ivs --connect-timeout 10 https://{domain}'
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


if __name__ == "__main__":
    parse = argparse.ArgumentParser(
        description=r'检查SSL证书是否过期',
        prog=r'检查SSL证书是否过期脚本')
    parse.add_argument('-webhook',type=str, help=r'飞书机器人webhook')
    parse.add_argument('-hosts', type=str, nargs="*", help=r'不带协议的域名list表，支持多个')
    args = parse.parse_args()
    feishu_webhook = args.webhook
    domains = args.hosts
    # print(feishu_webhook)
    # print(domains)
    content = "检查时间：" + datetime.now().strftime("%Y/%m/%d %H:%M:%S") + "\n\n"
    will_expire_domains = []
    expire_domains = []
    fail_domains = []
    for domain in domains:
        try:
            start_date, expire_date, expire_days = get_cert_info(domain)
            content = content + f'域名：{domain}\nSSL证书有效期：: {start_date} - {expire_date}\n有效期剩余天数：{expire_days}\n\n'
            if expire_days < 0:
                expire_domains.append(domain)
            elif expire_days < 5:
                will_expire_domains.append(domain)
        except Exception as e:
            print(e)
            fail_domains.append(domain)
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
    if len(fail_domains) > 0:
        content = content + "\n"
        content = content + "以下域名检查失败，请确认网络是否联通\n"
        for domain in fail_domains:
            content = content + domain + "，"
        content = content[:-1]
    print(content)
    if feishu_webhook is not None and feishu_webhook != '':
        json = {
            'msg_type': 'text',
            'content': {'text': content}
        }
        headers = {'Content-Type': 'application/json'}
        requests.post(url=feishu_webhook, headers=headers, json=json)