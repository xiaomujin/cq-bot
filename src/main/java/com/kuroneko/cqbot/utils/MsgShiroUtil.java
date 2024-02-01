package com.kuroneko.cqbot.utils;

import cn.hutool.core.util.StrUtil;
import com.kuroneko.cqbot.handler.ApplicationContextHandler;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.BotContainer;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.extern.slf4j.Slf4j;

import java.util.*;
import java.util.stream.Collectors;

@Slf4j
public class MsgShiroUtil {
    private MsgShiroUtil() {
    }

    public static boolean isAtMe(List<ArrayMsg> arrayMsg, long id) {
        return MsgShiroUtil.getAtList(arrayMsg).contains(id);
    }

    public static boolean isReplyMe(List<ArrayMsg> arrayMsg, long id) {
        return MsgTypeEnum.reply.equals(arrayMsg.get(0).getType())
                && MsgTypeEnum.at.equals(arrayMsg.get(1).getType())
                && Long.parseLong((String) arrayMsg.get(1).getData().get("qq")) == id;
    }

    public static int getReplyId(List<ArrayMsg> arrayMsg) {
        return Integer.parseInt(arrayMsg.get(0).getData().get("id"));
    }

    public static List<Long> getAtList(List<ArrayMsg> arrayMsg) {
        return ShiroUtils.getAtList(arrayMsg);
    }

    public static String getText(List<ArrayMsg> arrayMsg) {
        return MsgShiroUtil.getText(arrayMsg, ",").trim();
    }

    public static String getText(List<ArrayMsg> arrayMsg, String join) {
        return arrayMsg.stream()
                .filter(it -> MsgTypeEnum.text == it.getType())
                .map(it -> it.getData().get("text"))
                .collect(Collectors.joining(join));
    }

    public static Optional<String> getOneParam(String cmd, String msg) {
        List<String> params = getParams(cmd, msg, 1);
        if (params.isEmpty()) {
            return Optional.empty();
        }
        return Optional.of(params.get(0));
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
        botContainer.robots.forEach((_, bot) -> {
            groupList.forEach(group -> msgList.forEach(msg -> bot.sendGroupMsg(group.longValue(), msg, false)));
            try {
                Thread.sleep(2600);
            } catch (InterruptedException e) {
                log.error("sleep err", e);
            }
        });
    }
}
