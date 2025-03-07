package com.kuroneko.cqbot.service;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import okhttp3.Headers;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeltaService {
    private final RestTemplate restTemplate;
    private String phpSessId = "";
    private String version = "";

    public void updateCertificate() {
        HttpHeaders headers = getHeader();
        HttpEntity<Object> httpEntity = new HttpEntity<>(null, headers);
        ResponseEntity<String> menuRes = restTemplate.exchange("https://www.kkrb.net/getMenu", HttpMethod.POST, httpEntity, String.class);
        JSONObject menuBody = JSON.parseObject(menuRes.getBody());
        assert menuBody != null;
        Integer code = menuBody.getInteger("code");
        if (!code.equals(1)) {
            log.error("DeltaService 获取证书失败");
            return;
        }
        version = menuBody.getString("built_ver");

        List<String> cookies = menuRes.getHeaders().get("Set-Cookie");
        assert cookies != null;
        for (String cookie : cookies) {
            String[] parts = cookie.split(";");
            for (String part : parts) {
                if (part.trim().startsWith("PHPSESSID=")) {
                    phpSessId = part.trim().substring("PHPSESSID=".length());
                    break;
                }
            }
        }
    }

    private Headers getHeader2() {
        return new Headers.Builder()
                .add("Origin", "https://www.kkrb.net")
                .add("Referer", "https://www.kkrb.net/")
                .add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36")
                .add("X-Requested-With", "XMLHttpRequest")
                .build();
    }

    public JSONObject getSwatUpgradeData() {
        HttpHeaders headers = getHeader();
        HttpEntity<Object> httpEntity = new HttpEntity<>("version=" + version, headers);
        ResponseEntity<JSONObject> menuRes = restTemplate.exchange("https://www.kkrb.net/getSwatUpgradeData", HttpMethod.POST, httpEntity, JSONObject.class);
        JSONObject menuBody = menuRes.getBody();
        assert menuBody != null;
        Integer code = menuBody.getInteger("code");
        if (!code.equals(1)) {
            return null;
        }
        return menuBody;
    }

    private HttpHeaders getHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Origin", "https://www.kkrb.net");
        headers.add("Referer", "https://www.kkrb.net/");
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/133.0.0.0 Safari/537.36");
        headers.add("X-Requested-With", "XMLHttpRequest");
        headers.add("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        if (phpSessId == null || phpSessId.isEmpty()) {
            headers.set("Cookie", "PHPSESSID=" + phpSessId);
        }
//        headers.add("Connection", "keep-alive");
//        headers.add("Accept", "*/*");
//        headers.add("Accept-Encoding", "gzip, deflate, br");
        return headers;
    }


}
