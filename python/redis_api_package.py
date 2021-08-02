import os
import subprocess
import shutil


project_home = r'D:\gitclones\my\redis-admin'
work_home = r'D:\work_space\redis'

# subprocess.check_call('mvn clean', cwd=project_home, shell=True)
# print('开始压缩文件')
# shutil.make_archive(project_home, 'zip', project_home)

if os.path.exists(work_home):
    shutil.rmtree(work_home)

os.mkdir(work_home)

# 移动文件
"""
admin-api/src
admin-api/pom.xml
common-api/src
common-api/pom.xml
framework-api/src
framework-api/pom.xml
"""
admin_api_src = os.path.join('admin-api', 'src')
admin_api_pom = os.path.join('admin-api', 'pom.xml')
common_src = os.path.join('common', 'src')
common_pom = os.path.join('common', 'pom.xml')
framework_src = os.path.join('framework', 'src')
framework_pom = os.path.join('framework', 'pom.xml')

shutil.copytree(os.path.join(project_home, admin_api_src), os.path.join(work_home, admin_api_src))
shutil.copy(os.path.join(project_home, admin_api_pom), os.path.join(work_home, admin_api_pom))

shutil.copytree(os.path.join(project_home, common_src), os.path.join(work_home, common_src))
shutil.copy(os.path.join(project_home, common_pom), os.path.join(work_home, common_pom))

shutil.copytree(os.path.join(project_home, framework_src), os.path.join(work_home, framework_src))
shutil.copy(os.path.join(project_home, framework_pom), os.path.join(work_home, framework_pom))

shutil.copy(os.path.join(project_home, 'pom.xml'), os.path.join(work_home, 'pom.xml'))

# 压缩文件并上传
shutil.make_archive(work_home, 'zip', work_home)
print("压缩完毕，开始上传")
zip_path = work_home + '.zip'
print('压缩包地址: ' + zip_path)
# print(os.path.dirname(work_home))
subprocess.check_call('scp ./redis.zip root@1.15.223.154:/root/work_space/',
                      cwd=os.path.dirname(work_home),
                      shell=True)
subprocess.check_call(r'ssh root@1.15.223.154 "python3 /opt/tomcat/webapps/redis_api_unpark.py"',
                      cwd=os.path.dirname(work_home),
                      shell=True)

