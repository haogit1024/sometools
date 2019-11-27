import requests
import sys
import time


def main(http_url: str, time_out_second: int) -> bool :
    time.sleep(30)
    sum_time: int = 30
    while sum_time <= time_out_second:
        response = requests.get(http_url)
        if response.status_code == 200:
            return True
        time.sleep(5)
        sum_time = sum_time + 5;
    return False


if __name__ == '__main__':
    argv = sys.argv
    # http_url = argv[1]
    http_url = r"https://mpapi.cyui.cn/gas_mini/car/lock"
    if len(argv) >= 3:
        time_out_second = argv[2]
    else:
        time_out_second = 60

    print('http_url: ' + http_url)
    res = main(http_url, time_out_second)
    print("res:", res)
    if not res:
        raise RuntimeError("error")
