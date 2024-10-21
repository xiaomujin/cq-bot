create table word_cloud
(
    id          integer not null
        primary key,
    sender_id   integer not null,
    group_id    integer not null,
    content     text    not null,
    time        text    not null,
    create_time text,
    create_by   text(255),
    update_time text,
    update_by   text(255)
);

