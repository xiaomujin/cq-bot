package com.kuroneko.cqbot.annoPlugin;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.kuroneko.cqbot.config.module.LocalJavaTimeModule;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.enums.Regex;
import com.kuroneko.cqbot.service.BulletService;
import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.regex.Matcher;

@Shiro
@Slf4j
@Component
@AllArgsConstructor
public class BulletPlugin {
    private final BulletService bulletService;

    @AnyMessageHandler(cmd = Regex.SEARCH_BULLET)
    public void handler(Bot bot, AnyMessageEvent event, Matcher matcher) {
        String name = matcher.group("name").trim();

        String imgPath = Constant.BASE_IMG_PATH + "SEARCH_BULLET" + event.getSender().getUserId() + ".png";
        String screenshot = bulletService.screenshotBullet(name, imgPath);
        MsgUtils msg = MsgUtils.builder();
        if (StrUtil.isEmpty(screenshot)) {
            bot.sendMsg(event, msg.text("没有找到").text(name).build(), false);
            return;
        }
        msg.img("http://localhost:8081/getImage?path=" + imgPath);
        bot.sendMsg(event, msg.build(), false);
    }

    @AnyMessageHandler(cmd = Regex.TKF_TIME)
    public void tkvTime(Bot bot, AnyMessageEvent event, Matcher matcher) {
        LocalDateTime dateTime = LocalDateTimeUtil.of(Instant.now().toEpochMilli() * 7, ZoneId.of("Europe/Moscow"));
        LocalDateTime dateTime2 = LocalDateTimeUtil.of(Instant.now().toEpochMilli() * 7 - 43200000, ZoneId.of("Europe/Moscow"));
        String time = dateTime.toLocalTime().format(DateTimeFormatter.ofPattern(LocalJavaTimeModule.NORM_TIME_PATTERN));
        String time2 = dateTime2.toLocalTime().format(DateTimeFormatter.ofPattern(LocalJavaTimeModule.NORM_TIME_PATTERN));
        MsgUtils msg = MsgUtils.builder().text(time2 + Constant.XN).text(time);
        bot.sendMsg(event, msg.build(), false);
    }
}
