package com.kuroneko.cqbot.vo;

import lombok.Data;

import java.util.List;

@Data
public class BiliDynamicVo {
    private Integer code;
    private String msg;
    private String message;
    private BiliDynamicData data;

    @Data
    public static class BiliDynamicData {
        private List<BiliDynamicCard> cards;

    }

    @Data
    public static class BiliDynamicCard {
        private BiliDynamicDesc desc;

    }

    @Data
    public static class BiliDynamicDesc {
        private String dynamic_id_str;
        private UserProfile user_profile;

    }
    @Data
    public static class UserProfile {
        private BUserInfo info;

    }
    @Data
    public static class BUserInfo {
        private String uid;
        private String uname;
        private String face;

    }
}
