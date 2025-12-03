package com.kuroneko.cqbot.vo;

import lombok.Data;

import java.util.ArrayList;

@Data
public class BaVo {
    private Integer code = 0;
    private String message;
    private ArrayList<BaImage> data;

    @Data
    public static class BaImage {
        private String name;
        private String hash;
        private String content;
        /**
         * file: 此时content中的内容为图片的cdn地址 <br/>
         * plain: 此时content中的内容为纯文本，字面意思 <br/>
         */
        private String type;
    }
}
