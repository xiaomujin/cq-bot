package com.kuroneko.cqbot.annoPlugin;

import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.kuroneko.cqbot.config.module.LocalJavaTimeModule;
import com.kuroneko.cqbot.enums.Regex;
import com.kuroneko.cqbot.exception.BotException;
import com.kuroneko.cqbot.exception.ExceptionHandler;
import com.kuroneko.cqbot.vo.BaRankInfo;
import com.kuroneko.cqbot.vo.BaVo;
import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

@Shiro
@Slf4j
@Component
@RequiredArgsConstructor
public class BaPlugin {
    private final RestTemplate restTemplate;
    private static final String ARONA_API_IMAGE = "https://arona.diyigemt.com/api/v2/image?name=";
    private static final String ARONA_IMAGE = "https://arona.cdn.diyigemt.com/image/s";
    private final ExpiringMap<String, ArrayList<String>> expiringMap = ExpiringMap.builder()
            //允许更新过期时间值,如果不设置variableExpiration，不允许后面更改过期时间,一旦执行更改过期时间操作会抛异常UnsupportedOperationException
            .variableExpiration()
//            1）ExpirationPolicy.ACCESSED ：每进行一次访问，过期时间就会重新计算；
//            2）ExpirationPolicy.CREATED：在过期时间内重新 put 值的话，过期时间重新计算；
            .expirationPolicy(ExpirationPolicy.CREATED)
            //设置每个key有效时间30m,如果key不设置过期时间，key永久有效
            .expiration(5L, TimeUnit.MINUTES)
            .build();

    @AnyMessageHandler(cmd = Regex.BA_TOTAL_BATTLE)
    public void ba(Bot bot, AnyMessageEvent event, Matcher matcher) {
        ExceptionHandler.with(bot, event, () -> {
            ArrayList<String> arrayList = expiringMap.get(Regex.BA_TOTAL_BATTLE);
            if (arrayList != null) {
                arrayList.forEach(s -> bot.sendMsg(event, s, false));
                return "";
            }

            HttpHeaders headers = new HttpHeaders();
            headers.add("Authorization", "ba-token 1419229777:DjHOGwNOxOv2tcoLSwpqq99R77qsfXUm8vgJcj");
//            headers.add("Authorization", "ba-token uuz:uuz");
            HttpEntity<Object> httpEntity = new HttpEntity<>(headers);
            ResponseEntity<String> seasonList = restTemplate.exchange("https://api.arona.icu/api/season/list", HttpMethod.GET, httpEntity, String.class);
            if (!seasonList.getStatusCode().is2xxSuccessful()) {
                throw new BotException("获取总力战数据失败！");
            }
            JSONObject jsonObject = JSON.parseObject(seasonList.getBody());
            JSONArray data = jsonObject.getJSONArray("data");
            JSONObject seasonInfo = data.getJSONObject(0);
            Integer season = seasonInfo.getInteger("season");
            ResponseEntity<String> rankList = restTemplate.exchange("https://api.arona.icu/api/rank/list/1/2/" + season + "?page=1&size=50", HttpMethod.GET, httpEntity, String.class);
            if (!rankList.getStatusCode().is2xxSuccessful()) {
                throw new BotException("获取总力战数据失败！");
            }
            JSONObject rankListJO = JSON.parseObject(rankList.getBody());
            JSONArray rankListData = rankListJO.getJSONObject("data").getJSONArray("records");
            List<BaRankInfo> javaList = rankListData.toJavaList(BaRankInfo.class);
//            ResponseEntity<String> listByLastRank = restTemplate.exchange("https://api.arona.icu/api/rank/list_by_last_rank?server=1&season=" + season, HttpMethod.GET, httpEntity, String.class);
//            if (!rankList.getStatusCode().is2xxSuccessful()) {
//                throw new BotException("获取总力战数据失败！");
//            }
//            JSONObject listByLastRankJO = JSON.parseObject(listByLastRank.getBody());
//            JSONArray listByLastRankData = listByLastRankJO.getJSONArray("data");
            ArrayList<String> list = new ArrayList<>();
            MsgUtils msg = MsgUtils.builder();
            String mapName = seasonInfo.getJSONObject("map").getString("value");
            msg.text("第").text(season.toString()).text("期 ").text(mapName).text(seasonInfo.getString("boss"));
            msg.text("\n开始：").text(seasonInfo.getString("startTime"));
            msg.text("\n结束：").text(seasonInfo.getString("endTime"));
            String format = DateTimeFormatter.ofPattern(LocalJavaTimeModule.NORM_DATETIME_PATTERN).format(LocalDateTime.now());
            msg.text("\n数据更新于：\n");
            msg.text(format);
            msg.text("\n来源：https://arona.icu");
            msg.text("\n--------------------\n");
            msg.text("排名  |    分数    | 难度\n");
            for (int i = 6; i < javaList.size() + 6; i++) {
                BaRankInfo rankInfo = javaList.get(i - 6);
                msg.text(formatRank(rankInfo));
                if (i % 13 == 0) {
                    list.add(msg.build());
                    msg = MsgUtils.builder();
                }
            }
            if (StrUtil.isNotEmpty(msg.build())) {
                list.add(msg.build());
            }
            list.forEach(s -> bot.sendMsg(event, s, false));
            expiringMap.put(Regex.BA_TOTAL_BATTLE, list);
            return "";
        });
    }

    private String formatRank(BaRankInfo rankInfo) {
        return "%-6d %-10d %-2s\n".formatted(rankInfo.getRank(), rankInfo.getBestRankingPoint(), rankInfo.getHard());
    }


    @AnyMessageHandler(cmd = Regex.BA_IMAGE_BATTLE)
    public void image(Bot bot, AnyMessageEvent event, Matcher matcher) {
        ExceptionHandler.with(bot, event, () -> {
            String text = matcher.group("text").trim();
            BaVo baVo = restTemplate.getForObject(ARONA_API_IMAGE + text, BaVo.class);
            if (baVo == null) {
                throw new BotException("爱丽丝出错了");
            }
            List<BaVo.BaImage> baImages = baVo.getData().toJavaList(BaVo.BaImage.class);
            if (baImages == null || baImages.isEmpty()) {
                throw new BotException("爱丽丝什么都没有找到~");
            }
            if (baVo.getCode().equals(200)) {
                for (BaVo.BaImage baImage : baImages) {
                    MsgUtils msg = MsgUtils.builder();
                    if (baImage.getType().equalsIgnoreCase("file")) {
                        msg.img(ARONA_IMAGE + baImage.getContent());
                    } else if (baImage.getType().equalsIgnoreCase("plain")) {
                        msg.text(baImage.getContent());
                    }
                    bot.sendMsg(event, msg.build(), false);
                }
            } else if (baVo.getCode().equals(101)) {
                MsgUtils msg = MsgUtils.builder();
                msg.text("是想问什么呢：\n");
                for (BaVo.BaImage baImage : baImages) {
                    msg.text(baImage.getName() + "\n");
                }
                bot.sendMsg(event, msg.build(), false);
            } else {
                throw new BotException(baVo.getMessage());
            }
            return "";
        });
    }

}
