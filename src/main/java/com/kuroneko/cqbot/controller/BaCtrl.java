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
import java.util.Date;

@Controller
@RequiredArgsConstructor
public class BaCtrl {
    private final RestTemplate restTemplate;

    @RequestMapping(value = {"/ba"})
    public String Bullet(Model model) {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Authorization", Constant.BA_TOKEN);
//        headers.add("Authorization", "ba-token uuz:uuz");
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

}
