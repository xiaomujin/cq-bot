package com.kuroneko.cqbot.vo;

import lombok.Data;

@Data
public class BaRankInfo {
    private Integer rank;
    private Long bestRankingPoint;
    private Integer level;
    private String nickname;
    private Integer representCharacterUniqueId;
    private Integer tier;
    private String hard;
    private String battleTime;
    private Integer bossId;
    private Long recordTime;
    private Integer dataType;
}
