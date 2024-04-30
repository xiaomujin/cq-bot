package com.kuroneko.cqbot.annoPlugin;

import cn.hutool.core.lang.Pair;
import com.alibaba.fastjson2.JSON;
import com.kuroneko.cqbot.dto.SauceDTO;
import com.kuroneko.cqbot.dto.SearchMode;
import com.kuroneko.cqbot.enums.Regex;
import com.kuroneko.cqbot.exception.BotException;
import com.kuroneko.cqbot.exception.ExceptionHandler;
import com.kuroneko.cqbot.utils.HttpUtil;
import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.ArrayMsgUtils;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URLEncoder;
import java.nio.charset.Charset;
import java.util.List;
import java.util.regex.Matcher;

@Shiro
@Slf4j
@Component
@RequiredArgsConstructor
public class PicSearchPlugin {
    private final ModePlugin modePlugin;
    private static final String sauceNaoKey = "a4aa702c9ee5f0f66a7f0f4846a7a77c5a1f5ec2";

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = Regex.PIC_SEARCH, at = AtEnum.BOTH)
    public void handler(Bot bot, AnyMessageEvent event, Matcher matcher) {
        log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), Regex.PIC_SEARCH);
        ExceptionHandler.with(bot, event, () -> {
            modePlugin.setSearchMode("搜图", event.getUserId(), event.getGroupId(), bot, "");
            return "";
        });
    }

    @AnyMessageHandler
    public void searchPic(Bot bot, AnyMessageEvent event) {
        if (!modePlugin.check("搜图", event.getUserId(), event.getGroupId())) {
            return;
        }
        ExceptionHandler.with(bot, event, () -> {
            List<ArrayMsg> list = event.getArrayMsg().stream()
                    .filter(msg -> msg.getType() == MsgTypeEnum.image)
                    .toList();
            if (list.isEmpty()) {
                return "";
            }
            modePlugin.resetExpiration(event.getUserId(), event.getGroupId());
            bot.sendMsg(event, MsgUtils.builder().reply(event.getMessageId()).text("收到了，搜索中……").build(), false);
            String url = list.getFirst().getData().get("url");
            Pair<String, MsgUtils> sauceRes = getSauceRes(url);
            MsgUtils sauceResValue = sauceRes.getValue();
            if (sauceResValue != null) {
                bot.sendMsg(event, sauceResValue.reply(event.getMessageId()).build(), false);
            }
            if (Double.parseDouble(sauceRes.getKey()) > 75) {
                return "";
            }
            bot.sendMsg(event, MsgUtils.builder().reply(event.getMessageId()).text("检索结果相似度较低，正在使用Ascii2d进行检索···").build(), false);
            Pair<MsgUtils, MsgUtils> ascii2d = getAscii2d(url);
            // Ascii2d 色合検索
            bot.sendMsg(event, ascii2d.getKey().reply(event.getMessageId()).build(), false);
            // Ascii2d 特徴検索
            bot.sendMsg(event, ascii2d.getValue().reply(event.getMessageId()).build(), false);
            return "";
        });
    }

    private Pair<MsgUtils, MsgUtils> getAscii2d(String url) {
        String redirect = HttpUtil.getRedirect(STR."https://ascii2d.net/search/url/\{url}");
        try {
            MsgUtils colorSearchResult = ascii2dRequest(0, redirect);
            MsgUtils bovwSearchResult = ascii2dRequest(1, redirect.replace("/color/", "/bovw/"));
            return Pair.of(colorSearchResult, bovwSearchResult);
        } catch (IOException e) {
            throw new BotException(e.getMessage());
        }
    }

    private MsgUtils ascii2dRequest(Integer type, String resultUrl) throws IOException {
        Connection connect = Jsoup.connect(resultUrl);
        Connection header = connect.header("User-Agent", "PostmanRuntime/7.29.0");
        Document document = header.get();

        Element itemBox = document.getElementsByClass("item-box").get(1);
        Elements thumbnail = itemBox.select("div.image-box > img");
        Element link = itemBox.select("div.detail-box > h6 > a").get(0);
        Element author = itemBox.select("div.detail-box > h6 > a").get(1);
        return MsgUtils
                .builder()
                .img(thumbnail.attr("abs:src"))
                .text(STR."\n标题：\{link.text()}")
                .text(STR."\n作者：\{author.text()}")
                .text(STR."\n链接：\{link.attr("abs:href")}")
                .text(STR."\n数据来源：Ascii2d \{type == 0 ? "色合検索" : "特徴検索"}");
    }

    private Pair<String, MsgUtils> getSauceRes(String url) {
        SauceDTO request = sauceRequest(url);
        List<SauceDTO.Result> results = request.getResults();
        if (results.isEmpty()) {
            return Pair.of("0", null);
        }
        SauceDTO.Result first = results.getFirst();
        SauceDTO.ResultHeader header = first.getHeader();
        SauceDTO.ResultData data = first.getData();
        MsgUtils msgUtils = MsgUtils.builder()
                .img(header.getThumbnail())
                .text(STR."\n相似度：\{header.getSimilarity()}%");
        switch (header.getIndexId()) {
            case 5 -> {
                msgUtils.text(STR."\n标题：\{data.getTitle()}");
                msgUtils.text(STR."\n画师：\{data.getAuthorName()}");
                msgUtils.text(STR."\n作品主页：https://pixiv.net/i/\{data.getPixivId()}");
                msgUtils.text(STR."\n画师主页：https://pixiv.net/u/\{data.getAuthorId()}");
                msgUtils.text(STR."\n反代地址：https://i.loli.best/\{data.getPixivId()}");
                msgUtils.text("\n数据来源：SauceNao (Pixiv)");
            }
            case 41 -> {
                msgUtils.text(STR."\n链接：\{data.getExtUrls().getFirst()}");
                msgUtils.text("\n用户：" + STR."https://twitter.com/\{data.getTwitterUserHandle()}");
                msgUtils.text("\n数据来源：SauceNao (Twitter)");
            }
            case 18, 38 -> {
                msgUtils.text(STR."\n来源：\{data.getSource()}");
                msgUtils.text(STR."\n日文名：\{data.getJpName()}");
                msgUtils.text(STR."\n英文名：\{data.getEngName()}");
                msgUtils.text("\n数据来源：SauceNao (H-Misc)");
            }
            default -> {
                List<String> extUrls = data.getExtUrls();
                if (extUrls != null && !extUrls.isEmpty()) {
                    msgUtils.text(STR."\n链接：\{extUrls.getFirst()}");
                }
                msgUtils.text("\n数据来源：SauceNao");
                msgUtils.text(STR."\n数据来源：SauceNao(\{header.getIndexId()})");
            }
        }
        return Pair.of(header.getSimilarity(), msgUtils);
    }

    @Synchronized
    private SauceDTO sauceRequest(String img) {
        try {
            String api = STR."https://saucenao.com/search.php?api_key=\{sauceNaoKey}&output_type=2&numres=3&db=999&url=\{URLEncoder.encode(img, Charset.defaultCharset())}";
            String resStr = HttpUtil.get(api);
            SauceDTO sauceDTO = JSON.parseObject(resStr, SauceDTO.class);
            if (sauceDTO.getHeader().getLongRemaining() <= 0) throw new BotException("今日的搜索配额已耗尽啦");
            if (sauceDTO.getHeader().getShortRemaining() <= 0) throw new BotException("短时间内搜索配额已耗尽啦");
            if (sauceDTO.getResults() == null || sauceDTO.getResults().isEmpty())
                throw new BotException("服务出现问题或未能找到相似的内容");
            return sauceDTO;
        } catch (Exception e) {
            log.error(e.getMessage(), e);
            SauceDTO sauceDTO = new SauceDTO();
            sauceDTO.setResults(List.of());
            return sauceDTO;
//            throw new BotException(STR."SauceNao数据获取异常：\{e.getMessage()}");
        }
    }

    @AnyMessageHandler()
    @MessageHandlerFilter(cmd = Regex.PIC_TEXT, at = AtEnum.BOTH)
    public void picTextMode(Bot bot, AnyMessageEvent event, Matcher matcher) {
        log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), Regex.PIC_TEXT);
        ExceptionHandler.with(bot, event, () -> {
            String text = matcher.group("text").trim();
            if (text.isEmpty()) {
                return "";
            }
            modePlugin.setSearchMode("图语", event.getUserId(), event.getGroupId(), bot, text);
            return "";
        });
    }

    @AnyMessageHandler
    public void picText(Bot bot, AnyMessageEvent event) {
        if (!modePlugin.check("图语", event.getUserId(), event.getGroupId())) {
            return;
        }
        SearchMode searchMode = modePlugin.getSearchMode(event.getUserId(), event.getGroupId());
        ExceptionHandler.with(bot, event, () -> {
            List<ArrayMsg> list = event.getArrayMsg().stream()
                    .filter(msg -> msg.getType() == MsgTypeEnum.image)
                    .toList();
            if (list.isEmpty()) {
                return "";
            }
            String url = list.getFirst().getData().get("url");
            OneBotMedia summary = OneBotMedia.builder().file(url).summary(searchMode.getExt());
            List<ArrayMsg> arrayMsgs = ArrayMsgUtils.builder().img(summary).build();
            modePlugin.removeNow(event.getUserId(), event.getGroupId());
            bot.sendMsg(event, arrayMsgs, false);
            return null;
        });
    }
}

