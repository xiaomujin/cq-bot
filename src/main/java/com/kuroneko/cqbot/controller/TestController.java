package com.kuroneko.cqbot.controller;

import com.kuroneko.cqbot.service.BiLiService;
import com.kuroneko.cqbot.vo.BiliDynamicVo;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Optional;

@RestController
@RequiredArgsConstructor
public class TestController {
    private final BiLiService biLiService;

    @RequestMapping(value = "/testBiLi")
    public BiliDynamicVo.BiliDynamicCard testBiLi() {
        Optional<BiliDynamicVo.BiliDynamicCard> firstCard = biLiService.getFirstCard("152065343");
        return firstCard.orElse(null);
    }


}
