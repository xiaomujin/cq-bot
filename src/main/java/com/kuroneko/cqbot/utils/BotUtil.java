package com.kuroneko.cqbot.utils;

import cn.hutool.core.util.StrUtil;
import com.kuroneko.cqbot.config.ProjectConfig;
import com.kuroneko.cqbot.handler.ApplicationContextHandler;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.BotContainer;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.handler.ActionHandler;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
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
                .map(it -> it.getData().get("text"))
                .collect(Collectors.joining(join)).trim();
    }

    public static Optional<String> getOneParam(String cmd, String msg) {
        List<String> params = getParams(cmd, msg, 1);
        if (params.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(params.getFirst());
    }

    public static List<String> getParams(String cmd, String msg, int paramNum) {
        if (StrUtil.isBlank(msg)) {
            return List.of();
        }
        ArrayList<String> list = new ArrayList<>();
        if (msg.trim().length() > cmd.length()) {
            String mainMsg = msg.substring(cmd.length()).trim();
            String[] split = mainMsg.split("\\s+", paramNum);
            list.addAll(Arrays.asList(split));
        }
        log.info("{}:getParams:{}", cmd, list);
        return list;
    }

    public static List<String> getParams(String cmd, String msg) {
        return getParams(cmd, msg, 0);
    }

    public static void sendToGroupList(Collection<Number> groupList, String msgList) {
        sendToGroupList(groupList, Collections.singleton(msgList));
    }


    public static void sendToGroupList(Collection<Number> groupList, Collection<String> msgList) {
        BotContainer botContainer = ApplicationContextHandler.getBean(BotContainer.class);
        botContainer.robots.forEach((qq, bot) -> {
            groupList.forEach(group -> {
                msgList.forEach(msg -> {
                    bot.sendGroupMsg(group.longValue(), msg, false);
                    sleep(600);
                });
                sleep(1500);
            });
        });
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
        return OneBotMedia.builder().file(STR."\{ProjectConfig.url}\{method}?path=\{imgPath}").cache(cache);
    }

    public static OneBotMedia getLocalMedia(String imgPath) {
        return getLocalMedia(imgPath, false);
    }
}
