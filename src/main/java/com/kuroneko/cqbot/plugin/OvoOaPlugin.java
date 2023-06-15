package com.kuroneko.cqbot.plugin;

import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.utils.MsgShiroUtil;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OvoOaPlugin extends BotPlugin {
    private final RestTemplate restTemplate;

    private MsgUtils getMsg(String qq) {
        return MsgUtils.builder().img(Constant.OVO_OA_PA_URL + qq);
    }

    @Override
    public int onAnyMessage(Bot bot, AnyMessageEvent event) {
        List<Long> atList = MsgShiroUtil.getAtList(event.getArrayMsg());
        String text = MsgShiroUtil.getText(event.getArrayMsg());
        if (text.startsWith(CmdConst.PA) && !ObjectUtils.isEmpty(atList)) {
            log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), CmdConst.PA);
            MsgUtils msg = getMsg(Long.toString(atList.get(0)));
            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        } else if (text.startsWith(CmdConst.TUI)) {
            bot.sendMsg(event, MsgUtils.builder().img(Constant.OVO_OA_TUI_URL).build(), false);
            return MESSAGE_BLOCK;
        } else if (text.startsWith(CmdConst.TGRJ)) {
            String object = restTemplate.getForObject(Constant.OVO_OA_TGRJ_URL, String.class);
            assert object != null;
            bot.sendMsg(event, MsgUtils.builder().text(object).build(), false);
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }
}