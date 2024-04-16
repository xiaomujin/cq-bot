package com.kuroneko.cqbot.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.List;

@Data
public class SauceDTO {
    private Header header;
    private List<Result> results;

    @Data
    public static class Header {
        @JSONField(name = "long_remaining")
        private Integer shortRemaining;
        @JSONField(name = "short_remaining")
        private Integer longRemaining;
    }

    @Data
    public static class Result {
        private ResultData data;
        private ResultHeader header;
    }

    @Data
    public static class ResultData {
        @JSONField(name = "ext_urls")
        private List<String> extUrls;
        @JSONField(name = "member_id")
        private Long authorId;
        @JSONField(name = "member_name")
        private String authorName;
        @JSONField(name = "pixiv_id")
        private Long pixivId;
        @JSONField(name = "title")
        private String title;
        @JSONField(name = "source")
        private String source;
        @JSONField(name = "eng_name")
        private String engName;
        @JSONField(name = "jp_name")
        private String jpName;
        @JSONField(name = "tweet_id")
        private String tweetId;
        @JSONField(name = "twitter_user_id")
        private String twitterUserId;
        @JSONField(name = "twitter_user_handle")
        private String twitterUserHandle;
    }

    @Data
    public static class ResultHeader {
        @JSONField(name = "index_id")
        private Integer indexId;
        @JSONField(name = "similarity")
        private String similarity;
        @JSONField(name = "thumbnail")
        private String thumbnail;
    }
}
