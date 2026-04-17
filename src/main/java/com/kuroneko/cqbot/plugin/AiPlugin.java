package com.kuroneko.cqbot.plugin;

import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.service.AiService;
import com.kuroneko.cqbot.utils.BotUtil;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.action.common.ActionData;
import com.mikuac.shiro.dto.action.response.MsgResp;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.model.ArrayMsg;
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
            sendStreamingResponse(bot, event, msgId, BotUtil.getText(event.getArrayMsg()));
        } finally {
            bot.setGroupReaction(event.getGroupId(), msgId, "424", false);
        }
        return MESSAGE_BLOCK;
    }

    /**
     * 发送流式 AI 响应，检测到 \n\n 时立即发送
     *
     * @param bot   机器人实例
     * @param event 消息事件
     * @param msgId 消息 ID
     * @param text  用户输入的文本
     */
    private void sendStreamingResponse(Bot bot, AnyMessageEvent event, int msgId, String text) {
        List<String> imgUrls = BotUtil.getImgUrls(event.getArrayMsg());
        if (imgUrls.isEmpty()) {
            int replyMsgId = BotUtil.getReplyMsgId(event.getArrayMsg());
            if (replyMsgId != 0) {
                ActionData<MsgResp> replyMsg = bot.getMsg(replyMsgId);
                if (replyMsg != null && replyMsg.getRetCode().equals(0)) {
                    List<ArrayMsg> arrayMsg = replyMsg.getData().getArrayMsg();
                    imgUrls = BotUtil.getImgUrls(arrayMsg);
                }
            }
        }

        if (ObjectUtils.isEmpty(text)) {
            log.info("问题：{} 的ai回答 {}", text, "你想问什么呢");
            bot.sendGroupMsg(event.getGroupId(), MsgUtils.builder().reply(msgId).text("你想问什么呢").build(), false);
            return;
        }

        StringBuilder buffer = new StringBuilder();
        List<String> sentMessages = new ArrayList<>();
        boolean[] isFirstMessage = {true};

        aiService.getAiAnswer(new AiService.AiRequest(
                event.getGroupId(),
                event.getSender().getUserId(),
                event.getSender().getNickname(),
                text,
                imgUrls
        )).subscribe(
                chunk -> {
                    buffer.append(chunk);
                    processBuffer(bot, event, msgId, buffer, sentMessages, isFirstMessage);
                },
                error -> {
                    log.error("问题：{} 的ai回答异常", text, error);
                    bot.sendGroupMsg(event.getGroupId(), MsgUtils.builder().reply(msgId).text("emmmm，AI 暂时不可用，请稍后再试").build(), false);
                },
                () -> {
                    // 流结束，发送剩余的文本
                    if (buffer.length() > 0) {
                        String remaining = buffer.toString();
                        String message = isFirstMessage[0]
                                ? MsgUtils.builder().reply(msgId).text(remaining).build()
                                : MsgUtils.builder().text(remaining).build();
                        bot.sendGroupMsg(event.getGroupId(), message, false);
                    }
                    log.info("问题：{} 的ai回答完成，共发送 {} 条消息", text, sentMessages.size());
                }
        );
    }

    /**
     * 处理累积的文本缓冲区，检测到 \n\n 时立即发送
     *
     * @param bot            机器人实例
     * @param event          消息事件
     * @param msgId          消息 ID
     * @param buffer         文本缓冲区
     * @param sentMessages   已发送消息列表
     * @param isFirstMessage 是否是第一条消息
     */
    private void processBuffer(Bot bot, AnyMessageEvent event, int msgId,
                               StringBuilder buffer, List<String> sentMessages, boolean[] isFirstMessage) {
        String content = buffer.toString();
        int separatorIndex = content.indexOf("\n\n");

        while (separatorIndex != -1) {
            String messagePart = content.substring(0, separatorIndex);
            buffer.delete(0, separatorIndex + 2); // 删除已发送的部分（包括 \n\n）
            if (messagePart.trim().isEmpty()) {
                continue;
            }
            String message = isFirstMessage[0]
                    ? MsgUtils.builder().reply(msgId).text(messagePart).build()
                    : MsgUtils.builder().text(messagePart).build();

            bot.sendGroupMsg(event.getGroupId(), message, false);
            sentMessages.add(messagePart);

            if (isFirstMessage[0]) {
                isFirstMessage[0] = false;
            }

            content = buffer.toString();
            separatorIndex = content.indexOf("\n\n");
        }
    }
}
