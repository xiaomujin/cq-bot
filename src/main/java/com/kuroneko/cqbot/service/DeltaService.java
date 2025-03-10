package com.kuroneko.cqbot.service;

import com.alibaba.fastjson2.JSONObject;
import com.kuroneko.cqbot.exception.BotException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
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
        JSONObject resJSONObject = getResJSONObject("https://www.kkrb.net/getMenu");
        version = resJSONObject.getString("built_ver");
    }

    public JSONObject getSwatUpgradeData() {
        return getResJSONObject("https://www.kkrb.net/getSwatUpgradeData");
    }

    public JSONObject getResJSONObject(String url) {
        HttpEntity<String> httpEntity = getHttpEntity();
        ResponseEntity<JSONObject> res = restTemplate.exchange(url, HttpMethod.POST, httpEntity, JSONObject.class);
        JSONObject resBody = res.getBody();
        assert resBody != null;
        Integer code = resBody.getInteger("code");
        if (!code.equals(1)) {
            throw new BotException("获取数据失败");
        }
        List<String> cookies = res.getHeaders().get("Set-Cookie");
        if (cookies != null) {
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
        return resBody;
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
        return headers;
    }

    private HttpEntity<String> getHttpEntity() {
        HttpHeaders headers = getHeader();
        String versionStr = "version=" + version;
        if (version == null || version.isEmpty()) {
            versionStr = null;
        }
        return new HttpEntity<>(versionStr, headers);
    }


}
