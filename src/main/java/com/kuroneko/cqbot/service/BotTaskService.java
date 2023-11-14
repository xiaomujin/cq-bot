package com.kuroneko.cqbot.service;

import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.constant.RedisKey;
import com.kuroneko.cqbot.event.BiliSubscribeEvent;
import com.kuroneko.cqbot.handler.ApplicationContextHandler;
import com.kuroneko.cqbot.utils.RedisUtil;
import com.kuroneko.cqbot.vo.*;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import com.mikuac.shiro.dto.action.common.ActionList;
import com.mikuac.shiro.dto.action.response.GroupInfoResp;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

@Slf4j
@RequiredArgsConstructor
@Service
public class BotTaskService {

    private final RestTemplate restTemplate;
    private final BotContainer botContainer;
    private final BiLiService biLiService;
    private final RedisUtil redisUtil;

    public void doDaily() {
        DailyVo dailyVo = (DailyVo) Constant.CONFIG_CACHE.get(Constant.DAILY_KEY);
        MsgUtils msg = MsgUtils.builder().img(dailyVo.getImgUrl());

        Map<Long, Bot> bots = botContainer.robots;
        bots.forEach((id, bot) -> {
            ActionList<GroupInfoResp> groupList = bot.getGroupList();
            if (groupList != null) {
                groupList.getData().forEach(group -> {
                    bot.sendGroupMsg(group.getGroupId(), msg.build(), false);
                    try {
                        Thread.sleep(3600);
                    } catch (InterruptedException e) {
                        log.error("sleep err", e);
                    }
                });
            }
        });

    }

    public DailyVo getDailyVo() {
        return restTemplate.getForObject(Constant.DAILY_URL, DailyVo.class, Constant.ALAPI_TOKEN);
    }

    public RiLiVo getRiLiVo() {
        return restTemplate.getForObject(Constant.MO_YU_RI_LI_URL, RiLiVo.class);
    }

    public ResponseEntity<AgeListVo> getAgeListVo() {
        return restTemplate.getForEntity(Constant.AGE_LIST_URL, AgeListVo.class, 1, 1);
    }

    public void refreshDaily() {
        DailyVo dailyVo = getDailyVo();
        if (dailyVo == null || !dailyVo.getCode().equals(200)) {
            log.error("刷新日报图片 失败");
            return;
        }
        dailyVo.setImgUrl(dailyVo.getData().getImage() + "?t=" + System.currentTimeMillis());
        Constant.CONFIG_CACHE.put(Constant.DAILY_KEY, dailyVo);
        log.info("刷新日报图片 成功");
    }

    public void refreshRiLi() {
        RiLiVo riLiVo = getRiLiVo();
        if (riLiVo == null || !riLiVo.isSuccess()) {
            log.error("刷新日历图片 失败");
            return;
        }
        Constant.CONFIG_CACHE.put(Constant.MO_YU_RI_LI_KEY, riLiVo.getUrl());
        log.info("刷新日历图片 成功");
    }

    public void refreshAgeListVo() {
        ResponseEntity<AgeListVo> ageListVo = getAgeListVo();
        if (!ageListVo.getStatusCode().is2xxSuccessful()) {
            log.error("刷新age番剧 失败");
            return;
        }
        Constant.CONFIG_CACHE.put(Constant.AGE_LIST_KEY, ageListVo.getBody());
        log.info("刷新age番剧 成功");
    }


