package com.kuroneko.cqbot.service;

import cn.hutool.crypto.digest.DigestUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.kuroneko.cqbot.constant.Constant;
import jdk.jfr.consumer.EventStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.IOUtils;
import org.springframework.core.io.Resource;
import org.springframework.http.*;
import org.springframework.stereotype.Service;
import org.springframework.web.client.ResponseExtractor;
import org.springframework.web.client.RestTemplate;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Service
@Slf4j
public class AiService {
    private final RestTemplate restTemplate;

    public AiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public String getWuGuoKai(long groupId, String text) {
        long now = System.currentTimeMillis();
        Long room = Constant.AI_ROOM.getOrDefault(groupId, now);
        if (room + 3600000 < now) {
            room = now;
        }
        Constant.AI_ROOM.put(groupId, room);
        HashMap<String, Object> map = new HashMap<>();
        map.put("prompt", text);
        map.put("userId", "#/chat/" + room);
        map.put("network", true);
        map.put("system", "");
        map.put("withoutContext", false);
        map.put("stream", false);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Connection", "keep-alive");
        headers.set("Referer", "https://chat18.aichatos.xyz/");
        headers.setOrigin("https://chat18.aichatos.xyz");
        headers.set(HttpHeaders.USER_AGENT, "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36");

        HttpEntity<HashMap<String, Object>> requestEntity = new HttpEntity<>(map, headers);
        byte[] bytes;
        try {
            ResponseEntity<Resource> entity = restTemplate.postForEntity("https://api.binjie.fun/api/generateStream", requestEntity, Resource.class);
            assert entity.getBody() != null;
            bytes = entity.getBody().getInputStream().readAllBytes();
        } catch (IOException e) {
            log.error("提问获取失败", e);
            return "emmmm，出错了";
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    public String getFreeGpt(long groupId, String text) {
        HashMap<String, Object> map = new HashMap<>();
        map.put("content", text);
        map.put("role", "user");
        ArrayList<Object> messages = new ArrayList<>();
        messages.add(map);
        long time = System.currentTimeMillis();
        String sign = DigestUtil.sha256Hex(time + ":" + text + ":undefined");

        HashMap<String, Object> request = new HashMap<>();
        request.put("messages", messages);
        request.put("time", time);
        request.put("sign", sign);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(request, headers);
        byte[] bytes;
        try {
            ResponseEntity<Resource> entity = restTemplate.postForEntity("https://freegpt.chinaproxy.top/api/generate", requestEntity, Resource.class);
            assert entity.getBody() != null;
            bytes = entity.getBody().getInputStream().readAllBytes();
        } catch (IOException e) {
            log.error("提问获取失败", e);
            return "emmmm，出错了";
        }
        return new String(bytes, StandardCharsets.UTF_8);
    }

    private HashMap<Long, TokenTime> DS_TOKEN_MAP = new HashMap<>();

    public String getScnetDS(long groupId, String text) {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("Referer", "https://chat.scnet.cn/");
        headers.set("Origin", "https://chat.scnet.cn");
        headers.set("User-Agent", "Mozilla/5.0 (iPhone; CPU iPhone OS 16_6 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/16.6 Mobile/15E148 Safari/604.1");
        HashMap<String, Object> request = new HashMap<>();
        TokenTime tokenTime = DS_TOKEN_MAP.get(groupId);
        if (tokenTime == null || tokenTime.time + 86400000 < System.currentTimeMillis()) {
            HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(request, headers);
            ResponseEntity<String> loginTmp = restTemplate.postForEntity("https://chat.scnet.cn/api/oauth/LoginTemp", requestEntity, String.class);
            JSONObject loginTmpObject = JSON.parseObject(loginTmp.getBody());
            assert loginTmpObject != null;
            String das_app_client_token_id = loginTmpObject.getString("data");
            tokenTime = new TokenTime();
            tokenTime.token = das_app_client_token_id;
            tokenTime.time = System.currentTimeMillis();
            DS_TOKEN_MAP.put(groupId, tokenTime);
            log.info("group:{} 获取新Token：{}", groupId, das_app_client_token_id);
        }
        String modelType = "DeepSeek-R1-Distill-Qwen-7B";
//        String modelType = "DeepSeek-R1-Distill-Qwen-32B";
        request.put("modelType", modelType);
        request.put("query", text);
        request.put("conversationId", tokenTime.conversationId);
        headers.set("Cookie", "das_app_client_token_id=" + tokenTime.token);

        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(request, headers);
        ResponseEntity<String> ask = restTemplate.postForEntity("https://chat.scnet.cn/api/chat/Ask", requestEntity, String.class);
        JSONObject askObject = JSON.parseObject(ask.getBody());
        assert askObject != null;
        JSONObject askData = askObject.getJSONObject("data");
        String messageId = askData.getString("messageId");
        tokenTime.conversationId = askData.getJSONObject("conversation").getString("ConversationId");
        request.clear();
        requestEntity = new HttpEntity<>(request, headers);

        BufferedReader reader = null;
        StringBuilder resBody = new StringBuilder();
        try {
            ResponseEntity<Resource> entity = restTemplate.exchange("https://chat.scnet.cn/api/chat/GetReplay?messageId=" + messageId + "&query=&modelType=" + modelType, HttpMethod.GET, requestEntity, Resource.class);
            assert entity.getBody() != null;
            reader = new BufferedReader(new InputStreamReader(entity.getBody().getInputStream(), StandardCharsets.UTF_8));
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.startsWith("data: ")) {
                        String data = line.substring("data: ".length());
                        // 处理接收到的数据
//                        log.info("Received data: {}", data);
                        if (data.startsWith("\"")) {
                            resBody.append(data, 1, data.length() - 1);
                        }
                    }
                }
        } catch (Exception e) {
            log.error("提问获取失败", e);
            return "emmmm，出错了";
        }finally {
            IOUtils.closeQuietly(reader);
        }
        String bodyString = resBody.toString();
        String answer = bodyString.substring(bodyString.lastIndexOf("think") + 15);
        String replace = answer.replace("\\n\\n", "\n").replace("\\n", "\n").replace("\\\"", "\"");
        return replace;
    }

    static class TokenTime {
        public String conversationId = "";
        public String token;
        public long time;
    }

}
