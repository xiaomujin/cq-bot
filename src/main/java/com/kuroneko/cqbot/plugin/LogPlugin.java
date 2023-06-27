package com.kuroneko.cqbot.plugin;

import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.action.common.ActionData;
import com.mikuac.shiro.dto.action.response.GroupInfoResp;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.dto.event.message.GuildMessageEvent;
import com.mikuac.shiro.dto.event.message.PrivateMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * <a href="https://misakatat.github.io/shiro-docs/event.html">SDK文档</a>
 */
@Slf4j
@Component
public class LogPlugin extends BotPlugin {

    @Override
    public int onPrivateMessage(Bot bot, PrivateMessageEvent event) {
        log.info("->私:{}({}):{}", event.getPrivateSender().getNickname(), event.getPrivateSender().getUserId(), event.getRawMessage());
        return MESSAGE_IGNORE;
    }

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        log.info("->群:({})内:{}({}):{}", event.getGroupId(), event.getSender().getNickname(), event.getSender().getUserId(), event.getRawMessage());
        return MESSAGE_IGNORE;
    }

    @Override
    public int onGuildMessage(Bot bot, GuildMessageEvent event) {
        return super.onGuildMessage(bot, event);
    }
}
