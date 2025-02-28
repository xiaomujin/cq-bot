package com.kuroneko.cqbot.annoPlugin;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.enums.Regex;
import com.kuroneko.cqbot.exception.BotException;
import com.kuroneko.cqbot.exception.ExceptionHandler;
import com.kuroneko.cqbot.utils.BotUtil;
import com.kuroneko.cqbot.utils.CacheUtil;
import com.kuroneko.cqbot.utils.HttpUtil;
import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.springframework.stereotype.Component;

import java.io.*;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

@Shiro
@Slf4j
@Component
@RequiredArgsConstructor
public class DailyPlugin {

    @AnyMessageHandler()
    @MessageHandlerFilter(cmd = Regex.CALENDAR)
    public void calendar(Bot bot, AnyMessageEvent event, Matcher matcher) {
        ExceptionHandler.with(bot, event, () -> CacheUtil.getOrPut(Regex.CALENDAR, 20, TimeUnit.MINUTES, () -> {
//            String moyuStr = HttpUtil.get("https://api.j4u.ink/v1/store/other/proxy/remote/moyu.json");
            String moyuStr = HttpUtil.get("https://api.vvhan.com/api/moyu?type=json");
            JSONObject moyuObject = JSON.parseObject(moyuStr);
            Boolean success = moyuObject.getBoolean("success");
            if (!success) {
                throw new BotException("获取日历失败");
            }
            String url = moyuObject.getString("url");
            OneBotMedia media = new OneBotMedia().file(url).cache(true);
            return MsgUtils.builder().img(media).build();
        }));
    }

    @AnyMessageHandler()
    @MessageHandlerFilter(cmd = Regex.DAILY)
    public void daily(Bot bot, AnyMessageEvent event, Matcher matcher) {
        ExceptionHandler.with(bot, event, () -> CacheUtil.getOrPut(Regex.DAILY, 30, TimeUnit.MINUTES, () -> {
            String zaobaoStr = HttpUtil.get("https://v2.alapi.cn/api/zaobao?format=json&token=eCKR3lL7uFtt9PIm");
            JSONObject zaobaoObject = JSON.parseObject(zaobaoStr);
            int code = zaobaoObject.getIntValue("code", 0);
            if (code != 200) {
                throw new BotException("获取日报失败");
            }
            String imgUrl = zaobaoObject.getJSONObject("data").getString("image");
            String imgPath = Constant.BASE_IMG_PATH + "Daily.png";

            String[] command = {
                    "curl",
                    "-o", imgPath,
                    imgUrl
            };

            ProcessBuilder processBuilder = new ProcessBuilder(command);
            processBuilder.redirectErrorStream(true);

            try {
                Process process = processBuilder.start();
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    log.info(line);
                }
                process.waitFor(2, TimeUnit.MINUTES);
            } catch (IOException | InterruptedException e) {
                throw new BotException(e.getMessage());
            }

            return MsgUtils.builder().img(BotUtil.getLocalMedia(imgPath)).build();
        }));
    }
}

