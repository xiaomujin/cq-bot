package com.kuroneko.cqbot.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.constant.RedisKey;
import com.kuroneko.cqbot.enums.Regex;
import com.kuroneko.cqbot.utils.*;
import com.kuroneko.cqbot.vo.TarKovMarketVo;
import com.kuroneko.cqbot.vo.tarkov.TarKovRVo;
import com.mikuac.shiro.common.utils.MsgUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.util.ObjectUtils;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class TarKovMarketService {

    private final RestTemplate restTemplate;
    private final RedisUtil redisUtil;

    public List<TarKovMarketVo> search(String text) {
        HttpEntity<String> httpEntity = buildHttpEntity();
        ResponseEntity<TarKovRVo> entity = restTemplate.exchange(
                Constant.TAR_KOV_MARKET_URL,
                HttpMethod.GET,
                httpEntity,
                TarKovRVo.class,
                text
        );

        if (!entity.getStatusCode().is2xxSuccessful()) {
            return List.of();
        }
        //解密
        TarKovRVo encode = entity.getBody();
        assert encode != null;
        String items = encode.getItems();
        if (items.length() < 18) {
            return List.of();
        }
        String s1 = items.substring(0, 5);
        String s2 = items.substring(10);
        String urlJson = new String(Base64.getDecoder().decode(s1 + s2));
        String json = URLDecoder.decode(urlJson, StandardCharsets.UTF_8);
        return JsonUtil.toList(json, TarKovMarketVo.class);
    }

    private static HttpEntity<String> buildHttpEntity() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "cf_clearance=CVzPv4LT32X5WSW0QGJQXrdYI.rT.lo5y4rgxJ1Xi1U-1679205658-0-160; __cf_bm=kjMixk_YXW3S_0NXmwKAwi3lcQ0U_TLOFksVyKb1X6o-1679205665-0-AU8HOoHP/GzAjyCTvPIo8I+x8wAyUrrD8P5CYU4bp7HfQIiBK8Lp8yl+yv4rmp6XmkOcK3riU9NiOK8y2Qhh6wMDfbug2zgwo7k26xbv2uVd8liw4/TqQloFlaCuob+jg7PnMjiVWym1DO16vJWTEk3cKas8M7b34qQvljnHWiJk; tm_locale=cn; HCLBSTICKY=9002f577e4f8d278d30e4c50321e2c20|ZBalV|ZBalH");
        headers.add("referer", "https://tarkov-market.com/");
//        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36");
        return new HttpEntity<>(null, headers);
    }

    public String getTkfServerStatusMsg() {
        String respServices = HttpUtil.get("https://status.escapefromtarkov.com/api/services");
        JSONArray jsonArray = JSON.parseArray(respServices);
        StringBuilder sb = new StringBuilder();
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        String formatTime = LocalDateTime.now().format(formatter);
        sb.append(STR."\{formatTime}\n");
        sb.append("服务器状态速报：\n");
        jsonArray.forEach(item -> {
            JSONObject jsonObject = (JSONObject) item;
            String name = jsonObject.getString("name");
            int status = jsonObject.getIntValue("status");
            switch (name) {
                case "Website" -> sb.append("游戏官网：");
                case "Forum" -> sb.append("官方论坛：");
                case "Authentication" -> sb.append("身份认证：");
                case "Launcher" -> sb.append("   启动器：");
                case "Group lobby" -> sb.append("组队功能：");
                case "Trading" -> sb.append("交易功能：");
                case "Matchmaking" -> sb.append("战局匹配：");
                case "Friends and msg." -> sb.append("好友消息：");
                case "Inventory operations" -> sb.append("库存操作：");
                default -> sb.append(name);
            }
            sb.append(STR."\{getCNStatus(status)}\n");
        });
        String respGlobal = HttpUtil.get("https://status.escapefromtarkov.com/api/global/status");
        JSONObject jsonObject = JSON.parseObject(respGlobal);
        int status = jsonObject.getIntValue("status");
        sb.append(STR."\n总体状态：\{getCNStatus(status)}\n");
        String message = jsonObject.getString("message");
        if (!ObjectUtils.isEmpty(message)) {
            sb.append(STR."信息：\{message}");
        }
        return MsgUtils.builder().text(sb.toString()).build();
    }

    private String getCNStatus(int status) {
        return switch (status) {
            case 0 -> "\uD83D\uDFE2服务正常";
            case 1 -> "⚙️正在更新";
            case 2 -> "\uD83D\uDFE1部分故障";
            case 3 -> "\uD83D\uDD34服务不可用";
            default -> "⚪未知";
        };
    }

    public void cacheTkfServerStatusMsg() {
        Collection<String> msg = CacheUtil.get(Regex.TKF_SERVER_INFO);
        String tkfServerStatusMsg = getTkfServerStatusMsg();
        CacheUtil.put(Regex.TKF_SERVER_INFO, tkfServerStatusMsg, 20, TimeUnit.MINUTES);
        //推送
        if (msg == null) {
            return;
        }
        String old = msg.iterator().next().substring(20);
        String now = tkfServerStatusMsg.substring(20);
        if (!old.equals(now)) {
            pushTkfServerStatus(tkfServerStatusMsg);
        }
    }

    private void pushTkfServerStatus(String msg) {
        Set<Number> list = redisUtil.members(RedisKey.TKF_INFO);
        BotUtil.sendToGroupList(list, msg);
    }
}
