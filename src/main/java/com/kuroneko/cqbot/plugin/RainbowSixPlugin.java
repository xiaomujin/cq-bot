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


    public MsgUtils getMsg(List<String> text, Long qq) {
        MsgUtils msg = MsgUtils.builder();
        int min = Math.min(text.size(), 2);
        String name = null;
        String selected = "overview";
        switch (min) {
            case 2:
                String select = text.get(1);
                if (select.contains("1") || select.contains("o") || select.contains("总览")) {
                    selected = "overview";
                } else if (select.contains("2") || select.contains("m") || select.contains("匹配")) {
                    selected = "matches";
                } else if (select.contains("3") || select.contains("s") || select.contains("赛季")) {
                    selected = "seasons?playlist=pvp_casual&page=1";
                } else if (select.contains("4") || select.contains("e") || select.contains("见过")) {
                    selected = "encounters";
                } else if (select.contains("5") || select.contains("t") || select.contains("趋势")) {
                    selected = "trends?playlist=all";
                }
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
            pageNavigateOptions.setTimeout(20000);
            pageNavigateOptions.setWaitUntil(List.of("domcontentloaded"));
            Viewport viewport = new Viewport();
            viewport.setWidth(1455);
            page.setViewport(viewport);
            page.goTo(STR."https://r6.tracker.network/r6siege/profile/ubi/\{name}/\{selected}", pageNavigateOptions, true);
            StyleTagOptions styleTagOptions = new StyleTagOptions(null, null, ".bordered-ad {display:none} .primisslate {display:none}");
            page.addStyleTag(styleTagOptions);

            BotUtil.sleep(500L);

            ElementHandle elementHandle = page.$("#app > div.trn-wrapper > div.trn-container > div > main > div.content.content--error");
            if (elementHandle != null) {
                msg.text("没有查到 ").text(name);
                return msg;
            }
            elementHandle = page.$("#app > div.trn-wrapper > div.trn-container > div > main");

//            String nameFun = STR."""
//                        ()=>{
//                            let name = document.querySelectorAll(".trn-card__header .trn-card__header-subline")[0]
//                            name.textContent = `\{name}     |    ${name.textContent}`
//                            return true;
//                        }
//                        """;
//            page.waitForFunction(nameFun);

//            if (selectNum != null) {
//                String fun = STR."""
//                        ()=>{
//                            document.querySelectorAll("#app > div.trn-wrapper > div.trn-container > div > main > div.content.no-card-margin > div.ph > div.ph-nav > ul > li > a")[\{selectNum}].click()
//                            return true;
//                        }
//                        """;
//                page.waitForFunction(fun);
//            }

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
