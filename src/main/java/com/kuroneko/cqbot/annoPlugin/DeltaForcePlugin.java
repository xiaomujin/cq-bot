package com.kuroneko.cqbot.annoPlugin;

import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.core.annotation.BotHandler;
import com.kuroneko.cqbot.core.annotation.BotMsgHandler;
import com.kuroneko.cqbot.core.dto.MsgInfo;
import com.kuroneko.cqbot.dto.KkrbData;
import com.kuroneko.cqbot.enums.Regex;
import com.kuroneko.cqbot.enums.sysPluginRegex;
import com.kuroneko.cqbot.exception.ExceptionHandler;
import com.kuroneko.cqbot.service.DeltaForceService;
import com.kuroneko.cqbot.utils.BotUtil;
import com.kuroneko.cqbot.utils.CacheUtil;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
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
//        String text = matcher.group("text");
//        var msgId = event.getMessageId();
        ExceptionHandler.with(bot, event, () -> {
            KkrbData.OVData ovData = deltaForceService.getOVData();
            MsgUtils builder = MsgUtils.builder();
            builder.text("三角洲集市物品\n");
            if (ovData.getAriiData() != null && !ovData.getAriiData().isEmpty()) {
                KkrbData.AriiItem first = ovData.getAriiData().getFirst();
                builder.text(first.getActivityTime());
                ovData.getAriiData().forEach(item -> {
                    builder.img(item.getPic())
                            .text("名称：").text(item.getItemName()).text("\n")
                            .text("当前价格：").text(String.valueOf(item.getCurrectPrice())).text("\n")
                            .text("建议价格：").text(String.valueOf(item.getActivitySuggestedPrice())).text("\n")
                    ;
                });
            }
            return builder.build();
        });
    }

    @BotMsgHandler(model = sysPluginRegex.DELTA_FORCE_SYSTEM, cmd = Regex.DF_BC)
    public void bcHandler(MsgInfo msgInfo, Bot bot, AnyMessageEvent event, Matcher matcher) {
        log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), "三角洲脑机");
//        String text = matcher.group("text");
//        var msgId = event.getMessageId();
        ExceptionHandler.with(bot, event, () -> {
            KkrbData.OVData ovData = deltaForceService.getOVData();
            MsgUtils builder = MsgUtils.builder();
            builder.text("三角洲脑机物品\n");
            if (ovData.getBcicData() != null && !ovData.getBcicData().isEmpty()) {
                ovData.getBcicData().forEach(item -> {
                    builder.img("https://www.kkrb.net" + item.getPic().substring(1))
                            .text("名称：").text(item.getName()).text("\n")
                            .text("消耗能量：").text(String.valueOf(item.getEnergy())).text("\n")
                    ;
                });
            }
            return builder.build();
        });
    }

    @BotMsgHandler(model = sysPluginRegex.DELTA_FORCE_SYSTEM, cmd = Regex.DF_PASSWORD)
    public void passwordHandler(MsgInfo msgInfo, Bot bot, AnyMessageEvent event, Matcher matcher) {
        log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), "三角洲密码");
//        String text = matcher.group("text");
//        var msgId = event.getMessageId();
        ExceptionHandler.with(bot, event, () -> {
            KkrbData.OVData ovData = deltaForceService.getOVData();
            MsgUtils builder = MsgUtils.builder();
            builder.text("三角洲密码门\n");
            String updated = ovData.getBdData().getDb().getUpdated();// 20251209015448
            String updatedTime = updated.substring(0, 4) + "-" + updated.substring(4, 6) + "-" + updated.substring(6, 8);
            builder.text("更新时间：").text(updatedTime).text("\n");
            if (ovData.getBdData() != null) {
                builder.text("零号大坝：").text(ovData.getBdData().getDb().getPassword()).text("\n")
                        .text("长弓溪谷：").text(ovData.getBdData().getCgxg().getPassword()).text("\n")
                        .text("巴 克 什：").text(ovData.getBdData().getBks().getPassword()).text("\n")
                        .text("航天基地：").text(ovData.getBdData().getHtjd().getPassword()).text("\n")
                        .text("潮汐监狱：").text(ovData.getBdData().getCxjy().getPassword()).text("\n")
                ;
            }
            return builder.build();
        });
    }

    @BotMsgHandler(model = sysPluginRegex.DELTA_FORCE_SYSTEM, cmd = Regex.DF_OV)
    public void ovHandler(MsgInfo msgInfo, Bot bot, AnyMessageEvent event, Matcher matcher) {
        log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), "三角洲一图流");
//        String text = matcher.group("text");
//        var msgId = event.getMessageId();
        ExceptionHandler.with(bot, event, () -> CacheUtil.getOrPut(Regex.DF_OV, 5, TimeUnit.MINUTES, () -> {
            String imgPath = Constant.BASE_IMG_PATH + "kkrb/kkrbMain.png";
            deltaForceService.getOverviewScreenshot(imgPath);
            return MsgUtils.builder().img(BotUtil.getLocalMedia(imgPath)).build();
        }));
    }


}

