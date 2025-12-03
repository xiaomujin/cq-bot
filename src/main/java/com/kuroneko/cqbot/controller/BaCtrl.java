package com.kuroneko.cqbot.controller;

import com.kuroneko.cqbot.utils.JsonUtil;
import tools.jackson.databind.JsonNode;

import java.util.HashMap;

import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.exception.BotException;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.client.RestTemplate;

import java.text.SimpleDateFormat;
import java.time.*;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;

@Controller
@RequiredArgsConstructor
public class BaCtrl {
    private final RestTemplate restTemplate;

    @RequestMapping(value = {"/baRank"})
    public String baRank(Model model) {
        HttpHeaders headers = new HttpHeaders();
//        headers.add("Authorization", Constant.BA_TOKEN);
        headers.add("Authorization", "ba-token uuz:uuz");
        HashMap<String, Object> requestBody = new HashMap<>();
        HttpEntity<Object> httpEntity = new HttpEntity<>(requestBody, headers);
        ResponseEntity<String> seasonList = restTemplate.exchange("https://api.arona.icu/api/season/list", HttpMethod.POST, httpEntity, String.class);
        JsonNode seasonListBody = JsonUtil.toNode(seasonList.getBody());
        if (!seasonList.getStatusCode().is2xxSuccessful() || seasonListBody == null || seasonListBody.get("code").asInt() != 200) {
            throw new BotException("获取总力战数据失败！");
        }
        JsonNode seasonListBodyData = seasonListBody.get("data");
        JsonNode seasonInfo = seasonListBodyData.get(0);
        model.addAttribute("serverName", "官服");
        Integer season = seasonInfo.get("season").asInt();
        String mapName = seasonInfo.get("map").get("value").asString();
        String bossName = seasonInfo.get("boss").asString();
        String formatSeasonInfo = String.format("第%d期 %s %s", season, mapName, bossName);
        model.addAttribute("seasonInfo", formatSeasonInfo);

        ResponseEntity<String> listTop = restTemplate.exchange("https://api.arona.icu/api/v2/rank/list_top", HttpMethod.POST, httpEntity, String.class);
        JsonNode listTopBody = JsonUtil.toNode(listTop.getBody());
        if (!listTop.getStatusCode().is2xxSuccessful() || listTopBody == null || listTopBody.get("code").asInt() != 200) {
            throw new BotException("获取排名数据失败！");
        }
        JsonNode listTopBodyData = listTopBody.get("data");
        model.addAttribute("listTopBodyData", listTopBodyData);

        requestBody.clear();
        requestBody.put("server", 1);//服务器 1国服，2B服
        requestBody.put("season", season);//期数，latest默认当前最新，可填数值查询具体期数
        requestBody.put("dataType", 0);//1上边界，0下边界。不带这个参数时默认返回全部
        requestBody.put("tryNumber", 0);//0难度，0以上则是出刀边界。不带这个参数时默认返回全部
//        HttpEntity<String> httpEntity = new HttpEntity<>(JsonUtil.toString(requestBody), headers);
        ResponseEntity<String> lastRank = restTemplate.exchange("https://api.arona.icu/api/v2/rank/list_by_last_rank", HttpMethod.POST, httpEntity, String.class);
        JsonNode lastRankBody = JsonUtil.toNode(lastRank.getBody());
        if (!lastRank.getStatusCode().is2xxSuccessful() || lastRankBody == null || lastRankBody.get("code").asInt() != 200) {
            throw new BotException("获取排名数据失败！");
        }
        JsonNode lastRankBodyData = lastRankBody.get("data");
        model.addAttribute("lastRankBodyData", lastRankBodyData);

        requestBody.clear();
        requestBody.put("server", 1);//服务器 1国服，2B服
        requestBody.put("season", season);//期数，latest默认当前最新，可填数值查询具体期数
        requestBody.put("type", 0);//查询类型，1常规，2档线
        requestBody.put("page", 1);
        requestBody.put("size", 50);
//        HttpEntity<String> httpEntity = new HttpEntity<>(JsonUtil.toString(requestBody), headers);
        ResponseEntity<String> rankList = restTemplate.exchange("https://api.arona.icu/api/v2/rank/list", HttpMethod.POST, httpEntity, String.class);
        JsonNode rankListBody = JsonUtil.toNode(rankList.getBody());
        if (!rankList.getStatusCode().is2xxSuccessful() || rankListBody == null || rankListBody.get("code").asInt() != 200) {
            throw new BotException("获取排名数据失败！");
        }
        JsonNode rankListBodyData = rankListBody.get("data");
        JsonNode records = rankListBodyData.get("records");
        long recordTime = records.get(0).get("recordTime").asLong();
        SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
        String updateTime = df.format(new Date(recordTime));
        model.addAttribute("updateTime", updateTime);
        return "ba/BaRank";
    }

