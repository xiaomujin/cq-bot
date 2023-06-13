package com.kuroneko.cqbot.vo;

import lombok.Data;

import java.time.LocalDateTime;


@Data
public class XinfansInfo {
    private boolean isnew;
    private String id;
    private int wd;
    private String name;
    private LocalDateTime mtime;
    private String namefornew;
}
