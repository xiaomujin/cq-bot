package com.kuroneko.cqbot.plugin;

import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.constant.Constant;
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
public class TarKovMarcetPlugin extends BotPlugin {
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
            if (checkCd(event.getUserId())) {
                return MESSAGE_BLOCK;
            }
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
        search.forEach(tarKovMarketVo -> {
            msg.img(tarKovMarketVo.getEnImg());
            msg.text(Constant.XN + "名称：").text(tarKovMarketVo.getCnName() + Constant.XN);
            msg.text("占格：").text(tarKovMarketVo.getSize() + Constant.XN);
            msg.text("商人价格：").text(tarKovMarketVo.getTraderPrice() + tarKovMarketVo.getTraderPriceCur() + Constant.XN);
            msg.text("日价：").text(tarKovMarketVo.getAvgDayPrice() + Constant.XN);
            msg.text("周价：").text(tarKovMarketVo.getAvgWeekPrice() + Constant.XN);
            msg.text("单格日价：").text(tarKovMarketVo.getAvgDayPricePerSlot() + Constant.XN);
            msg.text("单格周价：").text(tarKovMarketVo.getAvgWeekPricePerSlot() + Constant.XN);
        });
        return msg;
    }

    private boolean checkCd(Long qq) {
        Long time = Constant.TAR_KOV_MARKET_CD.get(qq);
        if (time == null || System.currentTimeMillis() - time > 5000) {
            Constant.TAR_KOV_MARKET_CD.put(qq, System.currentTimeMillis());
            return false;
        }
        return true;

    }
}
