package com.kuroneko.cqbot.plugin;

import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.constant.RedisKey;
import com.kuroneko.cqbot.utils.PuppeteerUtil;
import com.kuroneko.cqbot.vo.ThreeDog;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.ruiyun.jvppeteer.core.page.ElementHandle;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.PageNavigateOptions;
import com.ruiyun.jvppeteer.options.ScreenshotOptions;
import com.ruiyun.jvppeteer.options.Viewport;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.util.CastUtils;
import org.springframework.stereotype.Component;

import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
@RequiredArgsConstructor
public class TarKovMapPlugin extends BotPlugin {
    private static final String CMD = CmdConst.MAP;

    @Override
    public int onAnyMessage(Bot bot, AnyMessageEvent event) {
        if (event.getRawMessage().contains(CMD)) {
            log.info("qq：{} 请求 {}", event.getUserId(), CMD);
            String imgPath = Constant.BASE_IMG_PATH + "tarkov_map/";

            String rawMessage = event.getRawMessage();

            if (rawMessage.contains("储备站")) {
                imgPath += "储备站.jpg";
            } else if (rawMessage.contains("灯塔")) {
                imgPath += "灯塔.jpg";
            } else if (rawMessage.contains("工厂")) {
                imgPath += "工厂.jpg";
            } else if (rawMessage.contains("海岸线")) {
                imgPath += "海岸线.jpg";
            } else if (rawMessage.contains("海关")) {
                imgPath += "海关.jpg";
            } else if (rawMessage.contains("街区")) {
                imgPath += "街区.jpg";
            } else if (rawMessage.contains("立交桥")) {
                imgPath += "立交桥.jpg";
            } else if (rawMessage.contains("森林")) {
                imgPath += "森林.jpg";
            } else if (rawMessage.contains("实验室")) {
                imgPath += "实验室.jpg";
            } else if (rawMessage.contains("疗养院")) {
                imgPath += "疗养院.jpg";
            } else {
                return MESSAGE_IGNORE;
            }

            OneBotMedia media = OneBotMedia.builder().file("http://localhost:8081/getJpgImage?path=" + imgPath).cache(false);
            MsgUtils msg = MsgUtils.builder().img(media);
            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        } else if (event.getRawMessage().equals(CmdConst.ZI_DAN)) {
            String imgPath = Constant.BASE_IMG_PATH + "tarkov_map/" + "子弹数据.png";
            OneBotMedia media = OneBotMedia.builder().file("http://localhost:8081/getImage?path=" + imgPath).cache(false);
            MsgUtils msg = MsgUtils.builder().img(media);
            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        } else if (event.getRawMessage().equals(CmdConst.UPDATE_ZI_DAN)) {
            Page page = PuppeteerUtil.getBrowser().newPage();
            try {
                MsgUtils msg = MsgUtils.builder().text("开始更新子弹数据，大约需要60秒。");
                bot.sendMsg(event, msg.build(), false);
                PageNavigateOptions pageNavigateOptions = new PageNavigateOptions();
                pageNavigateOptions.setTimeout(120000);
                pageNavigateOptions.setWaitUntil(List.of("domcontentloaded"));
                Viewport viewport = new Viewport();
                viewport.setWidth(1650);
                viewport.setHeight(12000);
                page.setViewport(viewport);
                page.goTo("https://escapefromtarkov.fandom.com/wiki/Ballistics", pageNavigateOptions, true);
//                StyleTagOptions styleTagOptions = new StyleTagOptions(null, null, "#WikiaBar {visibility: hidden !important;} .fandom-sticky-header.is-visible {visibility: hidden !important;} .notifications-placeholder {visibility: hidden !important;} .global-navigation {visibility: hidden !important;}");
//                page.addStyleTag(styleTagOptions);
                ElementHandle b = page.$("div._2O--J403t2VqCuF8XJAZLK");
                if (b != null) {
                    b.click();
                }
                ElementHandle elementHandle1 = page.$("table.wikitable.sortable.stickyheader");
                ScreenshotOptions screenshotOptions = new ScreenshotOptions();
                String imgPath = Constant.BASE_IMG_PATH + "tarkov_map/" + "子弹数据.png";
                screenshotOptions.setPath(imgPath);
                elementHandle1.screenshot(screenshotOptions, false);
            } catch (Exception e) {
                log.error("子弹数据更新失败", e);
                MsgUtils msg = MsgUtils.builder().text("子弹数据更新失败" + e.getMessage());
                bot.sendMsg(event, msg.build(), false);
                return MESSAGE_BLOCK;
            } finally {
                try {
                    page.close();
                } catch (InterruptedException e) {
                    log.error("页面关闭失败", e);
                }
            }
            String imgPath = Constant.BASE_IMG_PATH + "tarkov_map/" + "子弹数据.png";
            OneBotMedia media = OneBotMedia.builder().file("http://localhost:8081/getImage?path=" + imgPath).cache(false);
            MsgUtils msg = MsgUtils.builder().img(media).text("子弹数据更新成功");
            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        } else if (event.getRawMessage().contains("在哪")) {
            if (event.getRawMessage().contains("三兄弟") || event.getRawMessage().contains("三狗")) {
                ThreeDog threeDog = (ThreeDog) Constant.CONFIG_CACHE.get(RedisKey.THREE_DOG);
                if (threeDog != null) {
                    MsgUtils msg = MsgUtils.builder().text(threeDog.getLocation() + Constant.XN).text(threeDog.getLastReported());
                    bot.sendMsg(event, msg.build(), false);
                }
            }
            return MESSAGE_BLOCK;
        } else if (event.getRawMessage().startsWith("任务流程图")) {
            String imgPath = Constant.BASE_IMG_PATH + "tarkov_map/任务流程.jpg";
            OneBotMedia media = OneBotMedia.builder().file("http://localhost:8081/getJpgImage?path=" + imgPath).cache(false);
            MsgUtils msg = MsgUtils.builder().img(media);
            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        } else if (event.getRawMessage().startsWith("塔科夫服务器")) {
            Map<String, Object> serverInfo = CastUtils.cast(Constant.CONFIG_CACHE.get(Constant.TKF_SERVER_INFO));
            String content = (String) serverInfo.get("content");
            MsgUtils msg = MsgUtils.builder().text(content);
            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }


}
