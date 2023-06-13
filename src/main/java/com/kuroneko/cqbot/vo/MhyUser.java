package com.kuroneko.cqbot.vo;

import lombok.Data;

@Data
public class MhyUser {
    private String game_biz;
    private String region;
    private String game_uid;
    private String nickname;
    private Integer level;
    private Boolean is_chosen;
    private String region_name;
    private Boolean is_official;
}
