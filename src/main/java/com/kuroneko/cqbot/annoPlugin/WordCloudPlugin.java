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
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.apache.lucene.analysis.tokenattributes.OffsetAttribute;
import org.apache.lucene.analysis.tokenattributes.TypeAttribute;
import org.springframework.stereotype.Component;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.io.IOException;
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
        //构建IK分词器，使用smart分词模式
        long start = System.currentTimeMillis();
        Analyzer analyzer = new IKAnalyzer(true);

        //获取Lucene的TokenStream对象
        TokenStream ts = null;
        try {
            ts = analyzer.tokenStream("myfield", event.getMessage());
            //获取词元位置属性
            OffsetAttribute offset = ts.addAttribute(OffsetAttribute.class);
            //获取词元文本属性
            CharTermAttribute term = ts.addAttribute(CharTermAttribute.class);
            //获取词元文本属性
            TypeAttribute type = ts.addAttribute(TypeAttribute.class);

            //重置TokenStream（重置StringReader）
            ts.reset();
            //迭代获取分词结果
            while (ts.incrementToken()) {
                System.out.println(STR."\{offset.startOffset()} - \{offset.endOffset()} : \{term.toString()} | \{type.type()}");
            }
            //关闭TokenStream（关闭StringReader）
            ts.end();   // Perform end-of-stream operations, e.g. set the final offset.

        } catch (IOException e) {
            log.error(e.getMessage(), e);
        } finally {
            //释放TokenStream的所有资源
            if (ts != null) {
                try {
                    ts.close();
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
            }
        }
        log.info("耗时：{}", System.currentTimeMillis() - start);
    }
}

