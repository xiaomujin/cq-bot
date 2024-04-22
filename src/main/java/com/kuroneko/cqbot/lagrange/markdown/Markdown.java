//package com.kuroneko.cqbot.lagrange.markdown;
//
//import com.alibaba.fastjson2.JSONObject;
//import lombok.Getter;
//import lombok.Setter;
//
///**
// * 自定义按钮内容，最多可以发送5行按钮，每一行最多5个按钮。
// */
//@Getter
//@Setter
//public class Markdown {
//    private final String type = "markdown";
//    private Data data = new Data();
//
//    public static Markdown Builder() {
//        return new Markdown();
//    }
//
//    /**
//     * @param content markdown语法
//     */
//    public Markdown setContent(String content) {
//        JSONObject contentObj = new JSONObject();
//        contentObj.put("content", content);
//        getData().setContent(contentObj.toJSONString());
//        return this;
//    }
//
//    @Getter
//    @Setter
//    public static class Data {
//        private String content = "";
//    }
//
//}
