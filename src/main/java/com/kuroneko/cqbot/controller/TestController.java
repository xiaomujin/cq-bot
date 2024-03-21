package com.kuroneko.cqbot.controller;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.constant.RedisKey;
import com.kuroneko.cqbot.enums.Regex;
import com.kuroneko.cqbot.event.BiliSubscribeEvent;
import com.kuroneko.cqbot.exception.BotException;
import com.kuroneko.cqbot.handler.ApplicationContextHandler;
import com.kuroneko.cqbot.service.AiService;
import com.kuroneko.cqbot.service.BiLiService;
import com.kuroneko.cqbot.service.BulletService;
import com.kuroneko.cqbot.service.TarKovMarketService;
import com.kuroneko.cqbot.utils.CacheUtil;
import com.kuroneko.cqbot.utils.HttpUtil;
import com.kuroneko.cqbot.utils.PuppeteerUtil;
import com.kuroneko.cqbot.utils.RedisUtil;
import com.kuroneko.cqbot.vo.BiliDynamicVo;
import com.kuroneko.cqbot.vo.TarKovMarketVo;
import com.mikuac.shiro.common.utils.CommonUtils;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.mikuac.shiro.common.utils.RegexUtils;
import com.ruiyun.jvppeteer.core.page.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.util.ObjectUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

@RestController
@RequiredArgsConstructor
public class TestController {
    private final BiLiService biLiService;
    private final BulletService bulletService;
    private final RedisUtil redisUtil;
    private final RestTemplate restTemplate;
    private final AiService aiService;
    private final TarKovMarketService tarKovMarketService;

    @RequestMapping(value = "/testBiLi")
    public BiliDynamicVo.BiliDynamicCard testBiLi() {
        Optional<BiliDynamicVo.BiliDynamicCard> firstCard = biLiService.getFirstCard("152065343");
        return firstCard.orElse(null);
    }

    @RequestMapping(value = "/testRedis")
    public Collection<String> testRedis() {
//        redisUtil.add(RedisKey.BILI_SUB+":123456",123123);
//        redisUtil.add(RedisKey.BILI_SUB+":123456",1234);
//        redisUtil.add(RedisKey.BILI_SUB+":12345",1234);
//        redisUtil.remove(RedisKey.BILI_SUB + ":12345", 1234);
        Collection<String> allKeys = redisUtil.getAllKeys(RedisKey.BILI_SUB);
        return allKeys;
    }

    @RequestMapping(value = "/testListener")
    public Collection<String> testListener() {
//        redisUtil.add(RedisKey.BILI_SUB+":123456",123123);
//        redisUtil.add(RedisKey.BILI_SUB+":123456",1234);
//        redisUtil.add(RedisKey.BILI_SUB+":12345",1234);
//        redisUtil.remove(RedisKey.BILI_SUB + ":12345", 1234);
        Collection<String> allKeys = redisUtil.getAllKeys(RedisKey.BILI_SUB);
        ApplicationContextHandler.publishEvent(new BiliSubscribeEvent(new BiliDynamicVo.BiliDynamicCard()));
        return allKeys;
    }

    @RequestMapping(value = "/testBullet")
    public Integer testBullet() {
//        Page page = PuppeteerUtil.getNewPage("https://escapefromtarkov.fandom.com/wiki/Ballistics", "domcontentloaded", 120000, 1650, 12000);
//
//        ElementHandle b = page.$("div._2O--J403t2VqCuF8XJAZLK");
//        if (b != null) {
//            try {
//                b.click();
//            } catch (InterruptedException | ExecutionException e) {
//                throw new RuntimeException(e);
//            }
//        }
//
//        String fun = """
//                ()=>{
//                    let list = document.querySelectorAll(".sortable.stickyheader tr");
//                    let data_list = [];
//                    let caliber = "";
//                    for (let i = 0; i < list.length; i++) {
//                        let line_data = list[i];
//                        if(!line_data) continue;
//                        let td_list = line_data.querySelectorAll("td");
//                        if(!td_list) continue;
//                        let td_data_list = [];
//                        for (let j = 0; j < td_list.length; j++) {
//                            let td_line_data = td_list[j];
//                            if(j == 0 && td_list.length == 16){
//                                caliber = td_line_data.innerText;
//                            } else if(j == 0 && td_list.length == 15){
//                                td_data_list.push(caliber);
//                            }
//                            if (j === (td_list.length - 15)) {
//                                td_data_list.push(td_line_data.querySelector("a").innerText);
//                                let b = [];
//                                let b_list = td_line_data.querySelectorAll("b");
//                                if (b_list) {
//                                    for (let k = 0; k < b_list.length; k++) {
//                                        b.push(b_list[k].innerText);
//                                    }
//                                    td_data_list.push(b);
//                                }
//                            } else {
//                                td_data_list.push(td_line_data.innerText);
//                            }
//                        }
//                        data_list.push(td_data_list);
//                    }
//                    return data_list;
//                }
//                """;
//        try {
//            JSHandle jsHandle = page.waitFor(fun);
//            ArrayList<Object> jsonValue = CastUtils.cast(jsHandle.jsonValue());
//
//
//        } catch (InterruptedException e) {
//            throw new RuntimeException(e);
//        }
        return 1;
    }

    @RequestMapping(value = "/testBili")
    public String testBili() {
//        Page newPage = PuppeteerUtil.getNewPage("http://127.0.0.1:8081/Life/智爷", 500, 800);
//        PuppeteerUtil.screenshot(newPage, "/opt/bot_img/life/1.png");
//        Page page = PuppeteerUtil.getNewPage("http://localhost:8081/baRank", 880, 500);
//        String imgPath = Constant.BASE_IMG_PATH + "ba/ba.png";
//        PuppeteerUtil.screenshot(page, imgPath, "#app");
//        return aiService.getWuGuoKai(1, "吃了什么");
//        HttpClient client = HttpClient.newHttpClient();
//        URI uri = URI.create("https://www.bilibili.com/opus/887167886324400164");
//        String scheme = uri.getScheme();
//        HttpRequest request = HttpRequest.newBuilder().uri(uri).build();
//        String urlBid;
//        try {
//            HttpResponse<String> send = client.send(request, HttpResponse.BodyHandlers.ofString());
//            urlBid = send.headers().firstValue("location").orElse("");
//        } catch (IOException | InterruptedException e) {
//            throw new BotException(e.getMessage());
//        } finally {
//            client.close();
//        }
//        uri = URI.create(STR."\{scheme}:\{urlBid}");
//        long l = System.currentTimeMillis();
//        String redirect = HttpUtil.getRedirect("https://www.bilibili.com/opus/887167886324400164");
//        System.out.println(System.currentTimeMillis() - l);
//        List<TarKovMarketVo> btc = tarKovMarketService.search("btc");
        PuppeteerUtil.getBrowser();
        return """
                你想查什么呢？
                发送：跳蚤+(需要查询的物品关键字名称 多个关键字用空格分开)
                如下：
                跳蚤 显卡
                跳蚤 btc
                """ + "\n123\n";
//        Optional<Matcher> match = RegexUtils.matcher(Regex.TKF_MARKET_SEARCH, "跳蚤123");
//        return match.map(matcher -> matcher.group("text").trim()).orElse("null");
    }


}
