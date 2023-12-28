import os

def get_pid(port: int) -> list:
    """
    获取端口占用的进程pid
    : port: 端口
    : return: pid集合
    """
    # cmd = f'lsof -i:{port}'
    # res: str = os.system(cmd)
    # print(res) 
    res = """
    COMMAND   PID       USER   FD   TYPE             DEVICE SIZE/OFF NODE NAME
    java    64149 chenzhihao  243u  IPv6 0x25e823647852f715      0t0  TCP *:8084 (LISTEN)
    0
    """
    lines = res.split('\n')
    pids = []
    for line in lines:
        arr = line.split('\t')
        print(arr)


def check(port: int) -> bool:
    """
    检查端口是否被占用
    :params: port 端口
    """

def kill(port: int):
    """
    使用 kill -2 杀死端口锁占用的进程
    : port: 端口
    """
    pass

if __name__ == '__main__':
    get_pid("8084")