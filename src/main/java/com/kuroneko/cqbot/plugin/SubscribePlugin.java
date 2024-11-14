package com.kuroneko.cqbot.plugin;

import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.core.cfg.ConfigManager;
import com.kuroneko.cqbot.service.BiLiService;
import com.kuroneko.cqbot.utils.BotUtil;
import com.kuroneko.cqbot.utils.QqUtil;
import com.kuroneko.cqbot.vo.BiliDynamicVo;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class SubscribePlugin extends BotPlugin {
    private final BiLiService biLiService;
    private final QqUtil qqUtil;

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        MsgUtils msg = MsgUtils.builder();
        String message = event.getRawMessage();

        if (message.startsWith(CmdConst.BILI_SUBSCRIBE)) {
            log.info("qq：{} 请求 {}", event.getUserId(), CmdConst.BILI_SUBSCRIBE);
            if (qqUtil.verifyQq(event.getUserId())) {
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
            ConfigManager.ins.getBiliCfg().addSub(uid, event.getGroupId());

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
            Boolean remove = ConfigManager.ins.getBiliCfg().removeSub(uid, event.getGroupId());
            if (remove) {
                msg.text(uid + " 退订成功");
            } else {
                msg.text(uid + " 未订阅");
            }
            bot.sendGroupMsg(event.getGroupId(), msg.build(), false);
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }
}
