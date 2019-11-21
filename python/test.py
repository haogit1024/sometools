import platform
from logging import log


if __name__ == "__main__":
    print("platform.machine()=%s", platform.machine());
    print("platform.node()=%s", platform.node());
    print("platform.platform()=%s", platform.platform());
    print("platform.processor()=%s", platform.processor());
    print("platform.python_build()=%s", platform.python_build());
    print("platform.python_compiler()=%s", platform.python_compiler());
    print("platform.python_branch()=%s", platform.python_branch());
    print("platform.python_implementation()=%s", platform.python_implementation());
    print("platform.python_revision()=%s", platform.python_revision());
    print("platform.python_version()=%s", platform.python_version());
    print("platform.python_version_tuple()=%s", platform.python_version_tuple());
    print("platform.release()=%s", platform.release());
    print("platform.system()=%s", platform.system());
    #logging.info("platform.system_alias()=%s", platform.system_alias());
    print("platform.version()=%s", platform.version());
    print("platform.uname()=%s", platform.uname());