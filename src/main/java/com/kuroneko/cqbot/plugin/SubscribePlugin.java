package com.kuroneko.cqbot.plugin;

import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.constant.RedisKey;
import com.kuroneko.cqbot.service.BiLiService;
import com.kuroneko.cqbot.utils.BotUtil;
import com.kuroneko.cqbot.utils.QqUtil;
import com.kuroneko.cqbot.utils.RedisUtil;
import com.kuroneko.cqbot.vo.BiliDynamicVo;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscribePlugin extends BotPlugin {
    private static final String CMD1 = CmdConst.OPEN;
    private static final String CMD2 = CmdConst.CLOSE;
    private final RedisUtil redisUtil;
    private final BiLiService biLiService;
    private final QqUtil qqUtil;


    @Override
    public int onAnyMessage(Bot bot, AnyMessageEvent event) {
        String message = event.getRawMessage();
        if (message.startsWith(CMD1)) {
            log.info("qq：{} 请求 {}", event.getUserId(), CMD1);

            //三兄弟位置推送
            if (message.contains("三兄弟") || message.contains("三狗")) {
                pushMapPut(RedisKey.THREE_DOG, event.getGroupId());
                MsgUtils msg = MsgUtils.builder().text("成功订阅三兄弟位置推送");
                bot.sendMsg(event, msg.build(), false);
                return MESSAGE_BLOCK;
            } else if (message.contains("塔科夫动态")) {
                pushMapPut(RedisKey.TKF_DT, event.getGroupId());
                MsgUtils msg = MsgUtils.builder().text("成功订阅塔科夫动态推送");
                bot.sendMsg(event, msg.build(), false);
                return MESSAGE_BLOCK;
            } else if (message.contains("塔科夫服务器")) {
                pushMapPut(RedisKey.TKF_INFO, event.getGroupId());
                MsgUtils msg = MsgUtils.builder().text("成功订阅塔科夫服务器状态推送");
                bot.sendMsg(event, msg.build(), false);
                return MESSAGE_BLOCK;
            }

        } else if (message.startsWith(CMD2)) {
            log.info("qq：{} 请求 {}", event.getUserId(), CMD2);

            //三兄弟位置推送
            if (message.contains("三兄弟") || message.contains("三狗")) {
                MsgUtils msg = getRemoveMsg(RedisKey.THREE_DOG, event.getGroupId(), "取消订阅三兄弟位置推送", "本群没有订阅三兄弟位置推送");
                bot.sendMsg(event, msg.build(), false);
                return MESSAGE_BLOCK;
            } else if (message.contains("塔科夫动态")) {
                MsgUtils msg = getRemoveMsg(RedisKey.TKF_DT, event.getGroupId(), "取消订阅塔科夫动态推送", "本群没有订阅塔科夫动态推送");
                bot.sendMsg(event, msg.build(), false);
                return MESSAGE_BLOCK;
            } else if (message.contains("塔科夫服务器")) {
                MsgUtils msg = getRemoveMsg(RedisKey.TKF_INFO, event.getGroupId(), "取消订阅塔科夫服务器状态推送", "本群没有订阅塔科夫动态推送");
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


    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        MsgUtils msg = MsgUtils.builder();
        String message = event.getRawMessage();



        if (message.startsWith(CmdConst.BILI_SUBSCRIBE)) {
            log.info("qq：{} 请求 {}", event.getUserId(), CmdConst.BILI_SUBSCRIBE);
            if (qqUtil.verifyQq(event)){
                bot.sendGroupMsg(event.getGroupId(), MsgUtils.builder().text("暂无使用权限").build(), false);
                return MESSAGE_BLOCK;
            }
            Optional<String> uidOp = BotUtil.getOneParam(CmdConst.BILI_DYNAMICS, message);
            if (uidOp.isEmpty()) return MESSAGE_IGNORE;

            String uid = uidOp.get();
            Optional<BiliDynamicVo.BiliDynamicCard> firstCard = biLiService.getFirstCard(uid);
            if (firstCard.isEmpty()) {
                msg.text("订阅失败或UID不存在！");
                bot.sendGroupMsg(event.getGroupId(), msg.build(), false);
                return MESSAGE_BLOCK;
            }
            BiliDynamicVo.BiliDynamicCard dynamicCard = firstCard.get();
            BiliDynamicVo.ModuleAuthor moduleAuthor = dynamicCard.getModules().getModule_author();
            //添加到持久化
            redisUtil.add(RedisKey.BILI_SUB + ":" + uid, event.getGroupId());

            OneBotMedia media = OneBotMedia.builder().file(moduleAuthor.getFace()).cache(false);
            msg.img(media)
                    .text("UID: " + moduleAuthor.getMid() + Constant.XN)
                    .text("昵称: " + moduleAuthor.getName() + Constant.XN)
                    .text("订阅成功~");
            bot.sendGroupMsg(event.getGroupId(), msg.build(), false);
            return MESSAGE_BLOCK;
        } else if (message.startsWith(CmdConst.BILI_SUBSCRIBE_CANCEL)) {
            log.info("qq：{} 请求 {}", event.getUserId(), CmdConst.BILI_SUBSCRIBE_CANCEL);
            Optional<String> uidOp = BotUtil.getOneParam(CmdConst.BILI_SUBSCRIBE_CANCEL, message);
            if (uidOp.isEmpty()) return MESSAGE_IGNORE;
            msg = MsgUtils.builder();
            String uid = uidOp.get();
            Long remove = redisUtil.remove(RedisKey.BILI_SUB + ":" + uid, event.getGroupId());
            if (remove > 0) {
                msg.text(uid + " 取消订阅成功");
            } else {
                msg.text(uid + " 未订阅");
            }
            bot.sendGroupMsg(event.getGroupId(), msg.build(), false);
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }
}
