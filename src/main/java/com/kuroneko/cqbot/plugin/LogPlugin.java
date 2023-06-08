package com.kuroneko.cqbot.plugin;

import com.mikuac.shiro.annotation.GroupMessageHandler;
import com.mikuac.shiro.annotation.GroupMsgDeleteNoticeHandler;
import com.mikuac.shiro.annotation.GuildMessageHandler;
import com.mikuac.shiro.annotation.PrivateMessageHandler;
import com.mikuac.shiro.annotation.common.Order;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.GuildMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import com.mikuac.shiro.dto.event.notice.GroupMsgDeleteNoticeEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;

/**
 * <a href="https://misakatat.github.io/shiro-docs/event.html">SDK文档</a>
 */
@Slf4j
@Shiro
@Component
public class LogPlugin {

    @Order(Integer.MIN_VALUE)
    @GroupMessageHandler
    public void groupMessageLog(Bot bot, GroupMessageEvent event, Matcher matcher) {
        String rawMessage = event.getRawMessage();
        log.info(rawMessage);
    }

    @Order(Integer.MIN_VALUE)
    @PrivateMessageHandler
    public void privateMessageLog(Bot bot, PrivateMessageEvent event, Matcher matcher) {
        String rawMessage = event.getRawMessage();
        log.info(rawMessage);
    }
    @Order(Integer.MIN_VALUE)
    @GuildMessageHandler
    public void guildMessageLog(Bot bot, GuildMessageEvent event, Matcher matcher){
        String rawMessage = event.getRawMessage();
        log.info(rawMessage);
    }
    @Order(Integer.MIN_VALUE)
    @GroupMsgDeleteNoticeHandler
    public void groupMsgDeleteNoticeLog(Bot bot, GroupMsgDeleteNoticeEvent event, Matcher matcher){
        log.info(event.toString());
    }
}
