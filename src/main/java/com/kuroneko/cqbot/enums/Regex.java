package com.kuroneko.cqbot.enums;

/**
 * (?i) 忽略大小写
 */
public class Regex {
    public static final String SEARCH_BULLET = "^(?i)(查子弹|子弹数据)(?<name>.+)";
    public static final String TKF_TIME = "^(?i)塔科夫时间$";
    public static final String BILIBILI_BID = "(?s).*/(?<BVId>BV\\w+).*";
    public static final String BILIBILI_SHORT_URL = "(?s).*(?<sUrl>b23.tv/\\w+).*";
    public static final String SYS_UPDATE = "^自我更新$";
    public static final String BA_TOTAL_BATTLE = "^总力战$";
    public static final String BA_CALENDAR = "^(?i)ba日历$";
    public static final String BA_IMAGE_BATTLE = "^(?i)ba (?<text>.+)";

    public static final String LIFE_RESTART = "^(转世人生|活一回|转世|转生)$";

    public static final String CALENDAR = "^日历$";

    public static final String DAILY = "^日报$";
    public static final String TKF_SERVER_INFO = "^(?i)((塔科夫|tkf)?服务器(状态)?)$";
    public static final String BANGUMI_CALENDAR = "^(今日|每日|最新)番剧$";
    public static final String TKF_MARKET_SEARCH = "^跳蚤(?<text>.*)";
    public static final String WORD_CLOUD = "^(我的|本群)(今日|本周|本月|本年)词云$";
    public static final String DICE = "^(?i)(?<cmd>(\\.|。)r).+";
    public static final String UPDATE_TKF_TASK = "^更新任务$";

}
