import shutil
import os
import subprocess

zip_path = r'/root/work_space/redis.zip'
work_space_path = r'/root/work_space/redis_unpark'

if not os.path.exists(work_space_path):
    os.mkdir(work_space_path)

def stop_tomcat(port: str):
    prot_cmd = 'lsof -i:{}'.format(port)
    print('prot_cmd: ' + prot_cmd)
    cmd_ret = os.popen(prot_cmd)
    lines = cmd_ret.readlines()
    print("cmd_ret:")
    print(lines)
    lines_len = len(lines)
    if lines_len == 2:
        pid = __get_pid(lines[1])
        kill_cmd = 'kill  ' + pid
        print('kill_cmd: ' + kill_cmd)
        # os.popen(kill_cmd)
        subprocess.check_call(kill_cmd, shell=True)


def __get_pid(line: str):
    array = line.split(" ")
    print(array)
    index = 0
    for item in array:
        if item != "":
            index = index + 1
        if index == 2:
            return item


def unpark():
    shutil.rmtree(work_space_path)
    shutil.unpack_archive(zip_path, work_space_path)


def compile():
    # subprocess.check_call(r'mvn install:install-file -Dfile=./commons/commons-util/libs/webp-imageio-core-0.1.3.jar -DgroupId=com.github.nintha -DartifactId=webp-imageio-core -Dversion=0.1.3 -Dpackaging=jar', cwd=work_space_path, shell=True)
    # commons_path = os.path.join(work_space_path, 'commons')
    # subprocess.check_call('mvn clean install -Dmaven.test.skip=true', cwd=commons_path, shell=True)
    # admin_api_path = os.path.join(work_space_path, 'admin-api')
    # subprocess.check_call('mvn clean package -Ptest -Dmaven.test.skip=true', cwd=admin_api_path, shell=True)
    # admin_package_path = os.path.join(admin_api_path, 'target', 'admin-api.war')
    # print(admin_package_path)
    # shutil.copy(admin_package_path, r'/opt/tomcat/webapps')
    # subprocess.check_call('python3 restart_tomcat2.0.py 8080', cwd=r'/opt/tomcat/webapps', shell=True)
    subprocess.check_call('/opt/maven/bin/mvn clean package -Plocal -Dmaven.test.skip=true', cwd=work_space_path, shell=True)
    package_path = os.path.join(work_space_path, 'admin-api', 'target', 'admin-api.war')
    shutil.copy(package_path, r'/opt/tomcat/webapps')
    subprocess.check_call('python3 restart_tomcat2.0.py 8080',
                          cwd=r'/opt/tomcat/webapps',
                          shell=True)


if __name__ == '__main__':
    stop_tomcat('8080')
    unpark()
    compile()

