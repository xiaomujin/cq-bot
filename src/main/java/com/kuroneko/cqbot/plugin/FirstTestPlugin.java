package com.kuroneko.cqbot.plugin;

import com.mikuac.shiro.annotation.GroupMessageHandler;
import com.mikuac.shiro.annotation.PrivateMessageHandler;
import com.mikuac.shiro.annotation.common.Order;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;

@Slf4j
@Shiro
@Component
public class FirstTestPlugin {

    @Order(1)
    @PrivateMessageHandler
    public void privateMessageLog(Bot bot, PrivateMessageEvent event, Matcher matcher) {
        bot.sendPrivateMsg(event.getUserId(), event.getMessage(), false);
    }

    @Order(1)
    @GroupMessageHandler(at = AtEnum.NEED)
    public void GroupMessageHandler(Bot bot, GroupMessageEvent event, Matcher matcher) {
        bot.sendGroupMsg(event.getGroupId(), event.getMessage(), false);
    }
}
