package com.kuroneko.cqbot.service;

import com.kuroneko.cqbot.constant.Constant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;

@Service
@Slf4j
public class AiService {
    private final RestTemplate restTemplate;

    public AiService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }


    public String getWuGuoKai(long groupId, String text) {
        Long room = Constant.AI_ROOM.getOrDefault(groupId, System.currentTimeMillis());
        if (room + 3600000 < System.currentTimeMillis()) {
            room = System.currentTimeMillis();
        }
        Constant.AI_ROOM.put(groupId, room);
        HashMap<String, Object> map = new HashMap<>();
        map.put("prompt", text);
        map.put("options", null);
        map.put("userId", "#/chat/" + room);
        map.put("usingContext", true);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        headers.set("authority", "chat2.wuguokai.cn");
        headers.set("method", "POST");
        headers.set("path", "/api/chat-process");
        headers.set("scheme", "https");
        headers.set("referer", "https://chat.wuguokai.cn/");
        HttpEntity<Map<String, Object>> requestEntity = new HttpEntity<>(map, headers);
        ResponseEntity<Resource> entity = restTemplate.postForEntity("https://chat2.wuguokai.cn/api/chat-process", requestEntity, Resource.class);
        byte[] bytes;
        try {
            assert entity.getBody() != null;
            bytes = entity.getBody().getInputStream().readAllBytes();
        } catch (IOException e) {
            log.error("提问获取失败", e);
            return "emmmm，出错了";
        }
        return new String(bytes,Charset.defaultCharset());
    }

}
