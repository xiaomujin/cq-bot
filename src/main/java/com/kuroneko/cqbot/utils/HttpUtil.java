package com.kuroneko.cqbot.utils;

import com.kuroneko.cqbot.exception.BotException;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.HashMap;


@Slf4j
public class HttpUtil {
    private HttpUtil() {
    }

    public static HttpClient getHttpClient() {
        return getHttpClient(60L);
    }

    public static HttpClient getHttpClient(long connectTimeout) {
        return HttpClient.newBuilder()
//                .version(HttpClient.Version.HTTP_1_1)
                .followRedirects(HttpClient.Redirect.NORMAL)
                .connectTimeout(Duration.ofSeconds(connectTimeout))
                .build();
    }

    public static String getRedirect(String url) {
        try (HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(60L))
                .build()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(url))
                    .timeout(Duration.ofMinutes(1))
                    .GET()
                    .build();
            HttpResponse<Void> response = httpClient.send(request, HttpResponse.BodyHandlers.discarding());
            return response.uri().toString();
        } catch (Exception e) {
            throw new BotException(e.getMessage());
        }
    }

    /**
     * get请求
     */
    public static String get(HttpClient httpClient, String url) {
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .GET();
        HttpRequest request = requestBuilder.build();
        return request(httpClient, url, request);
    }

    /**
     * 获取响应结果
     */
    public static String request(HttpClient httpClient, String url, HttpRequest request) {
        try (httpClient) {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            return response.body();
        } catch (IOException | InterruptedException e) {
            log.error("httpClient getResponse error.url:{}", url, e);
            throw new BotException(e.getMessage());
        }
    }

    public static String get(String url) {
        HttpClient httpClient = getHttpClient();
        return get(httpClient, url);
    }
}
