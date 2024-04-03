package com.kuroneko.cqbot.vo;

import lombok.Data;

@Data
public class ThreeDog {
    private String _id;
    private String location;
    private String lastReported;
    private String reportedBy;
    private int __v;

    public String getLocationCN() {
        return switch (location) {
            case "Shoreline" -> "海岸线";
            case "Customs" -> "海关";
            case "Woods" -> "森林";
            case "Lighthouse" -> "灯塔";
            default -> location;
        };
    }
}
