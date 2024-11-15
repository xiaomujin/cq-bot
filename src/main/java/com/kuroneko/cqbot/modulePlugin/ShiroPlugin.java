package com.kuroneko.cqbot.modulePlugin;

import com.kuroneko.cqbot.core.dto.MsgInfo;
import com.kuroneko.cqbot.core.BotPluginHandler;
import com.kuroneko.cqbot.enums.sysPluginRegex;
import com.kuroneko.cqbot.utils.RegexUtil;
import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;


@Shiro
@Slf4j
@Component
@RequiredArgsConstructor
public class ShiroPlugin {
    public final BotPluginHandler botPluginHandler;

    @AnyMessageHandler()
    @Async("botTaskExecutor")
    public void ShiroAnyMessageHandler(Bot bot, AnyMessageEvent event) {
        long selfId = bot.getSelfId();
        MsgInfo msgInfo = convertMsg(event.getArrayMsg(), selfId);
        for (sysPluginRegex value : sysPluginRegex.values()) {
            Optional<String> match = match(msgInfo.getMsg(), value.regex);
            if (match.isPresent()) {
                msgInfo.setMsg(match.get().trim());
                botPluginHandler.handler(value, msgInfo, bot, event);
                return;
            }
        }
//        match(msgInfo.getMsg(), TKF_SYSTEM)

        // 对应模块系统入口分发
    }

    private MsgInfo convertMsg(List<ArrayMsg> arrayMsg, long selfId) {
        AtomicBoolean at = new AtomicBoolean(false);
        StringBuilder builder = new StringBuilder();
        ArrayList<String> imgList = new ArrayList<>();
        HashSet<Long> atList = new HashSet<>();
        arrayMsg.forEach(msg -> {
            switch (msg.getType()) {
                case at -> {
                    String qq = msg.getData().get("qq");
                    if (String.valueOf(selfId).equals(qq)) {
                        at.set(true);
                    } else {
                        atList.add(Long.valueOf(qq));
                    }
                }
                case text -> {
                    String text = msg.getData().get("text");
                    builder.append(text).append("\n");
                }
                case image -> {
                    String image = msg.getData().get("url");
                    imgList.add(image);
                }
            }
        });
        return MsgInfo.builder()
                .isAt(at.get())
                .msg(builder.toString().trim())
                .imgList(imgList)
                .atList(atList)
                .build();
    }

    private Optional<String> match(String msg, String pattern) {
        return RegexUtil.group("text", msg, pattern);
    }

}
