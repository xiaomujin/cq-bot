package com.kuroneko.cqbot.plugin;

import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.service.BiLiService;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

@Slf4j
@Component
@RequiredArgsConstructor
public class BiLiPlugin extends BotPlugin {
    private static final String CMD = CmdConst.TAKOV_DYNAMICS;
    private final BiLiService biLiService;


    @Override
    public int onAnyMessage(Bot bot, AnyMessageEvent event) {
        String message = event.getRawMessage();
        if (message.startsWith(CMD)) {
            log.info("qq：{} 请求 {}", event.getUserId(), CMD);
            String uid = "152065343";
            String dynamicId = biLiService.getNew(uid);
            String path = biLiService.getNewScreenshot(dynamicId, uid);
            if (ObjectUtils.isEmpty(path)) {
                bot.sendMsg(event, CMD + Constant.GET_FAIL, false);
                return MESSAGE_BLOCK;
            }
            MsgUtils msg = biLiService.buildDynamicMsg(path, dynamicId);
            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;

        }
        return MESSAGE_IGNORE;
    }


}
