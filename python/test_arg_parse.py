import argparse


if __name__ == '__main__':
     parse = argparse.ArgumentParser(
         prog='测试程序',
         description=r'处理一些参数',
         epilog='补充字段描述')
     parse.add_argument('-s', type=str)
     parse.add_argument('-i', type=int, nargs="*")
     args = parse.parse_args()
     print(args.s)
     print(args.i)

