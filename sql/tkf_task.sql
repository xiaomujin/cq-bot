create table tkf_task
(
    id             integer    not null
        primary key,
    name           text(255)  not null,
    s_name         text(255)  not null,
    min_level      integer    not null,
    is_kappa       integer(1) not null,
    is_lightkeeper integer(1) not null,
    trader_name    text(255),
    trader_img     text(255),
    task_img       text(255),
    create_time    text,
    create_by      text(255),
    update_time    text,
    update_by      text(255),
    finish_reward  text,
    pre_task_id    text,
    id_str         text(127),
    pre_task       text
);

