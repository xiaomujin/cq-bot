package com.kuroneko.cqbot.utils;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.kuroneko.cqbot.config.ProjectConfig;
import com.kuroneko.cqbot.handler.ApplicationContextHandler;
import com.kuroneko.cqbot.lagrange.markdown.Keyboard;
import com.kuroneko.cqbot.lagrange.markdown.Markdown;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.constant.ActionParams;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotContainer;
import com.mikuac.shiro.dto.action.common.ActionData;
import com.mikuac.shiro.dto.action.common.MsgId;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.ActionPathEnum;
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
        botContainer.robots.forEach((qq, bot) -> groupList.forEach(group -> {
            msgList.forEach(msg -> {
                bot.sendGroupMsg(group.longValue(), msg, false);
                sleep(600);
            });
            sleep(1500);
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
        return OneBotMedia.builder().file(STR."\{ProjectConfig.url}\{method}?path=\{imgPath}").cache(cache);
    }

    public static OneBotMedia getLocalMedia(String imgPath) {
        return getLocalMedia(imgPath, false);
    }


    public static List<Map<String, Object>> generateForwardMsg(String uin, String name, List<Object> contents) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        Map<String, Object> node = new HashMap<>();
        node.put("type", "node");
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("uin", uin);
        data.put("content", contents);
        node.put("data", data);
        nodes.add(node);
        return nodes;
    }

    public static ActionData<String> sendForwardMsg(Bot bot, List<Map<String, Object>> msg) {
        JSONObject params = new JSONObject();
        params.put(ActionParams.MESSAGES, msg);
        JSONObject result = BotUtil.actionHandler.action(bot.getSession(), ActionPathEnum.SEND_FORWARD_MSG, params);
        return result != null ? result.to(new TypeReference<ActionData<String>>() {
        }.getType()) : null;
    }

    public static ActionData<MsgId> sendMarkdownMsg(Bot bot, AnyMessageEvent event, String mdText) {
        return sendMarkdownMsg(bot, event, mdText, null);
    }

    public static ActionData<MsgId> sendMarkdownMsg(Bot bot, AnyMessageEvent event, String mdText, Keyboard keyboard) {
        List<Object> contents = new ArrayList<>();
        Markdown markdown = Markdown.Builder().setContent(mdText);
        contents.add(markdown);
        if (keyboard != null) {
            contents.add(keyboard);
        }
        List<Map<String, Object>> maps = generateForwardMsg("100000", "小助手", contents);
        ActionData<String> actionData = sendForwardMsg(bot, maps);
        if (actionData == null || StrUtil.isEmpty(actionData.getData())) {
            return null;
        }
        return bot.sendMsg(event, STR."[CQ:longmsg,id=\{actionData.getData()}]", false);
    }
}
