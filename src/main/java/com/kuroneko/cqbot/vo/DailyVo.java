package com.kuroneko.cqbot.vo;

import lombok.Data;

@Data
public class DailyVo {
    private Integer code;
    private DailyData data;
    private String imgUrl;

    @Data
    public static class DailyData {
        private String image;
    }
}
