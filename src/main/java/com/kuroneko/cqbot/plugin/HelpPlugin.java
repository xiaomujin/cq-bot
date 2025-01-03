package com.kuroneko.cqbot.plugin;

import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.enums.Regex;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Slf4j
@Component
public class HelpPlugin extends BotPlugin {

    private MsgUtils getMsg() {
        StringBuilder stringBuffer = new StringBuilder();
//        stringBuffer.append("指令列表").append(Constant.XN);
        List<String> allCmd = new ArrayList<>();
        allCmd.add(CmdConst.RI_BAO);
        allCmd.add(CmdConst.RI_LI);
        allCmd.add(CmdConst.TODAY_FANJU);
//        allCmd.add(CmdConst.SE_TU);
        allCmd.add(CmdConst.CHE_PAI);
        allCmd.add(CmdConst.TIAO_ZAO);
        allCmd.add(CmdConst.RAINBOW_KD);
        allCmd.add(CmdConst.THREE_HUNDRED_KD);
        allCmd.add(CmdConst.MAP);
        allCmd.add(CmdConst.ZI_DAN);
        allCmd.add(CmdConst.BILI_SUBSCRIBE);
        allCmd.add(CmdConst.BILI_SUBSCRIBE_CANCEL);
//        allCmd.add(CmdConst.RECORD_SAY);
        allCmd.add("任务流程图");
        allCmd.add("任务物品图");
        allCmd.add("信誉栏位图");
        allCmd.add("boss刷新率");
        allCmd.add("boss丢包时间");
        allCmd.add("3x4道具");
        allCmd.add("耳机强度");
        allCmd.add("我的本周词云");
        allCmd.add("");
        for (int i = 1; i <= allCmd.size(); i++) {
            stringBuffer.append(i).append(". ").append(allCmd.get(i - 1)).append(Constant.XN);
        }
        return MsgUtils.builder().text(stringBuffer.toString());
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
