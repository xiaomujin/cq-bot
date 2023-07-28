package com.kuroneko.cqbot.constant;

import com.kuroneko.cqbot.vo.BiliDynamicVo;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class Constant {
    public static String XN = "\n";
    public static String DAILY_URL = "https://api.vvhan.com/api/60s?type=json";
    public static String DAILY_URL_2 = "https://api.vvhan.com/api/60s";
    /**
     * <a href="http://www.alapi.cn/">age</a>
     */
    public static String ALAPI_TOKEN = "eCKR3lL7uFtt9PIm";
    public static String GET_FAIL = "获取失败";
    public static String GET_SUCCESS = "获取成功";
    public static String DAILY_KEY = "dailyVo";
    public static Map<String, Object> CONFIG_CACHE = new ConcurrentHashMap<>();
    public static String AGE_LIST_KEY = "ageListVo";
    public static String AGE_LIST_URL = "https://api.agefans.app/v2/home-list?update={1}&recommend={2}";
    public static String AGE_HOST_URL = "https://www.agemys.org";
    public static String BASE_IMG_PATH = "/opt/bot_img/";


    /**
     * 米哈游
     */
    //角色
    public static String MHY_ROLES_COOKIE_URL = "https://api-takumi.mihoyo.com/binding/api/getUserGameRolesByCookie?game_biz={1}";
    //首页
    public static String MHY_SIGN_REWARD_URL = "https://api-takumi.mihoyo.com/event/bbs_sign_reward/home?act_id={1}";
    //获取签到时长
    public static String MHY_SIGN_INFO_URL = "https://api-takumi.mihoyo.com/event/bbs_sign_reward/info?act_id={1}&region={2}&uid={3}";
    public static String MHY_SIGN_URL = "https://api-takumi.mihoyo.com/event/bbs_sign_reward/sign";
    public static String MHY_YS_ACT_ID = "e202009291139501";
    public static String MHY_YS_SIGN_SALT = "N50pqm7FSy2AkFz2B3TqtuZMJ5TOl3Ep";
    public static String MHY_YS_SALT = "xV8v4Qu54lUKrEYFZkJhB8cuOh9Asafs";


    /**
     * Se图 regular small    &proxy=proxy.pixivel.moe
     */
    public static String SE_TU_URL = "https://api.lolicon.app/setu/v2?size=regular&tag={1}";
    /**
     * &web=true 小图
     */
    public static String SE_TU_PID_URL = "https://px.s.rainchan.win/img/regular/IMGID";

    /**
     * <a href="https://bangumi.moe">...</a>
     */
    public static String BAN_GU_MI_KEY = "banGuMiMoeVo";
    public static String BAN_GU_MI_URL = "https://bangumi.moe/rss/tags/600c009432f14c00073a9f49";


    /**
     * <a href="https://ovooa.caonm.net/">...</a>
     */
    public static String OVO_OA_PA_URL = "https://ovooa.caonm.net/API/pa/api.php?QQ=";
    public static String OVO_OA_TUI_URL = "https://ovooa.caonm.net/API/meizi/api.php?type=image";
    public static String OVO_OA_TGRJ_URL = "https://ovooa.caonm.net/API/tgrj/api.php";
    /**
     *
     */
    public static String ZERO_MAGNET_URL = "https://0magnet.com/search?q=";

    /**
     * 摸鱼日历
     * <a href="https://api.vvhan.com/">...</a>
     */
    public static String MO_YU_RI_LI_URL = "https://api.vvhan.com/api/moyu?type=json";
    public static String MO_YU_RI_LI_KEY = "RiLiVoUrl";

    /**
     * 塔科夫
     * <a href="https://tarkov-market.com">跳蚤市场</a>
     */
    public static String TAR_KOV_MARKET_URL = "https://api.tarkov-market.app/api/items?lang=cn&search={1}&tag=&sort=name&sort_direction=desc&trader=&skip=0&limit=3";

    public static Map<Long, Long> TAR_KOV_MARKET_CD = new ConcurrentHashMap<>();

    public static Map<Long, Long> AI_ROOM = new ConcurrentHashMap<>();


    /**
     * 塔科夫地图、彩蛋
     * 海关    2  Customs
     * 工厂    1  Factory
     * 森林    2  Woods
     * 海岸线  2  Shoreline
     * 实验室  1  The Lab
     * 储备站  1  Reserve
     * 立交桥  2  Interchange
     * 灯塔    2  Lighthouse
     */
    public static String TKV_MAP_CUSTOMS = "";
    public static String TKV_EGG_CUSTOMS = "";

    public static String TKV_MAP_FACTORY = "";

    public static String TKV_MAP_WOODS = "";
    public static String TKV_EGG_WOODS = "";

    public static String TKV_MAP_SHORELINE = "";
    public static String TKV_EGG_SHORELINE = "";

    public static String TKV_MAP_THE_LAB = "";

    public static String TKV_MAP_RESERVE = "";

    public static String TKV_MAP_INTERCHANGE = "";
    public static String TKV_EGG_INTERCHANGE = "";

    public static String TKV_MAP_LIGHTHOUSE = "";
    public static String TKV_EGG_LIGHTHOUSE = "";

    /**
     * bili 动态api
     */
    public static String BL_DYNAMIC_URL = "https://api.vc.bilibili.com/dynamic_svr/v1/dynamic_svr/space_history?host_uid={1}";

    public static String TKF_SERVER_INFO = "TkfServerInfo";

    public static Map<String, BiliDynamicVo.BiliDynamicCard> BILI_DYNAMIC = new ConcurrentHashMap<>();
}
