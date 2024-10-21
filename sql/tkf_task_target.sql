create table tkf_task_target
(
    id          integer    not null
        primary key,
    parent_id   integer    not null,
    type        text(255)  not null,
    description text(1000),
    is_optional integer(1) not null,
    count       integer,
    is_raid     integer(1) not null,
    create_time text,
    create_by   text(255),
    update_time text,
    update_by   text(255)
);

