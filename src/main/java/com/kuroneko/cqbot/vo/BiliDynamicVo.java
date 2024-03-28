package com.kuroneko.cqbot.vo;

import lombok.Data;

import java.util.List;

@Data
public class BiliDynamicVo {
    private Integer code;
    private String message;
    private BiliDynamicData data;

    @Data
    public static class BiliDynamicData {
        private List<BiliDynamicCard> items;
    }

    @Data
    public static class BiliDynamicCard {
        private String id_str;
        private Modules modules;
    }

    @Data
    public static class Modules {
        private ModuleAuthor module_author;
    }

    @Data
    public static class ModuleAuthor {
        private String face;
        private String name;
        private Long pub_ts;
        private String mid;
    }
}
