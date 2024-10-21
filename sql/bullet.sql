create table bullet
(
    id                integer not null
        primary key,
    caliber           text(255),
    name              text(255),
    tracer            integer,
    subsonic          integer,
    no_market         integer,
    damage            text(255),
    penetration_power integer,
    armor_damage      integer,
    accuracy          integer,
    recoil            integer,
    frag_chance       integer,
    bleed_lt          integer,
    bleed_hvy         integer,
    speed             integer,
    effectiveness_lv1 integer,
    effectiveness_lv2 integer,
    effectiveness_lv3 integer,
    effectiveness_lv4 integer,
    effectiveness_lv5 integer,
    effectiveness_lv6 integer,
    create_time       text,
    create_by         text(255),
    update_time       text,
    update_by         text(255)
);