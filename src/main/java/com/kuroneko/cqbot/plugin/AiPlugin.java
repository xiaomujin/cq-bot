package com.kuroneko.cqbot.plugin;

import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.service.AiService;
import com.kuroneko.cqbot.utils.BotUtil;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class AiPlugin extends BotPlugin {
    private final AiService aiService;

    public AiPlugin(AiService aiService) {
        this.aiService = aiService;
    }

    @Override
    public int onAnyMessage(Bot bot, AnyMessageEvent event) {
        if (!aiService.isEnabled()) {
            return MESSAGE_IGNORE;
        }
        if (!BotUtil.isAtMe(event.getArrayMsg(), bot.getSelfId())) {
            return MESSAGE_IGNORE;
        }
        log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), CmdConst.TIWAN_AI);
        var msgId = event.getMessageId();
        try {
            bot.setGroupReaction(event.getGroupId(), msgId, "424", true);
            List<String> msgList = getMsgList(event, msgId, BotUtil.getText(event.getArrayMsg()));
            BotUtil.sendMsgList(bot, event, msgList, false);
        } finally {
            bot.setGroupReaction(event.getGroupId(), msgId, "424", false);
        }
        return MESSAGE_BLOCK;
    }

    private List<String> getMsgList(AnyMessageEvent event, int msgId, String text) {
        String answer = "你想问什么呢";
        if (!ObjectUtils.isEmpty(text)) {
            answer = aiService.getAiAnswer(new AiService.AiRequest(
                    event.getGroupId(),
                    event.getSender().getUserId(),
                    event.getSender().getNickname(),
                    text
            ));
        }
        log.info("问题：{} 的ai回答 {}", text, answer);
        String[] split = answer.split("\n\n");
        List<String> list = new ArrayList<>();
        for (int i = 0; i < split.length; i++) {
            String build;
            if (i == 0) {
                build = MsgUtils.builder().reply(msgId).text(split[i]).build();
            } else {
                build = MsgUtils.builder().text(split[i]).build();
            }
            list.add(build);
        }
        return list;
    }
}
