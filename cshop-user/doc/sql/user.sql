create table user
(
    id          bigint unsigned auto_increment primary key,
    name        varchar(32)                           null comment '用户昵称',
    pwd         varchar(64)                           null comment '用户密码',
    avatar      varchar(524)                          null comment '用户头像',
    sex         tinyint(1)  default 1                 null comment '0 女，1 男',
    phone       varchar(11) default ''                null comment '用户手机号',
    create_time datetime    default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime    default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint phone_idx
        unique (phone)
) comment '用户表' row_format = DYNAMIC;

