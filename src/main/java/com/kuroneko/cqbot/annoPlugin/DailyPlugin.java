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
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
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
            // https://api.j4u.ink/proxy/redirect/moyu/calendar/20250926.png
            String imgUrl = "https://api.52vmy.cn/api/wl/moyu";
//            String moyuStr = HttpUtil.get("https://api.vvhan.com/api/moyu?type=json");
//            JSONObject moyuObject = JSON.parseObject(moyuStr);
//            Boolean success = moyuObject.getBoolean("success");
//            if (!success) {
//                throw new BotException("获取日历失败");
//            }
//            String imgUrl = moyuObject.getString("url");
//            int lastIndexOf = imgUrl.lastIndexOf("https");
//            if (lastIndexOf != -1) {
//                imgUrl = imgUrl.substring(lastIndexOf);
//            }
            String imgPath = Constant.BASE_IMG_PATH + "Calendar.png";
            downloadFile(imgUrl, imgPath);
            return MsgUtils.builder().img(BotUtil.getLocalMedia(imgPath)).build();
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

            downloadFile(imgUrl, imgPath);

            return MsgUtils.builder().img(BotUtil.getLocalMedia(imgPath)).build();
        }));
    }

    private void downloadFile(String url, String path) {
        String[] command = {
                "curl",
                "-o", path,
                url
        };

        ProcessBuilder processBuilder = new ProcessBuilder(command);
        processBuilder.redirectErrorStream(true);
        BufferedReader reader = null;
        try {
            Process process = processBuilder.start();
            reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
            String line;
            while ((line = reader.readLine()) != null) {
                log.info(line);
            }
            process.waitFor(2, TimeUnit.MINUTES);
        } catch (IOException | InterruptedException e) {
            throw new BotException(e.getMessage());
        } finally {
            IOUtils.closeQuietly(reader);
        }
    }
}

