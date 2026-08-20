import argparse
import math
import re
import subprocess
from datetime import datetime, timezone

import requests

"""
检查SSL证书是否过期脚本

用法:
    python3 ssl_time.py -hosts xxx.com aaa.com bbb.com [-webhook https://open.feishu.cn/open-apis/bot/v2/hook/xxxx]
"""

# 全局hosts
hosts = {
    'jenkins.czhstudio.cn': '106.52.34.38'
}


def get_re_match_result(pattern: str, string: str) -> str:
    match = re.search(pattern, string)
    if match is not None:
        return match.group(1)
    return ""


def parse_time(date_str: str) -> datetime:
    """解析curl输出的GMT时间，返回带UTC时区的datetime"""
    gmt_format = "%b %d %H:%M:%S %Y GMT"
    return datetime.strptime(date_str, gmt_format).replace(tzinfo=timezone.utc)


def get_cert_info(domain: str) -> tuple[datetime, datetime, int]:
    """获取域名SSL证书信息，返回(签发时间, 到期时间, 剩余天数)"""
    cmd = [
        "curl", "-Ivs",
        "--connect-timeout", "10",
        "--max-time", "10",
    ]
    hosts_ip = hosts.get(domain)
    if hosts_ip is not None:
        # 模拟hosts
        cmd += ["--resolve", f"{domain}:443:{hosts_ip}"]
    cmd.append(f"https://{domain}")

    result = subprocess.run(cmd, capture_output=True, text=True, timeout=15)
    output = result.stderr + result.stdout
    # 正则匹配
    start_date = get_re_match_result("start date: (.*)", output)
    expire_date = get_re_match_result("expire date: (.*)", output)
    if not start_date or not expire_date:
        raise ValueError(f"解析证书失败, curl退出码: {result.returncode}")

    # 解析匹配结果
    start_date = parse_time(start_date)
    expire_date = parse_time(expire_date)
    now = datetime.now(timezone.utc)
    expire_days = math.ceil((expire_date - now).total_seconds() / 86400)
    return start_date, expire_date, expire_days


if __name__ == "__main__":
    parse = argparse.ArgumentParser(
        description=r"检查SSL证书是否过期", prog=r"检查SSL证书是否过期脚本"
    )
    parse.add_argument("-webhook", type=str, help=r"飞书机器人webhook")
    parse.add_argument(
        "-hosts", type=str, nargs="*", help=r"不带协议的域名list表，支持多个"
    )
    args = parse.parse_args()
    feishu_webhook = args.webhook
    # 去重并保持顺序
    domains = list(dict.fromkeys(args.hosts or []))

    now = datetime.now().strftime("%Y/%m/%d %H:%M:%S")
    sections = [f"检查时间：{now}"]
    domain_lines = []
    will_expire_domains = []
    expire_domains = []
    fail_domains = []
    for domain in domains:
        try:
            start_date, expire_date, expire_days = get_cert_info(domain)
            domain_lines.append(
                f"域名：{domain}\n"
                f"SSL证书有效期：{start_date} - {expire_date}\n"
                f"有效期剩余天数：{expire_days}"
            )
            if expire_date < datetime.now(timezone.utc):
                expire_domains.append(domain)
            elif expire_days < 5:
                will_expire_domains.append(domain)
        except Exception as e:
            print(f"检查 {domain} 失败: {e}")
            fail_domains.append(domain)

    if domain_lines:
        sections.append("\n\n".join(domain_lines))
    if will_expire_domains:
        sections.append("以下域名将要过期，建议更换\n" + "，".join(will_expire_domains))
    if expire_domains:
        sections.append("以下域名已过期，请尽快更换\n" + "，".join(expire_domains))
    if fail_domains:
        sections.append("以下域名检查失败，请确认网络是否联通\n" + "，".join(fail_domains))

    content = "\n\n".join(sections)
    print(content)

    if feishu_webhook:
        payload = {"msg_type": "text", "content": {"text": content}}
        headers = {"Content-Type": "application/json"}
        try:
            requests.post(url=feishu_webhook, headers=headers, json=payload, timeout=10)
        except Exception as e:
            print(f"发送飞书通知失败: {e}")