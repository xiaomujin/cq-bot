package com.kuroneko.cqbot.annoPlugin;

import com.kuroneko.cqbot.core.annotation.BotHandler;
import com.kuroneko.cqbot.core.annotation.BotMsgHandler;
import com.kuroneko.cqbot.core.dto.MsgInfo;
import com.kuroneko.cqbot.dto.KkrbData;
import com.kuroneko.cqbot.enums.Regex;
import com.kuroneko.cqbot.enums.sysPluginRegex;
import com.kuroneko.cqbot.exception.ExceptionHandler;
import com.kuroneko.cqbot.service.DeltaForceService;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.regex.Matcher;

@BotHandler
@Slf4j
@Component
@RequiredArgsConstructor
public class DeltaForcePlugin {

    private final DeltaForceService deltaForceService;

    @BotMsgHandler(model = sysPluginRegex.DELTA_FORCE_SYSTEM, cmd = Regex.DF_MARKET)
    public void marketHandler(MsgInfo msgInfo, Bot bot, AnyMessageEvent event, Matcher matcher) {
        log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), "三角洲集市");
        String text = matcher.group("text");
        var msgId = event.getMessageId();
        ExceptionHandler.with(bot, event, () -> {
            KkrbData.OVData ovData = deltaForceService.getOVData();
            MsgUtils builder = MsgUtils.builder();
            builder.text("三角洲集市");
            ovData.getAriiData().forEach(item -> {
                builder.img(item.getPic())
                        .text("名称：").text(item.getItemName())
                        .text("当前价格：").text(String.valueOf(item.getCurrectPrice()));
            });
            return builder.build();
        });
    }


}

