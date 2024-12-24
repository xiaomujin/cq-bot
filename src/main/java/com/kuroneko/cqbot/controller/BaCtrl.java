package com.kuroneko.cqbot.controller;

import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
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
        JSONObject httpObject = new JSONObject();
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpObject, headers);
        ResponseEntity<JSONObject> seasonList = restTemplate.exchange("https://api.arona.icu/api/season/list", HttpMethod.POST, httpEntity, JSONObject.class);
        JSONObject seasonListBody = seasonList.getBody();
        if (!seasonList.getStatusCode().is2xxSuccessful() || seasonListBody == null || !seasonListBody.getInteger("code").equals(200)) {
            throw new BotException("获取总力战数据失败！");
        }
        JSONArray seasonListBodyData = seasonListBody.getJSONArray("data");
        JSONObject seasonInfo = seasonListBodyData.getJSONObject(0);
        model.addAttribute("serverName", "官服");
        Integer season = seasonInfo.getInteger("season");
        String mapName = seasonInfo.getJSONObject("map").getString("value");
        String bossName = seasonInfo.getString("boss");
        String formatSeasonInfo = String.format("第%d期 %s %s", season, mapName, bossName);
        model.addAttribute("seasonInfo", formatSeasonInfo);

        ResponseEntity<JSONObject> listTop = restTemplate.exchange("https://api.arona.icu/api/v2/rank/list_top", HttpMethod.POST, httpEntity, JSONObject.class);
        JSONObject listTopBody = listTop.getBody();
        if (!seasonList.getStatusCode().is2xxSuccessful() || listTopBody == null || !listTopBody.getInteger("code").equals(200)) {
            throw new BotException("获取总力战数据失败！");
        }
        JSONArray listTopBodyData = listTopBody.getJSONArray("data");
        model.addAttribute("listTopBodyData", listTopBodyData);

        httpObject.clear();
        httpObject.put("server", 1);//服务器 1国服，2B服
        httpObject.put("season", season);//期数，latest默认当前最新，可填数值查询具体期数
        httpObject.put("dataType", 0);//1上边界，0下边界。不带这个参数时默认返回全部
        httpObject.put("tryNumber", 0);//0难度，0以上则是出刀边界。不带这个参数时默认返回全部
        ResponseEntity<JSONObject> lastRank = restTemplate.exchange("https://api.arona.icu/api/v2/rank/list_by_last_rank", HttpMethod.POST, httpEntity, JSONObject.class);
        JSONObject lastRankBody = lastRank.getBody();
        if (!seasonList.getStatusCode().is2xxSuccessful() || lastRankBody == null || !lastRankBody.getInteger("code").equals(200)) {
            throw new BotException("获取总力战数据失败！");
        }
        JSONArray lastRankBodyData = lastRankBody.getJSONArray("data");
        model.addAttribute("lastRankBodyData", lastRankBodyData);

        httpObject.clear();
        httpObject.put("server", 1);//服务器 1国服，2B服
        httpObject.put("season", season);//期数，latest默认当前最新，可填数值查询具体期数
        httpObject.put("type", 0);//查询类型，1常规，2档线
        httpObject.put("page", 1);
        httpObject.put("size", 50);
        ResponseEntity<JSONObject> rankList = restTemplate.exchange("https://api.arona.icu/api/v2/rank/list", HttpMethod.POST, httpEntity, JSONObject.class);
        JSONObject rankListBody = rankList.getBody();
        if (!seasonList.getStatusCode().is2xxSuccessful() || rankListBody == null || !rankListBody.getInteger("code").equals(200)) {
            throw new BotException("获取总力战数据失败！");
        }
        JSONObject rankListBodyData = rankListBody.getJSONObject("data");
        JSONArray records = rankListBodyData.getJSONArray("records");
        model.addAttribute("records", records);
        Long recordTime = records.getJSONObject(0).getLong("recordTime");
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
        JSONArray tableHeadList = new JSONArray();
        ArrayList<Integer> dateList = new ArrayList<>();
        ArrayList<String> weekList = new ArrayList<>();
        LocalDateTime startTime = tempDay.atStartOfDay();
        int tempDayMonthValue = tempDay.getMonthValue();
        for (int i = 0; i < 13; i++) {
            if (tempDayMonthValue != tempDay.getMonthValue()) {
                JSONObject jsonObject = new JSONObject();
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
        JSONObject jsonObject = new JSONObject();
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
        JSONObject httpObject = new JSONObject();
        httpObject.put("server", 1);
        HttpEntity<Object> httpEntity = new HttpEntity<>(httpObject, headers);
        ResponseEntity<JSONObject> seasonList = restTemplate.exchange("https://api.arona.icu/api/events/v2/info", HttpMethod.POST, httpEntity, JSONObject.class);
        JSONObject calendarListBody = seasonList.getBody();
        if (!seasonList.getStatusCode().is2xxSuccessful() || calendarListBody == null || !calendarListBody.getInteger("code").equals(200)) {
            throw new BotException("获取日历数据失败！");
        }
        JSONArray calendarBodyData = calendarListBody.getJSONArray("data");
        JSONArray infos = new JSONArray();
        for (int i = 0; i < calendarBodyData.size(); i++) {
            JSONObject info = calendarBodyData.getJSONObject(i);
            long infoEndTime = info.getLongValue("endTime");
            if (infoEndTime < startEpochMilli + 1000 * 60 * 60 * 24 * 3) {
                continue;
            }
            long infoStartTime = info.getLongValue("startTime");
            if (infoStartTime > endEpochMilli + 1000 * 60 * 60 * 24 * 3) {
                continue;
            }
            String infoStartStr = LocalDateTime.ofInstant(Instant.ofEpochMilli(infoStartTime), ZoneId.systemDefault()).format(formatter2);
            String infoEndStr = LocalDateTime.ofInstant(Instant.ofEpochMilli(infoEndTime), ZoneId.systemDefault()).format(formatter2);
            JSONObject newInfo = new JSONObject();
            String title = info.getString("title");
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
