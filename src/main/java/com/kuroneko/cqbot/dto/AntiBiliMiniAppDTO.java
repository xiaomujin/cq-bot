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
        private Owner owner;
        private Stat stat;

        @Data
        public static class Owner {
            private String name;
        }

        @Data
        public static class Stat {
            private Integer aid;
            private Integer view;
            private Integer danmaku;
            private Integer reply;
            private Integer coin;
            private Integer share;
            private Integer like;
        }
    }


}
