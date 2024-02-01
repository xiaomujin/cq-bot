package com.kuroneko.cqbot.plugin;

import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.service.TarKovMarketService;
import com.kuroneko.cqbot.vo.TarKovMarketVo;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class TarKovMarketPlugin extends BotPlugin {
    private final TarKovMarketService tarKovMarketService;
    private static final String CMD = CmdConst.TIAO_ZAO;

    private String getText(String msg) {
        String text = "";
        if (msg.length() > CMD.length()) {
            text = msg.substring(CMD.length()).trim();
        }
        log.info("text:{}", text);
        return text;
    }

    @Override
    public int onAnyMessage(Bot bot, AnyMessageEvent event) {
        if (event.getRawMessage().startsWith(CMD)) {
            log.info("qq：{} 请求 {}", event.getUserId(), CMD);
            String text = getText(event.getRawMessage());
            MsgUtils msg = getMsg(text, event.getUserId());

            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }


    private MsgUtils getMsg(String text, Long qq) {
        List<TarKovMarketVo> search = tarKovMarketService.search(text);
        MsgUtils msg = MsgUtils.builder();
        if (search.isEmpty()) {
            return msg.text(" 没有找到").text(text);
        }
        search.forEach(it -> {
            msg.img(it.getWikiIcon());
            msg.text("\n名称：").text(STR."\{it.getCnName()}\n");
            msg.text("波动 24h：").text(STR."\{it.getChange24()}%").text("  7d：").text(STR."\{it.getChange7d()}%\n");
            msg.text("基础价格：").text(STR."\{it.getBasePrice()}₽\n");
            msg.text(it.getTraderName()).text("：").text(STR."\{it.getTraderPrice()}\{it.getTraderPriceCur()}\n");
            if (it.isCanSellOnFlea()) {
                msg.text("跳蚤日价：").text(STR."\{it.getAvgDayPrice()}₽\n");
                msg.text("跳蚤周价：").text(STR."\{it.getAvgWeekPrice()}₽\n");
                msg.text("单格：").text(STR."\{it.getAvgDayPrice() / it.getSize()}₽");
            } else {
                msg.text("单格：").text(STR."\{it.getTraderPrice() / it.getSize()}\{it.getTraderPriceCur()}\n");
                msg.text("跳蚤禁售!");
            }
        });
        return msg;
    }
}
