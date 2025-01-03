package com.kuroneko.cqbot.constant;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;

public class CmdConst {

    /**
     * 帮助
     */
    public static String HELP = "帮助";
    /**
     * 日报
     */
    public static String RI_BAO = "日报";
    /**
     * 日历
     */
    public static String RI_LI = "日历";
    /**
     * 今日番剧
     */
    public static String TODAY_FANJU = "今日番剧";
    /**
     * 原神签到
     */
    public static String YS_SIGN = "签到";
    /**
     * 色图
     */
    public static String SE_TU = "涩图";
    /**
     * 高清色图
     */
    public static String HIGH_SE_TU = "高清涩图";
    /**
     * 原神绑定
     */
    public static String BANG_DING = "绑定";
    /**
     * 爬
     */
    public static String PA = "爬";
    public static String TGRJ = "舔狗日记";
    /**
     * 来份腿
     */
    public static String TUI = "来份腿";
    public static String TAO = "买家秀";
    /**
     * 车牌
     */
    public static String CHE_PAI = "车牌";
    /**
     * 逃离塔科夫
     */
    public static String TIAO_ZAO = "跳蚤";
    public static String RAINBOW_KD = "r6战绩";
    public static String THREE_HUNDRED_KD = "300战绩";
    public static String MAP = "地图";
    public static String OPEN = "订阅";
    public static String CLOSE = "退订";
    public static String ZI_DAN = "子弹数据";
    public static String UPDATE_ZI_DAN = "更新子弹数据";
    public static String BILI_SUBSCRIBE = "哔哩订阅";
    public static String BILI_SUBSCRIBE_CANCEL = "哔哩退订";
    public static String BILI_DYNAMICS = "哔哩动态";
    public static String RECORD_SAY = ".说";
    /**
     * 提问ai
     */
    public static String TIWAN_AI = "提问";

    public static List<String> getAllCmd() {
        ArrayList<String> cmd = new ArrayList<>();
        for (Field field : CmdConst.class.getFields()) {
            try {
                cmd.add((String) field.get(null));
            } catch (IllegalAccessException e) {

            }
        }
        return cmd;
    }
}
