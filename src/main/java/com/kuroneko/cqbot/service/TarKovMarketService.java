package com.kuroneko.cqbot.service;

import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.constant.RedisKey;
import com.kuroneko.cqbot.utils.JsonUtil;
import com.kuroneko.cqbot.utils.RedisUtil;
import com.kuroneko.cqbot.vo.TarKovMarketVo;
import com.kuroneko.cqbot.vo.tarkov.TarKovRVo;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.BotContainer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.data.util.CastUtils;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Service
@AllArgsConstructor
public class TarKovMarketService {

    private final RestTemplate restTemplate;
    private final BotContainer botContainer;
    private final RedisUtil redisUtil;

    public List<TarKovMarketVo> search(String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "cf_clearance=CVzPv4LT32X5WSW0QGJQXrdYI.rT.lo5y4rgxJ1Xi1U-1679205658-0-160; __cf_bm=kjMixk_YXW3S_0NXmwKAwi3lcQ0U_TLOFksVyKb1X6o-1679205665-0-AU8HOoHP/GzAjyCTvPIo8I+x8wAyUrrD8P5CYU4bp7HfQIiBK8Lp8yl+yv4rmp6XmkOcK3riU9NiOK8y2Qhh6wMDfbug2zgwo7k26xbv2uVd8liw4/TqQloFlaCuob+jg7PnMjiVWym1DO16vJWTEk3cKas8M7b34qQvljnHWiJk; tm_locale=cn; HCLBSTICKY=9002f577e4f8d278d30e4c50321e2c20|ZBalV|ZBalH");
        headers.add("referer", "https://tarkov-market.com/");
//        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.add("user-agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/111.0.0.0 Safari/537.36");
        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
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

    public Map<String, Object> getTkfServerInfo() {
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "auth=2308a45e7dd03b037c934dd8a3f4c9d91d42f77548e28f2a907b49ef91aee66b9728623-1685201483; PHPSESSID=hc88kfhp76a44rgmua6j59b9rb; _csrf_chuanyu=HR0GUBYNKMDgRHxlawz0qN_0QFEC-YHw");
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<String> httpEntity = new HttpEntity<>(null, headers);
        ResponseEntity<List<HashMap<String, Object>>> re = restTemplate.exchange(
                "https://www.kookapp.cn/api/v2/messages/1103179288889105?page_size=50",
                HttpMethod.GET,
                httpEntity,
                new ParameterizedTypeReference<>() {
                }
        );
        if (re.getStatusCode().is2xxSuccessful()) {
            HashMap<String, Object> map = re.getBody().get(0);
            return map;
        }
        return Map.of();
    }

    public Map<String, Object> cacheTkfServerInfo() {
        Map<String, Object> info = getTkfServerInfo();
        if (info.isEmpty()) {
            return Map.of();
        }
        Map<String, Object> oldInfo = CastUtils.cast(Constant.CONFIG_CACHE.get(Constant.TKF_SERVER_INFO));
        Constant.CONFIG_CACHE.put(Constant.TKF_SERVER_INFO, info);
        //推送
        if (oldInfo != null && !info.get("updated_at").equals(oldInfo.get("updated_at"))) {
            pushTkfServerInfo(info);
        }
        return info;
    }

    private void pushTkfServerInfo(Map<String, Object> info) {
        String content = (String) info.get("content");
        MsgUtils msg = MsgUtils.builder().text(content);
        Set<Number> list = redisUtil.members(RedisKey.TKF_INFO);
        log.info("TKF_INFO 推送群聊：{}", list);
        if (!list.isEmpty()) {
            botContainer.robots.forEach((id, bot) -> {
                list.forEach(groupId -> {
                    bot.sendGroupMsg(groupId.longValue(), msg.build(), false);
                    try {
                        Thread.sleep(3600);
                    } catch (InterruptedException e) {
                        log.error("sleep err", e);
                    }
                });
            });

        }
    }
}
