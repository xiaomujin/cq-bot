package com.kuroneko.cqbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

@Data
public class SauceDTO {
    private Header header;
    private List<Result> results;

    @Data
    public static class Header {
        @JsonProperty("long_remaining")
        private Integer shortRemaining;
        @JsonProperty("short_remaining")
        private Integer longRemaining;
    }

    @Data
    public static class Result {
        private ResultData data;
        private ResultHeader header;
    }

    @Data
    public static class ResultData {
        @JsonProperty("ext_urls")
        private List<String> extUrls;
        @JsonProperty("member_id")
        private Long authorId;
        @JsonProperty("member_name")
        private String authorName;
        @JsonProperty("pixiv_id")
        private Long pixivId;
        @JsonProperty("title")
        private String title;
        @JsonProperty("source")
        private String source;
        @JsonProperty("eng_name")
        private String engName;
        @JsonProperty("jp_name")
        private String jpName;
        @JsonProperty("tweet_id")
        private String tweetId;
        @JsonProperty("twitter_user_id")
        private String twitterUserId;
        @JsonProperty("twitter_user_handle")
        private String twitterUserHandle;
    }

    @Data
    public static class ResultHeader {
        @JsonProperty("index_id")
        private Integer indexId;
        @JsonProperty("similarity")
        private String similarity;
        @JsonProperty("thumbnail")
        private String thumbnail;
    }
}