    @RequestMapping(value = {"/baCalendar"})
    public String baCalendar(Model model) {
        String[] weekStr = new String[]{"", "一", "二", "三", "四", "五", "六", "日"};
        LocalDate today = LocalDate.now();
        LocalDate tempDay = today.minusDays(6);
        ArrayList<HashMap<String, Object>> tableHeadList = new ArrayList<>();
        ArrayList<Integer> dateList = new ArrayList<>();
        ArrayList<String> weekList = new ArrayList<>();
        LocalDateTime startTime = tempDay.atStartOfDay();
        int tempDayMonthValue = tempDay.getMonthValue();
        for (int i = 0; i < 13; i++) {
            if (tempDayMonthValue != tempDay.getMonthValue()) {
                HashMap<String, Object> jsonObject = new HashMap<>();
                jsonObject.put("month", tempDayMonthValue);
                jsonObject.put("date", dateList);
                jsonObject.put("week", weekList);
                dateList = new ArrayList<>();
                weekList = new ArrayList<>();

                tableHeadList.add(jsonObject);
                tempDayMonthValue = tempDay.getMonthValue();
            }
            dateList.add(tempDay.getDayOfMonth());
            weekList.add(weekStr[tempDay.getDayOfWeek().getValue()]);
            tempDay = tempDay.plusDays(1);
        }
        LocalDateTime endTime = tempDay.atStartOfDay().minusSeconds(1);
        HashMap<String, Object> jsonObject = new HashMap<>();
        jsonObject.put("month", tempDayMonthValue);
        jsonObject.put("date", dateList);
        jsonObject.put("week", weekList);
        tableHeadList.add(jsonObject);
        long startEpochMilli = startTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        long endEpochMilli = endTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        LocalDateTime nowTime = LocalDateTime.now();
        long nowEpochMilli = nowTime.toInstant(ZoneOffset.of("+8")).toEpochMilli();
        long totalRange = endEpochMilli - startEpochMilli;
        Double nowLeft = (nowEpochMilli - startEpochMilli) / (double) totalRange * 100;

        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
        DateTimeFormatter formatter2 = DateTimeFormatter.ofPattern("MM-dd HH:mm");

        model.addAttribute("tableHeadList", tableHeadList);
        model.addAttribute("nowLeft", nowLeft);
        model.addAttribute("nowDate", nowTime.getDayOfMonth());
        model.addAttribute("nowTime", nowTime.format(formatter));

        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", Constant.BA_TOKEN);
//        headers.add("Authorization", "ba-token uuz:uuz");
        HashMap<String, Object> requestBody = new HashMap<>();
        requestBody.put("server", 1);
        HttpEntity<String> httpEntity = new HttpEntity<>(JsonUtil.toString(requestBody), headers);
        ResponseEntity<String> seasonList = restTemplate.exchange("https://api.arona.icu/api/events/v2/info", HttpMethod.POST, httpEntity, String.class);
        JsonNode calendarListBody = JsonUtil.toNode(seasonList.getBody());
        if (!seasonList.getStatusCode().is2xxSuccessful() || calendarListBody == null || calendarListBody.get("code").asInt() != 200) {
            throw new BotException("获取活动日历数据失败！");
        }
        JsonNode calendarBodyData = calendarListBody.get("data");
        ArrayList<HashMap<String, Object>> infos = new ArrayList<>();
        for (int i = 0; i < calendarBodyData.size(); i++) {
            JsonNode info = calendarBodyData.get(i);
            long infoEndTime = info.get("endTime").asLong();
            if (infoEndTime < startEpochMilli + 1000 * 60 * 60 * 24 * 3) {
                continue;
            }
            long infoStartTime = info.get("startTime").asLong();
            if (infoStartTime > endEpochMilli + 1000 * 60 * 60 * 24 * 3) {
                continue;
            }
            String infoStartStr = LocalDateTime.ofInstant(Instant.ofEpochMilli(infoStartTime), ZoneId.systemDefault()).format(formatter2);
            String infoEndStr = LocalDateTime.ofInstant(Instant.ofEpochMilli(infoEndTime), ZoneId.systemDefault()).format(formatter2);
            HashMap<String, Object> newInfo = new HashMap<>();
            String title = info.get("title").asString();
            newInfo.put("title", title);
            infoStartTime = Math.max(infoStartTime, startEpochMilli);
            Double left = (infoStartTime - startEpochMilli) / (double) totalRange * 100;
            newInfo.put("left", left);
            infoEndTime = Math.min(infoEndTime, endEpochMilli);
            Double width = (infoEndTime - infoStartTime) / (double) totalRange * 100;
            newInfo.put("width", width);
            String label = String.format("(%s - %s)", infoStartStr, infoEndStr);
            newInfo.put("label", label);

            infos.add(newInfo);
        }
        model.addAttribute("infos", infos);
        return "ba/BaCalendar";
    }

}