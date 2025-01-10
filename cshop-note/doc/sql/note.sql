create table note
(
    id            bigint unsigned auto_increment primary key,
    user_id       bigint                               null comment '用户id',
    img_urls      varchar(255)                         null comment '笔记图片链接',
    title         varchar(32)                          null comment '笔记标题',
    content       varchar(64)                          null comment '正文内容',
    video_url     varchar(255)                         null comment '笔记视频链接',
    declaration   varchar(64)                          null comment '自主声明',
    visible_range tinyint(1) default 1                 null comment '1 公开可见，2 仅自己可见，3 仅互关好友可见，4 部分人可见 5 部分人不可见',
    status        tinyint(1) default 1                 null comment '1 审核中, 2 未通过，3 已发布',
    publish_time  datetime   default CURRENT_TIMESTAMP null comment '发布时间',
    create_time   datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    update_time   datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '用户笔记表' row_format = DYNAMIC;
CREATE INDEX user_id_id_idx ON note (user_id, id);