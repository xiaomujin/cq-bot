package com.kuroneko.cqbot.plugin;

import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.utils.RedisUtil;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscribePlugin extends BotPlugin {
    private static final String CMD1 = CmdConst.OPEN;
    private static final String CMD2 = CmdConst.CLOSE;
    private final RedisUtil redisUtil;


    @Override
    public int onAnyMessage(Bot bot, AnyMessageEvent event) {
        String message = event.getRawMessage();
        if (message.startsWith(CMD1)) {
            log.info("qq：{} 请求 {}", event.getUserId(), CMD1);

            //三兄弟位置推送
            if (message.contains("三兄弟") || message.contains("三狗")) {
                pushMapPut("THREE_DOG", event.getGroupId());
                MsgUtils msg = MsgUtils.builder().text("成功订阅三兄弟位置推送");
                bot.sendMsg(event, msg.build(), false);
                return MESSAGE_BLOCK;
            } else if (message.contains("塔科夫动态")) {
                pushMapPut("TKF_DT", event.getGroupId());
                MsgUtils msg = MsgUtils.builder().text("成功订阅塔科夫动态推送");
                bot.sendMsg(event, msg.build(), false);
                return MESSAGE_BLOCK;
            }

        } else if (message.startsWith(CMD2)) {
            log.info("qq：{} 请求 {}", event.getUserId(), CMD2);

            //三兄弟位置推送
            if (message.contains("三兄弟") || message.contains("三狗")) {
                MsgUtils msg = getRemoveMsg("THREE_DOG", event.getGroupId(), "取消订阅三兄弟位置推送", "本群没有订阅三兄弟位置推送");
                bot.sendMsg(event, msg.build(), false);
                return MESSAGE_BLOCK;
            } else if (message.contains("塔科夫动态")) {
                MsgUtils msg = getRemoveMsg("TKF_DT", event.getGroupId(), "取消订阅塔科夫动态推送", "本群没有订阅塔科夫动态推送");
                bot.sendMsg(event, msg.build(), false);
                return MESSAGE_BLOCK;
            }

        }
        return MESSAGE_IGNORE;
    }

    public MsgUtils getRemoveMsg(String mapKey, long groupId, String removeText, String unRemoveText) {
        MsgUtils msg = MsgUtils.builder();
        if (redisUtil.remove(mapKey, groupId) > 0L) {
            msg.text(removeText);
        } else {
            msg.text(unRemoveText);
        }
        return msg;
    }

    public void pushMapPut(String mapKey, long groupId) {
        redisUtil.add(mapKey, groupId);
    }


}
