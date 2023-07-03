package com.kuroneko.cqbot.plugin;

import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.utils.JvppeteerUtil;
import com.kuroneko.cqbot.vo.AgeListVo;
import com.kuroneko.cqbot.vo.AniPreUP;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.util.ObjectUtils;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;

@Slf4j
@Component
public class AgePlugin extends BotPlugin {

    @Override
    public int onAnyMessage(Bot bot, AnyMessageEvent event) {
        if (event.getRawMessage().startsWith(CmdConst.NEW_FANJU)) {
            log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), CmdConst.NEW_FANJU);
            AgeListVo ageListVo = (AgeListVo) Constant.CONFIG_CACHE.get(Constant.AGE_LIST_KEY);

            if (ageListVo == null) {
                bot.sendMsg(event, CmdConst.NEW_FANJU + Constant.GET_FAIL, false);
                return MESSAGE_BLOCK;
            }
            List<AniPreUP> aniPreUPs = ageListVo.getAniPreUP();
            aniPreUPs.forEach(aniPreUP -> {
                String href = Constant.AGE_HOST_URL + aniPreUP.getHref();
                String pic = aniPreUP.getPicSmall().startsWith("http") ? aniPreUP.getPicSmall() : "https:" + aniPreUP.getPicSmall();
                MsgUtils msg = MsgUtils.builder()
                        .img(pic)
                        .text(aniPreUP.getTitle() + Constant.XN)
                        .text(aniPreUP.getNewTitle() + Constant.XN)
                        .text(href);
                bot.sendMsg(event, msg.build(), false);
            });
            return MESSAGE_BLOCK;
        } else if (event.getRawMessage().startsWith(CmdConst.TODAY_FANJU)) {
            log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), CmdConst.TODAY_FANJU);
            String imgPath = Constant.BASE_IMG_PATH + "TODAY_FANJU.png";
            String screenshot = JvppeteerUtil.screenshot(Constant.AGE_HOST_URL, imgPath, "#container > div.div_right.baseblock > div.blockcontent");
            if (ObjectUtils.isEmpty(screenshot)) {
                bot.sendMsg(event, CmdConst.TODAY_FANJU + Constant.GET_FAIL, false);
                return MESSAGE_BLOCK;
            }
            OneBotMedia media = OneBotMedia.builder().file("http://localhost:8081/getImage?path=" + URLEncoder.encode(imgPath, Charset.defaultCharset())).cache(false);
            MsgUtils msg = MsgUtils.builder().img(media).text(Constant.AGE_HOST_URL);
            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }
}
