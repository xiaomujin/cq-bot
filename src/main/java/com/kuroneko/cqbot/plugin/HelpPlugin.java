package com.kuroneko.cqbot.plugin;

import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.constant.Constant;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class HelpPlugin extends BotPlugin {

    private MsgUtils getMsg() {
//        StringBuilder stringBuffer = new StringBuilder();
//        stringBuffer.append("指令列表").append(Constant.XN);
//        List<String> allCmd = CmdConst.getAllCmd();
//        for (int i = 1; i <= allCmd.size(); i++) {
//            stringBuffer.append(i).append(".").append(allCmd.get(i - 1)).append(Constant.XN);
//        }
        return MsgUtils.builder().text("还没做呢！");
    }

    @Override
    public int onAnyMessage(Bot bot, AnyMessageEvent event) {
        if (event.getRawMessage().startsWith(CmdConst.HELP)) {
            log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), CmdConst.HELP);
            MsgUtils msg = getMsg();
            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }
}
