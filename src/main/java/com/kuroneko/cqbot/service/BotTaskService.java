package com.kuroneko.cqbot.service;

import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.constant.RedisKey;
import com.kuroneko.cqbot.core.cfg.ConfigManager;
import com.kuroneko.cqbot.event.BiliSubscribeEvent;
import com.kuroneko.cqbot.handler.ApplicationContextHandler;
import com.kuroneko.cqbot.utils.BotUtil;
import com.kuroneko.cqbot.vo.BiliDynamicVo;
import com.kuroneko.cqbot.vo.ThreeDog;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class BotTaskService {

    private final BiLiService biLiService;


//    public DailyVo getDailyVo() {
//        return restTemplate.getForObject(Constant.DAILY_URL, DailyVo.class, Constant.ALAPI_TOKEN);
//    }

//    public RiLiVo getRiLiVo() {
//        return restTemplate.getForObject(Constant.MO_YU_RI_LI_URL, RiLiVo.class);
//    }

//    public ResponseEntity<AgeListVo> getAgeListVo() {
//        return restTemplate.getForEntity(Constant.AGE_LIST_URL, AgeListVo.class, 1, 1);
//    }

//    public void refreshDaily() {
//        DailyVo dailyVo = getDailyVo();
//        if (dailyVo == null || !dailyVo.getCode().equals(200)) {
//            log.error("刷新日报图片 失败");
//            return;
//        }
//        dailyVo.setImgUrl(dailyVo.getData().getImage() + "?t=" + System.currentTimeMillis());
//        Constant.CONFIG_CACHE.put(Constant.DAILY_KEY, dailyVo);
//        log.info("刷新日报图片 成功");
//    }

//    public void refreshRiLi() {
//        RiLiVo riLiVo = getRiLiVo();
//        if (riLiVo == null || !riLiVo.isSuccess()) {
//            log.error("刷新日历图片 失败");
//            return;
//        }
//        Constant.CONFIG_CACHE.put(Constant.MO_YU_RI_LI_KEY, riLiVo.getUrl());
//        log.info("刷新日历图片 成功");
//    }

//    public void refreshAgeListVo() {
//        ResponseEntity<AgeListVo> ageListVo = getAgeListVo();
//        if (!ageListVo.getStatusCode().is2xxSuccessful()) {
//            log.error("刷新age番剧 失败");
//            return;
//        }
//        Constant.CONFIG_CACHE.put(Constant.AGE_LIST_KEY, ageListVo.getBody());
//        log.info("刷新age番剧 成功");
//    }


    public void refreshThreeDog() {
        try {
            Document document;
            document = Jsoup.connect("https://9d33d34f.goon-98b.pages.dev/proxy/https://docs.google.com/spreadsheets/d/e/2PACX-1vR-wIQI351UH85ILq5KiCLMMrl0uHRmjDinBCt6nXGg5exeuCxQUf8DTLJkwn7Ckr8-HmLyEIoapBE5/pubhtml/sheet?headers=false&gid=1420050773").get();
            Elements td = document.getElementsByTag("td");
            String time = td.get(2).text();
            String mapName = td.get(3).text();

            SimpleDateFormat df = new SimpleDateFormat("M/dd/yyyy H:mm:ss");
            df.setTimeZone(TimeZone.getTimeZone("America/New_York"));
            Date after = df.parse(time);
            df.applyPattern("yyyy-MM-dd HH:mm:ss");
            df.setTimeZone(TimeZone.getDefault());
            String format = df.format(after);
            ThreeDog threeDog = new ThreeDog();
            threeDog.setLocation(mapName);
            threeDog.setLastReported(format);

            ThreeDog dog = (ThreeDog) Constant.CONFIG_CACHE.get(RedisKey.THREE_DOG);
            Constant.CONFIG_CACHE.put(RedisKey.THREE_DOG, threeDog);
            //推送
            if (dog != null && !dog.getLocation().equals(threeDog.getLocation())) {
//                pushMsg(dog, threeDog);
            }

            log.info("刷新三兄弟在哪 成功");
        } catch (ParseException | IOException e) {
            log.error("刷新三兄弟在哪 失败", e);
        }
    }


//    public void pushMsg(ThreeDog oldThreeDog, ThreeDog threeDog) {
//        Set<Number> list = redisUtil.members(RedisKey.THREE_DOG);
//        log.info("THREE_DOG 推送群聊：{}", list);
//        if (!list.isEmpty()) {
//            MsgUtils msg = MsgUtils.builder().text("三兄弟位置变更" + Constant.XN).text(oldThreeDog.getLocationCN() + " -> " + threeDog.getLocationCN() + Constant.XN).text(threeDog.getLastReported());
//            BotUtil.sendToGroupList(list, msg.build());
//        }
//    }

    @Synchronized
    public void refreshBiliSubscribe(boolean initSend) {
        Set<String> allKeys = ConfigManager.ins.getBiliCfg().getAllSubUid();
//        Collection<String> allKeys = redisUtil.getAllKeys(RedisKey.BILI_SUB);
        allKeys.forEach(uid -> {
            Optional<BiliDynamicVo.BiliDynamicCard> firstCard = biLiService.getFirstCard(uid);
            if (firstCard.isPresent()) {
                BiliDynamicVo.BiliDynamicCard dynamicCard = firstCard.get();
                BiliDynamicVo.BiliDynamicCard card = Constant.BILI_DYNAMIC.get(uid);
                Constant.BILI_DYNAMIC.put(uid, dynamicCard);
                if (!initSend) {
                    if (card == null || !card.getId_str().equals(dynamicCard.getId_str())) {
                        BiliSubscribeEvent event = new BiliSubscribeEvent(dynamicCard);
                        ApplicationContextHandler.publishEvent(event);
                    }
                }
            }
            BotUtil.sleep(3600);
        });
    }
}
