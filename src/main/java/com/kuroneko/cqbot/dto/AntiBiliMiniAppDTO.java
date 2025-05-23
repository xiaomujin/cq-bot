package com.kuroneko.cqbot.dto;

import lombok.Data;

@Data
public class AntiBiliMiniAppDTO {
    private Integer code;
    private BLData data;
    private String message;


    @Data
    public static class BLData {
        private String bvid;
        private String pic;
        private String title;
        private Long pubdate;
        private Owner owner;
        private Stat stat;

        @Data
        public static class Owner {
            private String name;
        }

        @Data
        public static class Stat {
            private Long aid;
            private Long view;
            private Long danmaku;
            private Long reply;
            private Long coin;
            private Long share;
            private Long like;
        }
    }


}
