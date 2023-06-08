package com.kuroneko.cqbot.plugin;

import com.mikuac.shiro.annotation.PrivateMessageHandler;
import com.mikuac.shiro.annotation.common.Order;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
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
        String rawMessage = event.getRawMessage();
        String msg = MsgUtils.builder().text(event.getMessage()).build();
        bot.sendPrivateMsg(event.getUserId(), msg, false);
    }
}
