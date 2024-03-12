import requests
from HttpClient import WindowsChrome


def hello(a='a', b='b', c='c'):
    print(a)
    print(b)
    print(c)


def main():
    content = 'hello world'
    with open(r'test.cache', 'wb') as f:
        f.write(bytes(content, encoding='UTF=8'))
    with open(r'test.cache', 'rb') as f:
        print(bytes.decode(f.read(), encoding='UTF-8'))


def check_server(url: str) -> bool:
    retry_amt = 10
    while (retry_amt > 1):
        response = requests.get(url)
        http_code = response.status_code
        if http_code == 200:
            return True
        retry_amt = retry_amt - 1
    return False


if __name__ == "__main__":
    print(check_server("http://localhost:8002/actuator"))
