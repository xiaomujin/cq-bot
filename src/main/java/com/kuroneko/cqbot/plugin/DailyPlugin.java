package com.kuroneko.cqbot.plugin;

import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.vo.DailyVo;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DailyPlugin extends BotPlugin {

    @Override
    public int onAnyMessage(Bot bot, AnyMessageEvent event) {
        if (event.getRawMessage().startsWith(CmdConst.RI_BAO)) {
            log.info("qq：{} 请求 {}", event.getUserId(), CmdConst.RI_BAO);
            DailyVo dailyVo = (DailyVo) Constant.CONFIG_CACHE.get(Constant.DAILY_KEY);

            if (dailyVo == null) {
                bot.sendMsg(event, CmdConst.RI_BAO + Constant.GET_FAIL, false);
                return MESSAGE_BLOCK;
            }
            MsgUtils msg = MsgUtils.builder().img(dailyVo.getImgUrl());
//            MsgUtils msg = MsgUtils.builder().img(Constant.DAILY_URL_2);

            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        }
        if (event.getRawMessage().startsWith(CmdConst.RI_LI)) {
            log.info("qq：{} 请求 {}", event.getUserId(), CmdConst.RI_LI);
            String riLiVo = (String) Constant.CONFIG_CACHE.get(Constant.MO_YU_RI_LI_KEY);

            if (riLiVo == null) {
                bot.sendMsg(event, CmdConst.RI_LI + Constant.GET_FAIL, false);
                return MESSAGE_BLOCK;
            }
            MsgUtils msg = MsgUtils.builder().img(riLiVo);

            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }
}
