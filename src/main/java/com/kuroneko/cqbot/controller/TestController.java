package com.kuroneko.cqbot.controller;

import com.kuroneko.cqbot.constant.RedisKey;
import com.kuroneko.cqbot.service.BiLiService;
import com.kuroneko.cqbot.vo.BiliDynamicVo;
import com.kuroneko.cqbot.utils.RedisUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Collection;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class TestController {
    private final BiLiService biLiService;
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


}
