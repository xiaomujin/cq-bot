package com.kuroneko.cqbot.plugin;

import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.utils.PuppeteerUtil;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.ruiyun.jvppeteer.core.page.ElementHandle;
import com.ruiyun.jvppeteer.core.page.Page;
import com.ruiyun.jvppeteer.options.PageNavigateOptions;
import com.ruiyun.jvppeteer.options.ScreenshotOptions;
import com.ruiyun.jvppeteer.options.StyleTagOptions;
import com.ruiyun.jvppeteer.options.Viewport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.File;
import java.util.List;

@Slf4j
@Component
public class RainbowSixPlugin extends BotPlugin {
    private static final String CMD = CmdConst.RAINBOW_KD;

    String imgPath = Constant.BASE_IMG_PATH + "r6/";

    public RainbowSixPlugin() {
        File file = new File(imgPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }


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
        if (event.getRawMessage().startsWith(CMD.toLowerCase()) || event.getRawMessage().startsWith(CMD.toUpperCase())) {
            log.info("qq：{} 请求 {}", event.getUserId(), CMD);

            String text = getText(event.getRawMessage());
            MsgUtils msg = getMsg(text, event.getUserId());

            bot.sendGroupMsg(event.getGroupId(), msg.build(), false);
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }


    private MsgUtils getMsg(String text, Long qq) {
        MsgUtils msg = MsgUtils.builder();
        Page page = PuppeteerUtil.getBrowser().newPage();
        try {
            PageNavigateOptions pageNavigateOptions = new PageNavigateOptions();
            pageNavigateOptions.setTimeout(30000);
            pageNavigateOptions.setWaitUntil(List.of("domcontentloaded"));
            Viewport viewport = new Viewport();
            viewport.setWidth(2000);
            page.setViewport(viewport);
            page.goTo("https://r6.tracker.network/profile/pc/" + text, pageNavigateOptions, true);
            StyleTagOptions styleTagOptions = new StyleTagOptions(null, null, "#trn-site-nav {display:none} .trn-gamebar{display:none}");
            page.addStyleTag(styleTagOptions);
            ElementHandle elementHandle = page.$("#profile > div.trn-scont.trn-scont--swap > div.trn-scont__content");
            if (elementHandle == null) {
                msg.text("没有查到 ").text(text);
                return msg;
            }
            ScreenshotOptions screenshotOptions = new ScreenshotOptions();
            String img = imgPath + text + ".png";
            screenshotOptions.setPath(img);
            elementHandle.screenshot(screenshotOptions);
            msg.img("http://localhost:8081/getImage?path=" + img);
        } catch (Exception e) {
            msg.text(e.getMessage());
            log.error("彩6战绩查询异常：", e);
        } finally {
            try {
                page.close();
            } catch (InterruptedException e) {
                log.error("page关闭失败", e);
                PuppeteerUtil.close();
            }
        }
        return msg;
    }

}
