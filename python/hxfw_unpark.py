import shutil
import os
import subprocess

zip_path = r'/root/work_space/hxfw.zip'
work_space_path = r'/root/work_space/unpark'


def unpark():
    shutil.rmtree(work_space_path)
    shutil.unpack_archive(zip_path, work_space_path)


def compile():
    subprocess.check_call(r'mvn install:install-file -Dfile=./commons/commons-util/libs/webp-imageio-core-0.1.3.jar -DgroupId=com.github.nintha -DartifactId=webp-imageio-core -Dversion=0.1.3 -Dpackaging=jar', cwd=work_space_path, shell=True)
    commons_path = os.path.join(work_space_path, 'commons')
    subprocess.check_call('mvn clean install -Dmaven.test.skip=true', cwd=commons_path, shell=True)
    admin_api_path = os.path.join(work_space_path, 'admin-api')
    subprocess.check_call('mvn clean package -Ptest -Dmaven.test.skip=true', cwd=admin_api_path, shell=True)
    admin_package_path = os.path.join(admin_api_path, 'target', 'admin-api.war')
    print(admin_package_path)
    shutil.copy(admin_package_path, r'/opt/tomcat/webapps')
    subprocess.check_call('python3 restart_tomcat2.0.py 8080', cwd=r'/opt/tomcat/webapps', shell=True)


if __name__ == '__main__':
    unpark()
    compile()
