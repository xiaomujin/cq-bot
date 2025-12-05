package com.kuroneko.cqbot.service;

import com.kuroneko.cqbot.dto.KkrbData;
import com.kuroneko.cqbot.exception.BotException;
import com.kuroneko.cqbot.utils.JsonUtil;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.web.client.RestTemplate;
import tools.jackson.databind.JsonNode;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DeltaForceService {
    private final RestTemplate restTemplate;
    private KkrbData.OVData ovData;
    private String cookie = "";
    private String version = "";

    public void updateCertificate() {
        // 使用通用方法处理GET请求
        HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = getHttpEntity();
        ResponseEntity<String> res = restTemplate.exchange("https://www.kkrb.net/?viewpage=view%2Foverview", HttpMethod.GET, httpEntity, String.class);
        updateCookie(res.getHeaders());

        // 再发送POST请求获取版本信息
        HttpEntity<LinkedMultiValueMap<String, Object>> versionEntity = getHttpEntity();
        versionEntity.getBody().add("globalData", false);
        ResponseEntity<JsonNode> versionRes = restTemplate.exchange("https://www.kkrb.net/getMenu", HttpMethod.POST, versionEntity, JsonNode.class);
        updateCookie(versionRes.getHeaders());
        if (versionRes.getBody() != null) {
            version = versionRes.getBody().get("built_ver").asString();
        }
    }

    public KkrbData.OVData getOVData() {
        if (ovData != null) {
            return ovData;
        }
        LinkedMultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
        body.add("globalData", false);
        KkrbData<KkrbData.OVData> res = getResJSONObject("https://www.kkrb.net/getOVData", body, new ParameterizedTypeReference<>() {
        });
        ovData = res.getData();
        return ovData;
    }

    public <T> KkrbData<T> getResJSONObject(String url, LinkedMultiValueMap<String, Object> body, ParameterizedTypeReference<KkrbData<T>> typeReference) {
        HttpEntity<LinkedMultiValueMap<String, Object>> httpEntity = getHttpEntity();
        if (body != null) {
            httpEntity.getBody().putAll(body);
        }
        ResponseEntity<KkrbData<T>> res = restTemplate.exchange(url, HttpMethod.POST, httpEntity, typeReference);
        updateCookie(res.getHeaders());
        KkrbData<T> resBody = res.getBody();
        if (resBody == null) {
            throw new BotException("请求失败，响应体为空");
        }
        Integer code = resBody.getCode();
        if (!code.equals(1)) {
            throw new BotException("获取数据失败，错误码：" + code);
        }
        return resBody;
    }

    private void updateCookie(HttpHeaders headers) {
        List<String> cookies = headers.get("Set-Cookie");
        if (cookies != null) {
            for (String cookieOne : cookies) {
                String[] parts = cookieOne.split(";\s*");
                for (String part : parts) {
                    part = part.trim();
                    if (part.startsWith("PHPSESSID=")) {
                        cookie = part;
                        break;
                    }
                }
            }
        }
    }

    private HttpHeaders getHeader() {
        HttpHeaders headers = new HttpHeaders();
        headers.add("Origin", "https://www.kkrb.net");
        headers.add("Referer", "https://www.kkrb.net/");
        headers.add("User-Agent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/142.0.0.0 Safari/537.36");
        headers.add("X-Requested-With", "XMLHttpRequest");
        headers.add("Content-Type", "application/x-www-form-urlencoded; charset=UTF-8");
        if (cookie != null && !cookie.isEmpty()) {
            headers.set("Cookie", cookie);
        }
        return headers;
    }

    private HttpEntity<LinkedMultiValueMap<String, Object>> getHttpEntity() {
        HttpHeaders headers = getHeader();
        LinkedMultiValueMap<String, Object> hashMap = new LinkedMultiValueMap<>();
        if (version != null && !version.isEmpty()) {
            hashMap.add("version", version);
        }
        return new HttpEntity<>(hashMap, headers);
    }

    @Scheduled(cron = "10 0 0 * * ? ")
    public void dailyClean() {
        log.info("清理df数据 开始");
        ovData = null;
        log.info("清理df数据 结束");
    }

}