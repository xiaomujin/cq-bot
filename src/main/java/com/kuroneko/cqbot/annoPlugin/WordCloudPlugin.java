package com.kuroneko.cqbot.annoPlugin;

import com.kennycason.kumo.CollisionMode;
import com.kennycason.kumo.WordFrequency;
import com.kennycason.kumo.bg.CircleBackground;
import com.kennycason.kumo.font.KumoFont;
import com.kennycason.kumo.font.scale.LinearFontScalar;
import com.kennycason.kumo.image.AngleGenerator;
import com.kennycason.kumo.nlp.FrequencyAnalyzer;
import com.kennycason.kumo.palette.ColorPalette;
import com.kuroneko.cqbot.entity.WordCloud;
import com.kuroneko.cqbot.enums.Regex;
import com.kuroneko.cqbot.exception.BotException;
import com.kuroneko.cqbot.exception.ExceptionHandler;
import com.kuroneko.cqbot.service.WordCloudService;
import com.kuroneko.cqbot.utils.CacheUtil;
import com.mikuac.shiro.annotation.GroupMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.lucene.analysis.Analyzer;
import org.apache.lucene.analysis.TokenStream;
import org.apache.lucene.analysis.tokenattributes.CharTermAttribute;
import org.springframework.stereotype.Component;
import org.wltea.analyzer.lucene.IKAnalyzer;

import java.awt.*;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

@Shiro
@Slf4j
@Component
@RequiredArgsConstructor
public class WordCloudPlugin {
    private final WordCloudService wordCloudService;

//    @GroupMessageHandler
//    public void saveMsg(Bot bot, GroupMessageEvent event) {
//        WordCloud wordCloud = WordCloud.builder()
//                .senderId(event.getSender().getUserId())
//                .groupId(event.getGroupId())
//                .time(LocalDateTime.now())
//                .content(event.getMessage())
//                .build();
//        wordCloudService.save(wordCloud);
//    }

//    @GroupMessageHandler
//    @MessageHandlerFilter(cmd = Regex.WORD_CLOUD)
//    public void handler(Bot bot, GroupMessageEvent event, Matcher matcher) {
//        log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), Regex.WORD_CLOUD);
//        Long uid = event.getUserId();
//        var msgId = event.getMessageId();
//        var type = matcher.group(1);
//        var range = matcher.group(2);
//        if ("本群".equals(type)) {
//            uid = 0L;
//        }
//        String tmpCache = STR."\{type}_\{range}_\{event.getGroupId()}_\{uid}";
//        ExceptionHandler.with(bot, event, () -> CacheUtil.getOrPut(tmpCache, 10, TimeUnit.MINUTES, () -> {
//            bot.sendGroupMsg(event.getGroupId(), "回想中，请耐心等待～", false);
//            List<String> contents = getWords(event.getUserId(), event.getGroupId(), type, range);
//            if (contents.isEmpty()) {
//                throw new BotException("唔呣～记忆里没有找到你的发言记录呢");
//            }
//            return MsgUtils.builder().reply(msgId).img(STR."base64://\{generateWordCloud(contents)}").build();
//        }));
//    }

    private String generateWordCloud(List<String> contents) {
        FrequencyAnalyzer frequencyAnalyzer = new FrequencyAnalyzer();
        frequencyAnalyzer.setWordFrequenciesToReturn(300);
        frequencyAnalyzer.setMinWordLength(2);
//        frequencyAnalyzer.setWordTokenizer(JieBaTokenizer());
        ArrayList<String> analyzerList = getAnalyzerList(contents);
        List<WordFrequency> wordFrequencies = frequencyAnalyzer.load(analyzerList);
        Dimension dimension = new Dimension(1000, 1000);
        var wordCloud = new com.kennycason.kumo.WordCloud(dimension, CollisionMode.PIXEL_PERFECT);
        wordCloud.setPadding(2);
        wordCloud.setAngleGenerator(new AngleGenerator(0));
        wordCloud.setKumoFont(
                new KumoFont(Objects.requireNonNull(this.getClass().getResourceAsStream("/static/font/loli.ttf")))
        );
        List<String> colors = List.of("0000FF", "40D3F1", "40C5F1", "40AAF1", "408DF1", "4055F1");
        wordCloud.setBackground(new CircleBackground(((1000 + 1000) / 4)));
        wordCloud.setBackgroundColor(new Color(0xFFFFFF));
        wordCloud.setColorPalette(
                new ColorPalette(colors.stream().map(it -> new Color(Integer.parseInt(it, 16))).collect(Collectors.toList()))
        );
        wordCloud.setFontScalar(new LinearFontScalar(20, 80));
        wordCloud.build(wordFrequencies);

        ByteArrayOutputStream stream = new ByteArrayOutputStream();
        wordCloud.writeToStreamAsPNG(stream);

        return Base64.getEncoder().encodeToString(stream.toByteArray());
    }

