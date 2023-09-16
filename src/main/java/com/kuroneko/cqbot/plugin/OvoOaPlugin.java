package com.kuroneko.cqbot.plugin;

import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.utils.MsgShiroUtil;
import com.kuroneko.cqbot.utils.QqUtil;
import com.kuroneko.cqbot.utils.RedisUtil;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import io.micrometer.common.util.StringUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.util.Arrays;
import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class OvoOaPlugin extends BotPlugin {

    private final RedisUtil redisUtil;
    private final RestTemplate restTemplate;

    private final QqUtil qqUtil;

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

            if (qqUtil.verifyQq(event.getUserId())){
                MsgUtils msg = MsgUtils.builder().text("暂无使用权限");
                bot.sendGroupMsg(event.getGroupId(), msg.build(), false);
                return MESSAGE_BLOCK;
            }


            OneBotMedia media = new OneBotMedia().file(Constant.VVHAN_GIRL_URL).cache(false);
            bot.sendMsg(event, MsgUtils.builder().img(media).build(), false);
            return MESSAGE_BLOCK;
        } else if (text.startsWith(CmdConst.TAO)) {

            if (qqUtil.verifyQq(event.getUserId())){
                MsgUtils msg = MsgUtils.builder().text("暂无使用权限");
                bot.sendGroupMsg(event.getGroupId(), msg.build(), false);
                return MESSAGE_BLOCK;
            }

            OneBotMedia media = new OneBotMedia().file(Constant.VVHAN_TAO_URL).cache(false);
            bot.sendMsg(event, MsgUtils.builder().img(media).build(), false);
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
