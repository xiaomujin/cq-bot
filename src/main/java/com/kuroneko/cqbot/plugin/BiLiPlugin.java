package com.kuroneko.cqbot.plugin;

import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.service.BiLiService;
import com.kuroneko.cqbot.utils.BotUtil;
import com.kuroneko.cqbot.vo.BiliDynamicVo;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.util.Optional;

@Slf4j
@Component
@RequiredArgsConstructor
public class BiLiPlugin extends BotPlugin {
    private final BiLiService biLiService;

    @Override
    public int onGroupMessage(Bot bot, GroupMessageEvent event) {
        String message = event.getRawMessage();
        if (message.startsWith(CmdConst.BILI_DYNAMICS)) {
            log.info("qq：{} 请求 {}", event.getUserId(), CmdConst.BILI_DYNAMICS);
            Optional<String> uidOp = BotUtil.getOneParam(CmdConst.BILI_DYNAMICS, message);
            if (uidOp.isEmpty()) {
                return MESSAGE_IGNORE;
            }
            String uid = uidOp.get();
            MsgUtils msg = MsgUtils.builder();
            Optional<BiliDynamicVo.BiliDynamicCard> firstCard = biLiService.getFirstCard(uid);
            if (firstCard.isEmpty()) {
                msg.text("查询失败或UID不存在！");
                bot.sendGroupMsg(event.getGroupId(), msg.build(), false);
                return MESSAGE_BLOCK;
            }
            BiliDynamicVo.BiliDynamicCard dynamicCard = firstCard.get();
            String path = biLiService.getNewScreenshot(dynamicCard.getId_str(), uid);
            if (ObjectUtils.isEmpty(path)) {
                msg.text(CmdConst.BILI_DYNAMICS + Constant.GET_FAIL);
                bot.sendGroupMsg(event.getGroupId(), msg.build(), false);
                return MESSAGE_BLOCK;
            }
            msg = biLiService.buildDynamicMsg(path, dynamicCard);
            bot.sendGroupMsg(event.getGroupId(), msg.build(), false);
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }

}
