package com.kuroneko.cqbot.vo;

import lombok.Data;

@Data
public class DailyVo {
    private int code;
    private String msg;
    private DailyData data;
    private long time;
    private long log_id;
}
