package com.kuroneko.cqbot.utils;

import cn.hutool.core.util.StrUtil;
import com.kuroneko.cqbot.config.ProjectConfig;
import com.kuroneko.cqbot.handler.ApplicationContextHandler;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import com.mikuac.shiro.dto.action.common.ActionData;
import com.mikuac.shiro.dto.action.common.MsgId;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.handler.ActionHandler;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Consumer;
import java.util.stream.Collectors;

@Slf4j
@Component
public class BotUtil {
    public static ActionHandler actionHandler;

    private BotUtil(ActionHandler actionHandler) {
        BotUtil.actionHandler = actionHandler;
    }

    public static boolean isAtMe(List<ArrayMsg> arrayMsg, long id) {
        return getAtList(arrayMsg).contains(id);
    }

    public static List<Long> getAtList(List<ArrayMsg> arrayMsg) {
        return ShiroUtils.getAtList(arrayMsg);
    }

    public static String getText(List<ArrayMsg> arrayMsg) {
        return getText(arrayMsg, ",");
    }

    public static String getText(List<ArrayMsg> arrayMsg, String join) {
        return arrayMsg.stream()
                .filter(it -> MsgTypeEnum.text == it.getType())
                .map(it -> it.getStringData("text"))
                .collect(Collectors.joining(join)).trim();
    }

    public static Optional<String> getOneParam(String cmd, String msg) {
        List<String> params = getParams(cmd, msg, 1);
        if (params.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(params.getFirst());
    }

    public static Optional<String> getOneParam(String msg) {
        List<String> params = getParams(null, msg, 1);
        if (params.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(params.getFirst());
    }

    public static List<String> getParams(String cmd, String msg, int paramNum) {
        if (StrUtil.isBlank(msg)) {
            return List.of();
        }
        if (Objects.isNull(cmd)) {
            return getStrings(msg.trim(), paramNum);
        } else if (msg.trim().length() > cmd.length()) {
            String mainMsg = msg.substring(cmd.length()).trim();
            return getStrings(mainMsg, paramNum);
        }
        return List.of();
    }

    private static ArrayList<String> getStrings(String msg, int paramNum) {
        String[] split = msg.split("\\s+", paramNum);
        return new ArrayList<>(Arrays.asList(split));
    }

    public static List<String> getParams(String cmd, String msg) {
        return getParams(cmd, msg, 0);
    }

    public static List<String> getParams(String msg) {
        return getParams(null, msg, 0);
    }

    public static void sendToGroupList(Bot bot, List<Long> groupList, String msg) {
        sendToGroupList(bot, groupList, Collections.singletonList(msg));
    }

    public static void sendToGroupList(Bot bot, List<Long> groupList, List<String> msgList) {
        groupList.forEach(group -> {
            sendToGroup(bot, group, msgList);
            sleep(2600);
        });
    }

    public static void sendToGroup(Bot bot, Long groupId, List<String> msgList) {
        switch (msgList.size()) {
            case 0 -> {
            }
            case 1 -> bot.sendGroupMsg(groupId, msgList.getFirst(), false);
            default -> msgList.forEach(msg -> {
                bot.sendGroupMsg(groupId, msg, false);
                sleep(1600);
            });
        }
    }

    public static void sendToGroupList(List<Long> groupList, List<String> msgList) {
        BotContainer botContainer = ApplicationContextHandler.getBean(BotContainer.class);
        botContainer.robots.forEach((qq, bot) -> groupList.forEach(group -> {
            msgList.forEach(msg -> {
                bot.sendGroupMsg(group, msg, false);
                sleep(1600);
            });
            sleep(2600);
        }));
    }

    public static void sleep(long millis) {
        try {
            Thread.sleep(millis);
        } catch (InterruptedException e) {
            log.error("sleep err", e);
        }
    }

    public static OneBotMedia getLocalMedia(String imgPath, boolean cache) {
        String method;
        if (imgPath.endsWith("png")) {
            method = "getImage";
        } else {
            method = "getJpgImage";
        }
        return OneBotMedia.builder().file(getLocalHost() + method + "?path=" + imgPath).cache(cache);
    }

    public static OneBotMedia getLocalMedia(String imgPath) {
        return getLocalMedia(imgPath, false);
    }

    public static String getLocalHost() {
        return ProjectConfig.url;
    }

    @SuppressWarnings("unchecked")
    public static <T> T cast(Object object) {
        return (T) object;
    }

    public static ActionData<MsgId> at(Bot bot, Long userId, Long groupId, String msg) {
        if (groupId != null && groupId != 0L) {
            return bot.sendGroupMsg(
                    groupId,
                    MsgUtils.builder().at(userId).text(" ").text(msg).build(),
                    false
            );
        }

        return bot.sendPrivateMsg(userId, msg, false);
    }

    public static void closeQuietly(final AutoCloseable autoCloseable) {
        closeQuietly(autoCloseable, null);
    }

    public static void closeQuietly(final AutoCloseable autoCloseable, final Consumer<Exception> consumer) {
        if (autoCloseable != null) {
            try {
                autoCloseable.close();
            } catch (final Exception e) {
                if (consumer != null) {
                    consumer.accept(e);
                }
            }
        }
    }

}
