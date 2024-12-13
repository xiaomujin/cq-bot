package com.kuroneko.cqbot.utils;

import com.kuroneko.cqbot.exception.BotException;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.Resource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.MultiValueMap;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;


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

    public static void ofMultipartData(MultiValueMap<String, Object> data, HttpRequest.Builder builder) {
        String boundary = MimeTypeUtils.generateMultipartBoundaryString();
        builder.setHeader(HttpHeaders.CONTENT_TYPE, MediaType.MULTIPART_FORM_DATA_VALUE + "; boundary=" + boundary);
        try (var out = new ByteArrayOutputStream()) {
            for (var entry : data.toSingleValueMap().entrySet()) {
                if (entry.getValue() instanceof Resource resource) {
                    print(out, "--" + boundary);
                    println(out);
                    print(out, "Content-Disposition: form-data; name=\"" + entry.getKey() + "\"; filename=\"" + resource.getFilename() + "\"");
                    println(out);
                    print(out, "Content-Disposition: form-data; name=\"" + entry.getKey() + "\"");
                    println(out);
                    println(out);
                    try (InputStream in = resource.getInputStream()) {
                        in.transferTo(out);
                    }
                    println(out);
                } else {
                    print(out, "--" + boundary);
                    println(out);
                    print(out, "Content-Disposition: form-data; name=\"" + entry.getKey() + "\"");
                    println(out);
                    println(out);
                    print(out, String.valueOf(entry.getValue()));
                    println(out);
                }
            }

            print(out, "--" + boundary + "--");
            println(out);
            builder.POST(HttpRequest.BodyPublishers.ofByteArray(out.toByteArray()));
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    private static void println(OutputStream os) throws IOException {
        os.write('\r');
        os.write('\n');
    }

    private static void print(OutputStream os, String buf) throws IOException {
        os.write(buf.getBytes(StandardCharsets.US_ASCII));
    }
}
