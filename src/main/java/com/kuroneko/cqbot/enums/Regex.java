package com.kuroneko.cqbot.enums;

/**
 * (?i) 忽略大小写
 */
public class Regex {
    public static final String SEARCH_BULLET = "^(?i)查子弹(?<name>.+)";
    public static final String TKF_TIME = "^(?i)塔科夫时间$";
    public static final String BILIBILI_BID = ".*(?<BVId>BV\\w+).*";
    public static final String BILIBILI_SHORT_URL = ".*(?<sUrl>b23.tv/\\w+).*";
    public static final String SYS_UPDATE = "^自我更新$";
    public static final String BA_TOTAL_BATTLE = "^总力战$";
    public static final String BA_IMAGE_BATTLE = "^(?i)ba (?<text>.+)";
}
