package com.kuroneko.cqbot.plugin;

import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.utils.MsgShiroUtil;
import com.kuroneko.cqbot.utils.PuppeteerUtil;
import com.kuroneko.cqbot.vo.AgeListVo;
import com.kuroneko.cqbot.vo.AniPreUP;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.ruiyun.jvppeteer.core.page.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.Optional;

@Slf4j
@Component
public class RecordPlugin extends BotPlugin {

    @Override
    public int onAnyMessage(Bot bot, AnyMessageEvent event) {
        String rawMessage = event.getRawMessage();
        if (rawMessage.startsWith(CmdConst.RECORD_SAY)) {
            log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), CmdConst.RECORD_SAY);
            Optional<String> oneParam = MsgShiroUtil.getOneParam(CmdConst.RECORD_SAY, rawMessage);
            if (oneParam.isEmpty()) {
                return MESSAGE_IGNORE;
            }
            String text = oneParam.get();
            OneBotMedia media = new OneBotMedia().file("https://artrajz-vits-simple-api.hf.space/voice/vits?text=" + URLEncoder.encode(text, StandardCharsets.UTF_8) + "&id=609").cache(false);
            MsgUtils msg = MsgUtils.builder().voice(media);
            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }
}
