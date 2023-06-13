package com.kuroneko.cqbot.plugin;

import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.service.ZeroMagnetService;
import com.kuroneko.cqbot.vo.ZeroMagnetVo;
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
public class ZeroMagnetPlugin extends BotPlugin {
    private final ZeroMagnetService zeroMagnetService;

    private MsgUtils getMsg(String text) {
        String query = "";
        int index = 0;
        if (text.length() > 3) {
            query = text.substring(2).trim();
        }
        String[] strings = query.split(" ");
        switch (strings.length) {
            case 2:
                index = Integer.parseInt(strings[1]);
            case 1:
                query = strings[0];
        }

        ZeroMagnetVo zeroMagnetVo = zeroMagnetService.getZeroMagnetVo(query, index);
        return MsgUtils.builder()
                .text("title:" +zeroMagnetVo.getTitle() + Constant.XN)
                .text(zeroMagnetVo.getMagnet() + Constant.XN)
                .text("size:" + zeroMagnetVo.getSize());
    }

    @Override
    public int onAnyMessage(Bot bot, AnyMessageEvent event) {
        if (event.getRawMessage().startsWith(CmdConst.CHE_PAI)) {
            log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), CmdConst.CHE_PAI);
            MsgUtils msg = getMsg(event.getRawMessage());
            bot.sendGroupMsg(event.getGroupId(), msg.build(), false);
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }
}
