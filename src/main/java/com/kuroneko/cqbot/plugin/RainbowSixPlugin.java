package com.kuroneko.cqbot.plugin;

import cn.hutool.core.io.FileUtil;
import cn.hutool.core.util.StrUtil;
import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.utils.BotUtil;
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

import java.util.List;

@Slf4j
@Component
public class RainbowSixPlugin extends BotPlugin {
    private static final String CMD = CmdConst.RAINBOW_KD;

    String imgPath = STR."\{Constant.BASE_IMG_PATH}r6/";

    public RainbowSixPlugin() {
        FileUtil.mkdir(imgPath);
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
            List<String> params = BotUtil.getParams(CMD, event.getRawMessage());
            MsgUtils msg = getMsg(params, event.getUserId());

            bot.sendGroupMsg(event.getGroupId(), msg.build(), false);
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }


    private MsgUtils getMsg(List<String> text, Long qq) {
        MsgUtils msg = MsgUtils.builder();
        int min = Math.min(text.size(), 2);
        String name = null;
        String select = null;
        switch (min) {
            case 2:
                select = text.get(1);
            case 1:
                name = text.getFirst();
        }
        if (StrUtil.isEmpty(name)) {
            msg.text("请输入需要查询昵称");
            return msg;
        }

        Page page = PuppeteerUtil.getBrowser().newPage();
        try {
            PageNavigateOptions pageNavigateOptions = new PageNavigateOptions();
            pageNavigateOptions.setTimeout(30000);
            pageNavigateOptions.setWaitUntil(List.of("domcontentloaded"));
            Viewport viewport = new Viewport();
            viewport.setWidth(1000);
            page.setViewport(viewport);
            page.goTo(STR."https://r6.tracker.network/profile/pc/\{name}", pageNavigateOptions, true);
            StyleTagOptions styleTagOptions = new StyleTagOptions(null, null, "#trn-site-nav {display:none} .trn-gamebar{display:none}");
            page.addStyleTag(styleTagOptions);
            ElementHandle elementHandle = page.$("#profile > div.trn-scont.trn-scont--swap > div.trn-scont__content");
            if (elementHandle == null) {
                msg.text("没有查到 ").text(name);
                return msg;
            }

            String nameFun = STR."""
                        ()=>{
                            let name = document.querySelectorAll(".trn-card__header .trn-card__header-subline")[0]
                            name.textContent = `\{name}     |    ${name.textContent}`
                            return true;
                        }
                        """;
            page.waitForFunction(nameFun);

            if (select != null && (select.contains("s") || select.contains("赛季"))) {
                String fun = """
                        ()=>{
                            document.querySelectorAll("div.trn-card__header li")[0].click()
                            return true;
                        }
                        """;
                page.waitForFunction(fun);
            }

            ScreenshotOptions screenshotOptions = new ScreenshotOptions();
            String img = STR."\{imgPath}\{name}.png";
            screenshotOptions.setPath(img);
            elementHandle.screenshot(screenshotOptions);
            msg.img(BotUtil.getLocalMedia(img));
        } catch (Exception e) {
            msg.text(e.getMessage());
            log.error("彩6战绩查询异常：", e);
        } finally {
            PuppeteerUtil.safeClosePage(page);
        }
        return msg;
    }

}
