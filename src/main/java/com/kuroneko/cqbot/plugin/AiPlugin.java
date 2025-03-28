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

@Slf4j
@Component
public class AiPlugin extends BotPlugin {
    private final AiService aiService;

    public AiPlugin(AiService aiService) {
        this.aiService = aiService;
    }

    @Override
    public int onAnyMessage(Bot bot, AnyMessageEvent event) {
        if (BotUtil.isAtMe(event.getArrayMsg(), bot.getSelfId())) {
            log.info("qq：{} 请求 {}", event.getUserId(), CmdConst.TIWAN_AI);
            var msgId = event.getMessageId();
            try {
                bot.setGroupReaction(event.getGroupId(), msgId, "424", true);
                MsgUtils msg = getMsg(event.getGroupId(), msgId, BotUtil.getText(event.getArrayMsg()));
                bot.sendMsg(event, msg.build(), false);
            } finally {
                bot.setGroupReaction(event.getGroupId(), msgId, "424", false);
            }
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }

    private MsgUtils getMsg(long groupId, int msgId, String text) {
        String answer = "你想问什么呢";
        if (!ObjectUtils.isEmpty(text)) {
            answer = aiService.getScnetDS2(groupId, text);
        }
        log.info("问题：{} 的ai回答 {}", text, answer);
        return MsgUtils.builder().reply(msgId).text(answer);
    }
}