    private List<String> query(Long userId, Long groupId, LocalDateTime start, LocalDateTime end) {
        return wordCloudService.lambdaQuery()
                .select(WordCloud::getContent)
                .eq(userId != null, WordCloud::getSenderId, userId)
                .eq(WordCloud::getGroupId, groupId)
                .between(WordCloud::getTime, start, end)
                .list()
                .stream()
                .map(WordCloud::getContent)
                .toList();
    }

    private List<String> query(Long groupId, LocalDateTime start, LocalDateTime end) {
        return query(null, groupId, start, end);
    }

    private List<String> getWordsForRange(Long userId, Long groupId, String type, String range) {
        LocalDate now = LocalDate.now();
        LocalDateTime startOfDay = now.atStartOfDay();
        LocalDateTime endOfDay = now.plusDays(1).atStartOfDay();
        LocalDateTime startOfWeek = now.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY)).atStartOfDay();
        LocalDateTime endOfWeek = now.with(TemporalAdjusters.nextOrSame(DayOfWeek.SUNDAY)).plusDays(1).atStartOfDay();
        LocalDateTime startOfMonth = now.with(TemporalAdjusters.firstDayOfMonth()).atStartOfDay();
        LocalDateTime endOfMonth = now.with(TemporalAdjusters.lastDayOfMonth()).plusDays(1).atStartOfDay();
        LocalDateTime startOfYear = now.with(TemporalAdjusters.firstDayOfYear()).atStartOfDay();
        LocalDateTime endOfYear = now.with(TemporalAdjusters.lastDayOfYear()).plusDays(1).atStartOfDay();

        return switch (type) {
            case "我的" -> switch (range) {
                case "今日" -> query(userId, groupId, startOfDay, endOfDay);
                case "本周" -> query(userId, groupId, startOfWeek, endOfWeek);
                case "本月" -> query(userId, groupId, startOfMonth, endOfMonth);
                case "本年" -> query(userId, groupId, startOfYear, endOfYear);
                default -> List.of();
            };
            case "本群" -> switch (range) {
                case "今日" -> query(groupId, startOfDay, endOfDay);
                case "本周" -> query(groupId, startOfWeek, endOfWeek);
                case "本月" -> query(groupId, startOfMonth, endOfMonth);
                case "本年" -> query(groupId, startOfYear, endOfYear);
                default -> List.of();
            };
            default -> List.of();
        };
    }

    private List<String> getWords(Long userId, Long groupId, String type, String range) {
//        var filterRule = StringUtils.join(Config.plugins.wordCloud.filterRule, "|").toRegex()
        var contents = new ArrayList<String>();
        List<String> forRange = getWordsForRange(userId, groupId, type, range);
        forRange.forEach(raw -> {
            List<ArrayMsg> arrayMsgs = ShiroUtils.rawToArrayMsg(raw);
            List<String> textList = arrayMsgs.stream()
                    .filter(it -> it.getType() == MsgTypeEnum.text)
                    .map(it -> {
                        String text = it.getData().get("text");
                        if (text != null) {
                            return text.trim();
                        }
                        return null;
                    })
                    .filter(it -> it != null && it.length() > 1)
                    .toList();
            contents.addAll(textList);
        });
        return contents;
    }


    private static ArrayList<String> getAnalyzerList(List<String> msgList) {
        String allMsg = String.join(" ", msgList);
        ArrayList<String> res = new ArrayList<>();
        try (
                //构建IK分词器，使用smart分词模式
                Analyzer analyzer = new IKAnalyzer(true);
                //获取Lucene的TokenStream对象
                TokenStream ts = analyzer.tokenStream("myfield", allMsg)
        ) {
            //获取词元位置属性
//            OffsetAttribute offset = ts.addAttribute(OffsetAttribute.class);
            //获取词元文本属性
            CharTermAttribute term = ts.addAttribute(CharTermAttribute.class);
            //获取词元文本属性
//            TypeAttribute type = ts.addAttribute(TypeAttribute.class);
            //重置TokenStream（重置StringReader）
            ts.reset();
            //迭代获取分词结果
            while (ts.incrementToken()) {
//                System.out.println(STR."\{offset.startOffset()} - \{offset.endOffset()} : \{term.toString()} | \{type.type()}");
                res.add(term.toString());
            }
            //关闭TokenStream（关闭StringReader）
            ts.end();   // Perform end-of-stream operations, e.g. set the final offset.
        } catch (IOException e) {
            log.error(e.getMessage(), e);
            throw new BotException("分词失败");
        }
        return res;
    }
}

