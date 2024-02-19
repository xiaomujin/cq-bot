package com.kuroneko.cqbot.service;

import com.mikuac.shiro.common.utils.MsgUtils;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class HelpService {

    public String tkfMarketSearchHelp() {
        return MsgUtils.builder()
                .text("""
                        你想查什么呢？
                        发送：跳蚤+(需要查询的物品关键字名称 多个关键字用空格分开)
                        如下：
                        跳蚤 显卡
                        跳蚤 btc
                        """)
                .build();
    }

}
