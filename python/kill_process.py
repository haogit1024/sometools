import os
import argparse

"""杀掉某个进程"""

def get_process_info(name: str):
    pass

if __name__ == '__main__':
    parse = argparse.ArgumentParser(
        description="杀掉某个进程" 
    )
    parse.add_argument("-name", type=str, help="进程名")
    args = parse.parse_args()
    process_name: str = args.name
    print(f"name = {process_name}")
