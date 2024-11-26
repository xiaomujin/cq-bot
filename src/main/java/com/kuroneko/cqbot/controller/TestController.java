package com.kuroneko.cqbot.controller;

import com.kuroneko.cqbot.entity.WordCloud;
import com.kuroneko.cqbot.service.*;
import com.kuroneko.cqbot.vo.BiliDynamicVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.util.*;

@RestController
@RequiredArgsConstructor
public class TestController {
    private final BiLiService biLiService;
    private final BulletService bulletService;
    private final RestTemplate restTemplate;
    private final AiService aiService;
    private final TarKovMarketService tarKovMarketService;
    private final WordCloudService wordCloudService;

    @RequestMapping(value = "/testBiLi")
    public BiliDynamicVo.BiliDynamicCard testBiLi() {
        Optional<BiliDynamicVo.BiliDynamicCard> firstCard = biLiService.getFirstCard("152065343");
        return firstCard.orElse(null);
    }

    @RequestMapping(value = "/testRedis")
    public Collection<String> testRedis() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(10);
        wordCloudService.lambdaUpdate().lt(WordCloud::getTime, localDateTime).remove();
        return Collections.emptyList();
    }

}
