package com.kuroneko.cqbot.annoPlugin;

import cn.hutool.core.util.ObjUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.kuroneko.cqbot.dto.AntiBiliMiniAppDTO;
import com.kuroneko.cqbot.enums.Regex;
import com.kuroneko.cqbot.exception.BotException;
import com.kuroneko.cqbot.exception.ExceptionHandler;
import com.kuroneko.cqbot.utils.HttpUtil;
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
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;

@Shiro
@Slf4j
@Component
@RequiredArgsConstructor
public class BLPlugin {
    private final RestTemplate restTemplate;
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
                handleMiniApp(bot, event, id);
            } else if (msg.contains("bilibili.com/video/BV")) {
                handleURL(bot, event, id);
            } else if (msg.contains("b23.tv/")) {
                handleShortURL(bot, event, id);
            }

            return "";
        });

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

    public String parseBidByShortURL(String url) {
        String redirect = HttpUtil.getRedirect(url);
        return RegexUtil.group("BVId", redirect, Regex.BILIBILI_BID).orElseThrow(() -> new BotException("解析失败1"));
    }

    private String buildMsg(AntiBiliMiniAppDTO.BLData data) {
        return MsgUtils.builder()
                .img(data.getPic())
                .text("\n" + ShiroUtils.escape2(data.getTitle()))
                .text("\nUP：" + ShiroUtils.escape2(data.getOwner().getName()))
                .text("\n播放：" + data.getStat().getView() + " 弹幕：" + data.getStat().getDanmaku())
                .text("\n投币：" + data.getStat().getCoin() + " 点赞：" + data.getStat().getLike())
                .text("\n评论：" + data.getStat().getReply() + " 分享：" + data.getStat().getShare())
                .text("\nav" + data.getStat().getAid())
                .text("\nhttps://www.bilibili.com/video/" + data.getBvid())
                .build();
    }

    private void handleMiniApp(Bot bot, AnyMessageEvent event, Long id) {
        List<String> json = event.getArrayMsg().stream()
                .filter(it -> MsgTypeEnum.json == it.getType())
                .map(it -> it.getData().get("data"))
                .toList();
        if (!json.isEmpty()) {
            JSONObject jsonObject = JSON.parseObject(json.get(0));
            String url = jsonObject.getJSONObject("meta").getJSONObject("detail_1").getString("qqdocurl");
            handleRequest(bot, event, id, parseBidByShortURL(url));
        }
    }

    private void handleURL(Bot bot, AnyMessageEvent event, Long id) {
        handleRequest(bot, event, id, RegexUtil.group("BVId", event.getMessage(), Regex.BILIBILI_BID).orElseThrow(() -> new BotException("解析失败2")));
    }

    private void handleShortURL(Bot bot, AnyMessageEvent event, Long id) {
        String sUrl = RegexUtil.group("sUrl", event.getMessage(), Regex.BILIBILI_SHORT_URL).orElseThrow(() -> new BotException("解析失败3"));
        handleRequest(bot, event, id, parseBidByShortURL("https://" + sUrl));
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
}
