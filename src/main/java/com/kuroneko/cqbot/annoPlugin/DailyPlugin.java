package com.kuroneko.cqbot.annoPlugin;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.kuroneko.cqbot.enums.Regex;
import com.kuroneko.cqbot.exception.BotException;
import com.kuroneko.cqbot.exception.ExceptionHandler;
import com.kuroneko.cqbot.utils.CacheUtil;
import com.kuroneko.cqbot.utils.HttpUtil;
import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Collection;
import java.util.Collections;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

@Shiro
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyPlugin {

    @AnyMessageHandler(cmd = Regex.CALENDAR)
    public void calendar(Bot bot, AnyMessageEvent event, Matcher matcher) {
        ExceptionHandler.with(bot, event, () -> {
            Collection<String> cacheMsg = CacheUtil.getOrPut(Regex.CALENDAR, 20, TimeUnit.MINUTES, () -> {
                String moyuStr = HttpUtil.get("https://api.vvhan.com/api/moyu?type=json");
                JSONObject moyuObject = JSON.parseObject(moyuStr);
                boolean success = moyuObject.getBooleanValue("success", false);
                if (!success) {
                    throw new BotException("获取日历失败");
                }
                String url = moyuObject.getString("url");
                OneBotMedia media = new OneBotMedia().file(url).cache(false);
                String msg = MsgUtils.builder().img(media).build();
                return Collections.singleton(msg);
            });
            return cacheMsg.iterator().next();
        });
    }

    @AnyMessageHandler(cmd = Regex.DAILY)
    public void daily(Bot bot, AnyMessageEvent event, Matcher matcher) {
        ExceptionHandler.with(bot, event, () -> {
            Collection<String> cacheMsg = CacheUtil.getOrPut(Regex.CALENDAR, 30, TimeUnit.MINUTES, () -> {
                String zaobaoStr = HttpUtil.get("https://v2.alapi.cn/api/zaobao?format=json&token=eCKR3lL7uFtt9PIm");
                JSONObject zaobaoObject = JSON.parseObject(zaobaoStr);
                int code = zaobaoObject.getIntValue("code", 0);
                if (code != 200) {
                    throw new BotException("获取日报失败");
                }
                String imgUrl = zaobaoObject.getJSONObject("data").getString("image");
                OneBotMedia media = new OneBotMedia().file(imgUrl).cache(false);
                String msg = MsgUtils.builder().img(media).build();
                return Collections.singleton(msg);
            });
            return cacheMsg.iterator().next();
        });
    }
}

