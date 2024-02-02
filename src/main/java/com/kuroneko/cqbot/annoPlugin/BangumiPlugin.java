package com.kuroneko.cqbot.annoPlugin;

import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.enums.Regex;
import com.kuroneko.cqbot.exception.ExceptionHandler;
import com.kuroneko.cqbot.utils.BotUtil;
import com.kuroneko.cqbot.utils.CacheUtil;
import com.kuroneko.cqbot.utils.PuppeteerUtil;
import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.ruiyun.jvppeteer.core.page.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

@Shiro
@Slf4j
@Component
@RequiredArgsConstructor
public class BangumiPlugin {

    @AnyMessageHandler(cmd = Regex.BANGUMI_CALENDAR)
    public void bangumiCalendar(Bot bot, AnyMessageEvent event, Matcher matcher) {
        log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), Regex.BANGUMI_CALENDAR);
        ExceptionHandler.with(bot, event, () -> CacheUtil.getOrPut(Regex.BANGUMI_CALENDAR, 20, TimeUnit.MINUTES, () -> {
            String imgPath = STR."\{Constant.BASE_IMG_PATH}BANGUMI_CALENDAR.png";
            Page newPage = PuppeteerUtil.getNewPage(Constant.AGE_HOST_URL);
            PuppeteerUtil.screenshot(newPage
                    , imgPath
                    , "div.text_list_box.weekly_list.mb-4"
                    , ".global_notice_wrapper {display: none !important;} .head_content_wrapper {display: none !important;}"
            );
            OneBotMedia localMedia = BotUtil.getLocalMedia(imgPath, false);
            return MsgUtils.builder()
                    .img(localMedia)
                    .text("age：").text("https://rentry.la/agefans\n")
                    .text("次元城：").text("https://www.cycdm01.top/")
                    .build();
        }));
    }
}

