package com.kuroneko.cqbot.exception;

import cn.hutool.core.util.StrUtil;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.*;
import lombok.extern.slf4j.Slf4j;

import java.util.Collection;
import java.util.function.Supplier;

@Slf4j
public class ExceptionHandler {
    public static void with(Bot bot, MessageEvent event, Supplier<?> block) {
        with(bot, event, block, null);
    }

    public static void with(Bot bot, MessageEvent event, Supplier<?> block, Supplier<?> f) {
        try {
            Object object = block.get();
            if (null == object) {
                return;
            }

            switch (object) {
                case String msg -> push(event, bot, msg);
                case Collection<?> msgList -> msgList.forEach(msg -> push(event, bot, msg.toString()));
                default -> {
                }
            }

        } catch (BotException e) {
            push(event, bot, e.getMessage());
        } catch (Exception e) {
            push(event, bot, "ERROR: " + e.getMessage());
            log.error(event.getMessage(), e);
        } finally {
            if (null != f) {
                f.get();
            }
        }
    }

    private static void push(MessageEvent event, Bot bot, String message) {
        if (StrUtil.isEmpty(message)) {
            return;
        }
        switch (event) {
            case AnyMessageEvent it -> bot.sendMsg(it, message, false);
            case GroupMessageEvent it -> bot.sendGroupMsg(it.getGroupId(), message, false);
            case PrivateMessageEvent it -> bot.sendPrivateMsg(it.getUserId(), message, false);
            case GuildMessageEvent it -> bot.sendGuildMsg(it.getGuildId(), it.getChannelId(), message);
            default -> {
            }
        }
    }

}
