create table user_relation
(
    id              bigint auto_increment comment '主键id' primary key,
    user_id         bigint   default 0                 not null comment '博主id',
    attention_count bigint   default 0                 not null comment '关注数',
    follower_count  bigint   default 0                 not null comment '粉丝数',
    create_time     datetime default CURRENT_TIMESTAMP null comment '创建时间',
    update_time     datetime default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间',
    constraint uid unique (user_id)
) comment '用户关系表';

create table user_follower
(
    id          bigint auto_increment comment '主键id' primary key,
    user_id     bigint                               not null comment '博主id',
    follower_id bigint                               not null comment '粉丝id/关注博主的用户id',
    del         tinyint(1) default 0                 not null comment '是否删除 0 正常 1删除',
    create_time datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    update_time datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '粉丝关系表';
create index uid on user_follower (user_id, create_time);

create table user_attention
(
    id           bigint auto_increment comment '主键id' primary key,
    user_id      bigint                               not null comment '博主id',
    attention_id bigint                               not null comment '博主关注的用户id',
    del          tinyint(1) default 0                 not null comment '是否删除，0 正常 1 删除',
    create_time  datetime   default CURRENT_TIMESTAMP null comment '创建时间',
    update_time  datetime   default CURRENT_TIMESTAMP null on update CURRENT_TIMESTAMP comment '更新时间'
) comment '用户关注表';
create index uid on user_attention (user_id, create_time);





