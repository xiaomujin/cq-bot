package com.kuroneko.cqbot.annoPlugin;

import cn.hutool.core.util.ObjUtil;
import com.kuroneko.cqbot.dto.AntiBiliMiniAppDTO;
import com.kuroneko.cqbot.enums.Regex;
import com.kuroneko.cqbot.exception.BotException;
import com.kuroneko.cqbot.exception.ExceptionHandler;
import com.kuroneko.cqbot.service.BiLiService;
import com.kuroneko.cqbot.utils.HttpUtil;
import com.kuroneko.cqbot.utils.JsonUtil;
import com.kuroneko.cqbot.utils.NumberFormatter;
import com.kuroneko.cqbot.utils.RegexUtil;
import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.apache.commons.lang3.time.DateFormatUtils;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Shiro
@Slf4j
@Component
@RequiredArgsConstructor
public class BLPlugin {
    private final RestTemplate restTemplate;
    private final BiLiService biLiService;
    private final ExpiringMap<Long, String> expiringMap = ExpiringMap.builder()
            .variableExpiration()
            .expirationPolicy(ExpirationPolicy.CREATED)
            .expiration(300 * 1000L, TimeUnit.MILLISECONDS)
            .build();

    @AnyMessageHandler
    public void handler(Bot bot, AnyMessageEvent event) {
        ExceptionHandler.with(bot, event, () -> {
            String msg = event.getMessage();
            Long id = ObjUtil.defaultIfNull(event.getGroupId(), event.getUserId());
            if (msg.contains("com.tencent.miniapp_01") && msg.contains("哔哩哔哩")) {
                // qq json卡片
                handleMiniApp(bot, event, id);
            } else if (msg.contains("bilibili.com/")) {
                handleBiliUrl(bot, event, id, event.getMessage());
            } else if (msg.contains("b23.tv/")) {
                handleShortURL(bot, event, id);
            }
            return "";
        });

    }

    private void handleBiliUrl(Bot bot, AnyMessageEvent event, Long id, String msg) {
        handleURL(bot, event, id, msg);
    }


    private void handleMiniApp(Bot bot, AnyMessageEvent event, Long id) {
        List<String> json = event.getArrayMsg().stream()
                .filter(it -> MsgTypeEnum.json == it.getType())
                .map(it -> it.getStringData("data"))
                .toList();
        if (!json.isEmpty()) {
            JsonNode jsonNode = JsonUtil.toNode(json.get(0));
            String url = jsonNode.get("meta").get("detail_1").get("qqdocurl").asString();
            String redirect = HttpUtil.getRedirect(url);
            handleURL(bot, event, id, redirect);
        }
    }

    private void handleShortURL(Bot bot, AnyMessageEvent event, Long id) {
        String sUrl = RegexUtil.group("sUrl", event.getMessage(), Regex.BILIBILI_SHORT_URL).orElseThrow(() -> new BotException("解析失败3"));
        String redirect = HttpUtil.getRedirect("https://" + sUrl);
        handleURL(bot, event, id, redirect);
    }

    /**
     * https://t.bilibili.com/1151722276789420041
     * https://www.bilibili.com/opus/1158188375814963207
     * https://www.bilibili.com/read/cv19282068
     * https://www.bilibili.com/video/BV1mokxBtEZh
     */
    private void handleURL(Bot bot, AnyMessageEvent event, Long id, String url) {
        // 从文本中提取链接
        url = RegexUtil.group("sUrl", url, Regex.BILIBILI_URL).orElseThrow(() -> new BotException("解析失败0"));
        url = "https://" + url;
        if (url.contains("/read")) {
            url = HttpUtil.getRedirect(url);
        }
        String substring = url.substring(url.lastIndexOf("/") + 1);
        if (substring.contains("BV")) {
            handleRequest(bot, event, id, substring);
            return;
        }
        String path = biLiService.getNewScreenshot(substring, null);
        MsgUtils msg = biLiService.buildDynamicMsgLess(path, substring);
        bot.sendMsg(event, msg.build(), false);
    }

    private void handleRequest(Bot bot, AnyMessageEvent event, Long id, String bid) {
        String oldBid = expiringMap.get(id);
        if (Objects.equals(oldBid, bid)) {
            return;
        }
        expiringMap.put(id, bid);
        AntiBiliMiniAppDTO request = request(bid);
        bot.sendMsg(event, buildMsg(request.getData()), false);
    }

    public AntiBiliMiniAppDTO request(String bid) {
        AntiBiliMiniAppDTO data = restTemplate.getForObject("https://api.bilibili.com/x/web-interface/view?bvid=" + bid, AntiBiliMiniAppDTO.class);
        if (data == null) {
            throw new BotException("与世界失去同步");
        }
        if (data.getCode() != 0) {
            throw new BotException(data.getMessage());
        }
        return data;
    }

    private String buildMsg(AntiBiliMiniAppDTO.BLData data) {
        Long pubdate = data.getPubdate();
        String pubdateStr = "";
        if (pubdate != null) {
            String format = DateFormatUtils.format(pubdate * 1000L, "yyyy-MM-dd HH:mm:ss");
            pubdateStr = "\n" + format;
        }
        return MsgUtils.builder()
                .img(data.getPic())
                .text("\n" + ShiroUtils.escape2(data.getTitle()))
                .text("\nUP：" + ShiroUtils.escape2(data.getOwner().getName()))
                .text("\n播放：" + NumberFormatter.formatNumberWithUnit(data.getStat().getView(), 1) + " 弹幕：" + NumberFormatter.formatNumberWithUnit(data.getStat().getDanmaku(), 1))
                .text("\n投币：" + NumberFormatter.formatNumberWithUnit(data.getStat().getCoin(), 1) + " 点赞：" + NumberFormatter.formatNumberWithUnit(data.getStat().getLike(), 1))
                .text("\n评论：" + NumberFormatter.formatNumberWithUnit(data.getStat().getReply(), 1) + " 分享：" + NumberFormatter.formatNumberWithUnit(data.getStat().getShare(), 1))
                .text(pubdateStr)
                .text("\nav" + data.getStat().getAid())
                .text("\nhttps://www.bilibili.com/video/" + data.getBvid())
                .build();
    }

}
