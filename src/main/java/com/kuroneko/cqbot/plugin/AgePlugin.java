package com.kuroneko.cqbot.plugin;

import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.constant.Constant;
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
            Page newPage = PuppeteerUtil.getNewPage(Constant.AGE_HOST_URL);
            String screenshot = PuppeteerUtil.screenshot(newPage
                    , imgPath
                    , "div.text_list_box.weekly_list.mb-4"
                    , ".global_notice_wrapper {display: none !important;} .head_content_wrapper {display: none !important;}"
            );
            if (ObjectUtils.isEmpty(screenshot)) {
                bot.sendMsg(event, CmdConst.TODAY_FANJU + Constant.GET_FAIL, false);
                return MESSAGE_BLOCK;
            }
            OneBotMedia media = OneBotMedia.builder().file("http://localhost:8081/getImage?path=" + imgPath).cache(false);
            MsgUtils msg = MsgUtils.builder().img(media).text("https://agefans.com/");
            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }
}
