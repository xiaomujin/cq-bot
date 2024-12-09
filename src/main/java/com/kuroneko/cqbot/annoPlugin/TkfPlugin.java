package com.kuroneko.cqbot.annoPlugin;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.kuroneko.cqbot.config.ProjectConfig;
import com.kuroneko.cqbot.core.annotation.BotHandler;
import com.kuroneko.cqbot.core.annotation.BotMsgHandler;
import com.kuroneko.cqbot.core.dto.MsgInfo;
import com.kuroneko.cqbot.dto.Tkf;
import com.kuroneko.cqbot.enums.Regex;
import com.kuroneko.cqbot.enums.sysPluginRegex;
import com.kuroneko.cqbot.exception.ExceptionHandler;
import com.kuroneko.cqbot.utils.CacheUtil;
import com.kuroneko.cqbot.utils.HttpUtil;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

@BotHandler
@Slf4j
@Component
@RequiredArgsConstructor
public class TkfPlugin {

    private final RestTemplate restTemplate;

    @BotMsgHandler(model = sysPluginRegex.TKF_SYSTEM, cmd = Regex.TKF_BOSS_CHANCE)
    public void handler(MsgInfo msgInfo, Bot bot, AnyMessageEvent event, Matcher matcher) {
        log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), "塔科夫BOSS概率");
        Integer msgId = event.getMessageId();
        ExceptionHandler.with(bot, event, () -> CacheUtil.getOrPut(Regex.TKF_BOSS_CHANCE, 10, TimeUnit.MINUTES, () -> {
            bot.setGroupReaction(event.getGroupId(), msgId, "424", true);
            Optional<JSONObject> queryRes = getQueryRes2(QUERY_BOSS_CHANCE);
            if (queryRes.isEmpty()) {
                return "查询失败";
            }
            JSONArray jsonArray = queryRes.get().getJSONArray("maps");
            List<Tkf.Map> maps = jsonArray.toJavaList(Tkf.Map.class);
            // 整理数据
            HashMap<String, HashMap<String, List<Double>>> hashMap = new HashMap<>();
            for (Tkf.Map map : maps) {
                HashMap<String, List<Double>> bossMap = new HashMap<>();
                for (Tkf.Boss boss : map.getBosses()) {
                    bossMap.computeIfAbsent(boss.getBoss().getName(), k -> new ArrayList<>()).add(boss.getSpawnChance());
                }
                hashMap.put(map.getName(), bossMap);
            }
            StringBuilder sb = new StringBuilder();
            hashMap.forEach((mapName, v1) -> {
                sb.append(mapName).append("\n");
                v1.forEach((bossName, v2) -> {
                    double chance = v2.stream().mapToDouble(Double::doubleValue).average().getAsDouble();
                    String format = String.format(": %.0f%%\n", chance * 100);
                    sb.append("    ").append(bossName).append(format);
                });
            });
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            String formatTime = LocalDateTime.now().format(formatter);
            sb.append(formatTime);
            bot.setGroupReaction(event.getGroupId(), msgId, "424", false);
            return sb.toString();
        }));
    }


    private Optional<JSONObject> getQueryRes(String query) {
        String url = "https://api.tarkov.dev/graphql";
        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put("query", query);

        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.CONTENT_TYPE, "application/json");
        headers.add(HttpHeaders.ACCEPT, "application/json");
        headers.add(HttpHeaders.ORIGIN, "https://api.tarkov.dev");
        headers.add(HttpHeaders.REFERER, "https://api.tarkov.dev/");
        headers.add(HttpHeaders.USER_AGENT, ProjectConfig.USER_AGENT);

        HttpEntity<HashMap<String, String>> httpEntity = new HttpEntity<>(queryMap, headers);
        ResponseEntity<String> response = restTemplate.exchange(url, HttpMethod.POST, httpEntity, String.class);
        String responseBody = response.getBody();
        if (!response.getStatusCode().is2xxSuccessful() || responseBody == null) {
            log.error(responseBody);
            return Optional.empty();
        }
        JSONObject parse = JSON.parseObject(responseBody);
        JSONObject bodyData = parse.getJSONObject("data");
        return Optional.ofNullable(bodyData);
    }

    private static Optional<JSONObject> getQueryRes2(String query) {
        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put("query", query);
        String url = "https://api.tarkov.dev/graphql";
        HttpClient httpClient = HttpUtil.getHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(queryMap)))
                .build();
        String responseBody = HttpUtil.request(httpClient, url, httpRequest);
        JSONObject parse = JSON.parseObject(responseBody);
        JSONObject bodyData = parse.getJSONObject("data");
        return Optional.ofNullable(bodyData);
    }

    private static final String QUERY_BOSS_CHANCE = """
            {
              maps(gameMode: regular, lang: zh) {
                name
                bosses {
                  boss {
                    name
                  }
                  spawnChance
                }
              }
            }
            """;
}

