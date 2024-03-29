package com.kuroneko.cqbot.annoPlugin;

import com.kuroneko.cqbot.entity.WordCloud;
import com.kuroneko.cqbot.enums.Regex;
import com.kuroneko.cqbot.service.WordCloudService;
import com.mikuac.shiro.annotation.GroupMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.regex.Matcher;

@Shiro
@Slf4j
@Component
@RequiredArgsConstructor
public class WordCloudPlugin {
    private final WordCloudService wordCloudService;

    @GroupMessageHandler
    public void saveMsg(Bot bot, GroupMessageEvent event) {
        WordCloud wordCloud = WordCloud.builder()
                .senderId(event.getSender().getUserId())
                .groupId(event.getGroupId())
                .time(LocalDateTime.now())
                .content(event.getMessage())
                .build();
        wordCloudService.save(wordCloud);
    }

    @GroupMessageHandler
    @MessageHandlerFilter(cmd = Regex.WORD_CLOUD)
    public void handler(Bot bot, GroupMessageEvent event, Matcher matcher) {

    }
}

