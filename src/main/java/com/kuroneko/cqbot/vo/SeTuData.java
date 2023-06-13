package com.kuroneko.cqbot.vo;

import lombok.Data;

import java.util.List;

@Data
public class SeTuData {
    private long pid;
    private int p;
    private long uid;
    private String title;
    private String author;
    private boolean r18;
    private int width;
    private int height;
    private List<String> tags;
    private String ext;
    private long uploadDate;
    private SeTuUrls urls;

    public String getImgUrl() {
        if (this.urls.getRegular() != null) {
            return this.urls.getRegular();
        } else if (this.urls.getSmall() != null) {
            return this.urls.getSmall();
        } else {
            return null;
        }
    }
}
