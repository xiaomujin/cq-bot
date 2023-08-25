package com.kuroneko.cqbot.exception;

import cn.hutool.core.util.StrUtil;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.*;
import lombok.extern.slf4j.Slf4j;

import java.util.function.Supplier;

@Slf4j
public class ExceptionHandler {
    public static void with(Bot bot, MessageEvent event, Supplier<String> block) {
        try {
            String msg = block.get();
            if (StrUtil.isNotEmpty(msg)) {
                push(event, bot, msg);
            }
        } catch (BotException e) {
            if (StrUtil.isNotEmpty(e.getMessage())) {
                push(event, bot, e.getMessage());
            }
        } catch (Exception e) {
            push(event, bot, "ERROR: " + e.getMessage());
            log.error(event.getMessage(), e);
        }
    }

    private static void push(MessageEvent event, Bot bot, String message) {
        if (event instanceof AnyMessageEvent it) {
            bot.sendMsg(it, message, false);
        } else if (event instanceof GroupMessageEvent it) {
            bot.sendGroupMsg(it.getGroupId(), message, false);
        } else if (event instanceof PrivateMessageEvent it) {
            bot.sendPrivateMsg(it.getUserId(), message, false);
        }
//        switch (event) {
//            case AnyMessageEvent it -> bot.sendMsg(it, message, false);
//            case GroupMessageEvent it -> bot.sendGroupMsg(it.getGroupId(), message, false);
//            case PrivateMessageEvent it -> bot.sendPrivateMsg(it.getUserId(), message, false);
//            case GuildMessageEvent it -> bot.sendGuildMsg(it.getGuildId(), it.getChannelId(), message);
//            default -> {
//            }
//        }
    }

}
