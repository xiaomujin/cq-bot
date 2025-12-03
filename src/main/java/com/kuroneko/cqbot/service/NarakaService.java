package com.kuroneko.cqbot.service;

import com.kuroneko.cqbot.utils.JsonUtil;
import com.kuroneko.cqbot.dto.Naraka;
import com.mikuac.shiro.common.utils.MsgUtils;
import lombok.RequiredArgsConstructor;
import lombok.Synchronized;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

@Slf4j
@Service
@RequiredArgsConstructor
public class NarakaService {
    private final RestTemplate restTemplate;

    /**
     * <a href="https://record.uu.163.com/naraka/?name=%E5%98%A0%E4%B9%B1&server=163">永劫无间战绩</a>
     */
    @Synchronized
    public String getRecord(String name, int gameMode, int seasonId) {
        String authUrl = "https://record.uu.163.com/api/naraka/auth/" + name + "/163";
        HttpHeaders headers = new HttpHeaders();
        headers.add(HttpHeaders.COOKIE, "session=6-F1vljKhCcvBwyLB395cvFuvRHv4c-6u-Z0WIf7");
        headers.add(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36");
        // 使用空字符串作为请求体
        HttpEntity<String> httpEntity = new HttpEntity<>("", headers);
        ResponseEntity<String> authRes = restTemplate.exchange(authUrl, HttpMethod.GET, httpEntity, String.class);
        JsonNode authResBody = JsonUtil.toNode(authRes.getBody());
        if (!authRes.getStatusCode().is2xxSuccessful() || authResBody == null || authResBody.get("code").asInt() != 1000) {
            return "用户不存在";
        }
        String careerUrl = "https://record.uu.163.com/api/naraka/career?game_mode=" + gameMode + "&season_id=" + seasonId;
        ResponseEntity<String> careerRes = restTemplate.exchange(careerUrl, HttpMethod.GET, httpEntity, String.class);
        JsonNode careerResBody = JsonUtil.toNode(careerRes.getBody());
        Naraka.Career career = JsonUtil.toBean(careerResBody.get("data").toString(), Naraka.Career.class);
        return MsgUtils.builder().img(career.getGradeIconUrl())
                .text(career.getRoleName() + ": " + career.getRoleId() + "\n" +
                        career.getGradeName() + "  " + career.getRankScore() + "分\n" +
                        "Lv." + career.getRoleLevel() + "  游戏时长: " + getGameTime(career.getGameTime()) + "\n" +
                        "对局时长: " + getGameTime(career.getTotalTime()) + "\n" +
                        "模式: " + getGameModeName(career.getGameMode()) + "  场次: " + career.getRound() + "\n" +
                        "胜场: " + career.getWin() + "  胜率: " + getRate(career.getWin(), career.getRound()) + "\n" +
                        "前五数: " + career.getTopFive() + "  前五率: " + getRate(career.getTopFive(), career.getRound()) + "\n" +
                        "KD: " + getRate2(career.getKill(), career.getDead()) + "\n" +
                        "场均输出: " + getRate2(career.getDamage(), career.getRound())
                ).build();
    }

    private String getGameModeName(int gameMode) {
        return switch (gameMode) {
            case 1 -> "单排";
            case 12 -> "双排";
            case 2 -> "三排";
            default -> "未知";
        };
    }

    private String getGameTime(int gameSec) {
        return gameSec / 60 / 60 + "h";
    }

    private String getRate(int one, int two) {
        if (two == 0) {
            two = 1;
        }
        return String.format("%.2f%%", one * 1.0 / two * 100);
    }

    private String getRate2(int one, int two) {
        if (two == 0) {
            two = 1;
        }
        return String.format("%.2f", one * 1.0 / two);
    }

}