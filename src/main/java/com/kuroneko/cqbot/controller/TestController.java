package com.kuroneko.cqbot.controller;

import cn.hutool.core.util.NumberUtil;
import com.kuroneko.cqbot.constant.RedisKey;
import com.kuroneko.cqbot.entity.Bullet;
import com.kuroneko.cqbot.event.BiliSubscribeEvent;
import com.kuroneko.cqbot.handler.ApplicationContextHandler;
import com.kuroneko.cqbot.service.BiLiService;
import com.kuroneko.cqbot.service.BulletService;
import com.kuroneko.cqbot.utils.PuppeteerUtil;
import com.kuroneko.cqbot.utils.RedisUtil;
import com.kuroneko.cqbot.vo.BiliDynamicVo;
import com.ruiyun.jvppeteer.core.page.ElementHandle;
import com.ruiyun.jvppeteer.core.page.JSHandle;
import com.ruiyun.jvppeteer.core.page.Page;
import lombok.RequiredArgsConstructor;
import org.springframework.data.util.CastUtils;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;

@RestController
@RequiredArgsConstructor
public class TestController {
    private final BiLiService biLiService;
    private final BulletService bulletService;
    private final RedisUtil redisUtil;

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


}
