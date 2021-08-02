import os
import subprocess
import shutil

hxfw_home_path = r'D:\gitclones\hxfw\hxfw-api'
work_space_path = r'D:\work_space\hxfw'


def move_to_work_space():
    target_commons_path = os.path.join(work_space_path, r'commons')
    target_admin_api_path = os.path.join(work_space_path, r'admin-api')
    target_app_api_path = os.path.join(work_space_path, r'app-api')
    if os.path.exists(target_commons_path):
        shutil.rmtree(target_commons_path)
    if os.path.exists(target_admin_api_path):
        shutil.rmtree(target_admin_api_path)
    if os.path.exists(target_app_api_path):
        shutil.rmtree(target_app_api_path)
    commons_path = os.path.join(hxfw_home_path, 'commons')
    admin_api_path = os.path.join(hxfw_home_path, 'admin-api')
    app_api_path = os.path.join(hxfw_home_path, 'app-api')
    shutil.copytree(commons_path, target_commons_path)
    shutil.copytree(admin_api_path, target_admin_api_path)
    shutil.copytree(app_api_path, target_app_api_path)
    admin_api_logs_path = os.path.join(target_admin_api_path, r'logs')
    app_api_logs_path = os.path.join(target_app_api_path, r'logs')
    if os.path.exists(admin_api_logs_path):
        shutil.rmtree(admin_api_logs_path)
    if os.path.exists(app_api_logs_path):
        shutil.rmtree(app_api_logs_path)
    subprocess.check_call(r'mvn clean', cwd=target_commons_path, shell=True)
    # subprocess.check_call(r'mvn clean', cwd=target_admin_api_path, shell=True)
    admin_api_target = os.path.join(target_admin_api_path, 'target')
    if os.path.exists(admin_api_target):
        shutil.rmtree(admin_api_target)
    # subprocess.check_call(r'mvn clean', cwd=target_app_api_path, shell=True)
    app_api_target = os.path.join(target_app_api_path, 'target')
    if os.path.exists(app_api_target):
        shutil.rmtree(app_api_target)


def package_code_and_upload():
    shutil.make_archive(r'D:\work_space\hxfw', 'zip', r'D:\work_space\hxfw')
    subprocess.check_call('scp ./hxfw.zip root@1.15.223.154:/root/work_space/', cwd=r'D:\work_space', shell=True)
    subprocess.check_call(r'ssh root@1.15.223.154 "python3 /opt/tomcat/webapps/hxfw_unpark.py"',
                          cwd=os.path.dirname(r'D:\work_space'),
                          shell=True)


if __name__ == '__main__':
    move_to_work_space()
    package_code_and_upload()
