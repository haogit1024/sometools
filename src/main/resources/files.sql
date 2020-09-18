create table file_size (
    `id`            int(11)         NOT NULL AUTO_INCREMENT,
    file_system     varchar(100)    COMMENT '文件系统',
    parent_dir      text            COMMENT '夫文件夹',
    file_path       text            COMMENT '文件路径',
    file_name       text            COMMENT '文件名字',
    file_size       bigint(20)      COMMENT '文件字节大小',
    is_dir          tinyint(1)      COMMENT '是否是文件夹',
    scan_time       int(11)         COMMENT '扫描时间',
    PRIMARY KEY (`id`)
)