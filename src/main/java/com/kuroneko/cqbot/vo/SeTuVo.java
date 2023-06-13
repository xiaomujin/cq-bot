package com.kuroneko.cqbot.vo;

import lombok.Data;

import java.util.List;

@Data
public class SeTuVo {
    private String error;
    private List<SeTuData> data;
}
