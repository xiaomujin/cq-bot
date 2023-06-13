package com.kuroneko.cqbot.vo;

import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class DailyData {
    private LocalDate date;
    private List<String> news;
    private String weiyu;
    private String image;
    private String head_image;
}
