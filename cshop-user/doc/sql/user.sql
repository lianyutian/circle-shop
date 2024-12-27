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


create table user_address
(
    id             bigint unsigned auto_increment primary key,
    user_id        bigint                               null comment '用户id',
    default_status tinyint(1) default 0                 null comment '是否默认收货地址：0否 1是',
    receive_name   varchar(64)                          null comment '收发货人姓名',
    phone          varchar(64)                          null comment '收货人电话',
    province       varchar(64)                          null comment '省/直辖市',
    city           varchar(64)                          null comment '市',
    region         varchar(64)                          null comment '区',
    detail_address varchar(200)                         null comment '详细地址',
    create_time    datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    update_time    datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '用户收货地址表' row_format = DYNAMIC;
create index uid on user_address (user_id);