    public void refreshThreeDog() {
        try {
            ThreeDog threeDog = restTemplate.getForObject("https://t0t.co/proxy/congested-valleygirl-9254455.herokuapp.com/goonDetectors/current", ThreeDog.class);
            if (threeDog != null) {
                String lastReported = threeDog.getLastReported();
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSX");
//设置时区UTC
                df.setTimeZone(TimeZone.getTimeZone("UTC"));
//格式化，转当地时区时间
                Date after = df.parse(lastReported);
                df.applyPattern("yyyy-MM-dd HH:mm:ss");
//默认时区
                df.setTimeZone(TimeZone.getDefault());
                threeDog.setLastReported(df.format(after));

                ThreeDog dog = (ThreeDog) Constant.CONFIG_CACHE.get(RedisKey.THREE_DOG);
                Constant.CONFIG_CACHE.put(RedisKey.THREE_DOG, threeDog);
                //推送
                if (dog != null && !dog.getLocation().equals(threeDog.getLocation())) {
                    pushMsg(dog, threeDog);
                }

                log.info("刷新三兄弟在哪 成功");
            }
        } catch (ParseException e) {
            log.error("刷新三兄弟在哪 失败", e);
        }
    }

    public void refreshTKFDT() {
        String uid = "152065343";
        try {
            String dynamicId = biLiService.getNew(uid);
            String oldDynamicId = (String) Constant.CONFIG_CACHE.get(RedisKey.TKF_DT);
            Constant.CONFIG_CACHE.put(RedisKey.TKF_DT, dynamicId);
            //推送
            if (oldDynamicId != null && !dynamicId.equals(oldDynamicId)) {
                pushTKFDTMsg(dynamicId, uid);
            }
            log.info("刷新" + uid + "动态 成功");
        } catch (Exception e) {
            log.error("刷新" + uid + "动态 失败", e);
        }
    }

    public void pushTKFDTMsg(String dynamicId, String uid) {
        String path = biLiService.getNewScreenshot(dynamicId, uid);
        Set<Number> list = redisUtil.members(RedisKey.TKF_DT);
        log.info("TKF_DT 推送群聊：{}", list);
        if (!list.isEmpty()) {
            botContainer.robots.forEach((id, bot) -> {
                MsgUtils msg = biLiService.buildDynamicMsg(path, dynamicId);
                list.forEach(groupId -> {
                    bot.sendGroupMsg(groupId.longValue(), msg.build(), false);
                    try {
                        Thread.sleep(3600);
                    } catch (InterruptedException e) {
                        log.error("sleep err", e);
                    }
                });
            });

        }
    }

    public void pushMsg(ThreeDog oldThreeDog, ThreeDog threeDog) {
        Set<Number> list = redisUtil.members(RedisKey.THREE_DOG);
        log.info("THREE_DOG 推送群聊：{}", list);
        if (!list.isEmpty()) {
            botContainer.robots.forEach((id, bot) -> {
                MsgUtils msg = MsgUtils.builder().text("三兄弟位置变更" + Constant.XN).text(oldThreeDog.getLocation() + " -> " + threeDog.getLocation() + Constant.XN).text(threeDog.getLastReported());
                list.forEach(groupId -> {
                    bot.sendGroupMsg(groupId.longValue(), msg.build(), false);
                    try {
                        Thread.sleep(3600);
                    } catch (InterruptedException e) {
                        log.error("sleep err", e);
                    }
                });
            });

        }
    }

    public void refreshBiliSubscribe(boolean initSend) {
        Collection<String> allKeys = redisUtil.getAllKeys(RedisKey.BILI_SUB);
        allKeys.forEach(uid -> {
            Optional<BiliDynamicVo.BiliDynamicCard> firstCard = biLiService.getFirstCard(uid);
            if (firstCard.isPresent()) {
                BiliDynamicVo.BiliDynamicCard dynamicCard = firstCard.get();
                BiliDynamicVo.BiliDynamicCard card = Constant.BILI_DYNAMIC.get(uid);
                Constant.BILI_DYNAMIC.put(uid, dynamicCard);
                if (!initSend) {
                    if (card == null || !card.getDesc().getDynamic_id_str().equals(dynamicCard.getDesc().getDynamic_id_str())) {
                        BiliSubscribeEvent event = new BiliSubscribeEvent(dynamicCard);
                        ApplicationContextHandler.publishEvent(event);
                    }
                }

            }
            try {
                Thread.sleep(3600);
            } catch (InterruptedException e) {
                log.error("sleep err", e);
            }
        });
    }
}
