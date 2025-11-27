package com.kuroneko.cqbot.controller;

import com.alibaba.fastjson2.JSONObject;
import com.kuroneko.cqbot.entity.WordCloud;
import com.kuroneko.cqbot.exception.BotException;
import com.kuroneko.cqbot.service.*;
import com.kuroneko.cqbot.utils.HttpUtil;
import com.kuroneko.cqbot.utils.RegexUtil;
import com.kuroneko.cqbot.vo.BiliDynamicVo;
import com.kuroneko.cqbot.vo.TarKovMarketVo;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;
import java.time.Instant;
import java.time.LocalDateTime;
import java.util.Base64;
import java.util.Collection;
import java.util.Collections;
import java.util.Optional;
import java.util.regex.Matcher;

@Slf4j
@RestController
@RequiredArgsConstructor
public class TestController {
    private final BiLiService biLiService;
    private final BulletService bulletService;
    private final RestTemplate restTemplate;
    private final AiService aiService;
    private final TarKovMarketService tarKovMarketService;
    private final WordCloudService wordCloudService;
    private final DeltaService deltaService;

    @RequestMapping(value = "/testBiLi")
    public BiliDynamicVo.BiliDynamicCard testBiLi() {
        Optional<BiliDynamicVo.BiliDynamicCard> firstCard = biLiService.getFirstCard("152065343");
        return firstCard.orElse(null);
    }

    @RequestMapping(value = "/testRedis")
    public Collection<String> testRedis() {
        LocalDateTime localDateTime = LocalDateTime.now().minusDays(10);
        wordCloudService.lambdaUpdate().lt(WordCloud::getTime, localDateTime).remove();
        return Collections.emptyList();
    }

    @RequestMapping(value = "/testAi")
    public String testAi() {
        String scnetDS = aiService.getScnetDS2(1, "你好");
        log.info(scnetDS);
        return scnetDS;
    }

    @RequestMapping(value = "/testTarKovMarket")
    public Object testTarKovMarket() {
        Collection<TarKovMarketVo> search = tarKovMarketService.search("btc");
        return search;
    }

    @RequestMapping(value = "/testDelta")
    public JSONObject testDelta() {
        deltaService.updateCertificate();
        return deltaService.getSwatUpgradeData();
    }

    @RequestMapping(value = "/testH")
    public String testH() throws IOException, InterruptedException {
        String finalKeyB64 = "";
        String m = "";
        String user_agent = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/131.0.0.0 Safari/537.36";
        m = getM(user_agent, m);
        finalKeyB64 = getFinalKeyB64(user_agent, Long.parseLong(m));

        System.out.println("key: " + finalKeyB64);
        String image_path = "C:\\Users\\Nekoko\\Pictures\\KuroNeko.jpg";
        String url = "https://soutubot.moe/api/search";


        byte[] imageData = Files.readAllBytes(Path.of(image_path));
        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
//        FileSystemResource resource = new FileSystemResource(new File(image_path));
        ByteArrayResource resource = new ByteArrayResource(imageData);
        body.add("file", resource);
        body.add("factor", "1.2");

        HttpClient httpClient = HttpUtil.getHttpClient();
        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .timeout(Duration.ofMinutes(1))
                .header("Content-Type", "multipart/form-data")
                .header("User-Agent", user_agent)
                .header("X-Api-Key", finalKeyB64)
                .header("X-Requested-With", "XMLHttpRequest");
        HttpUtil.ofMultipartData(body, requestBuilder);
        HttpResponse<String> send = httpClient.send(requestBuilder.build(), HttpResponse.BodyHandlers.ofString());
        String bytes = send.body();

//
//        HttpHeaders headers = new HttpHeaders();
//        headers.setContentType(MediaType.MULTIPART_FORM_DATA);
//        headers.set("Referer", "https://soutubot.moe/");
//        headers.setOrigin("https://soutubot.moe");
//        headers.set(HttpHeaders.USER_AGENT, user_agent);
//        headers.set("X-Api-Key", finalKeyB64);
//        headers.set("X-Requested-With", "XMLHttpRequest");
//
//        byte[] imageData = Files.readAllBytes(Path.of(image_path));
//        MultiValueMap<String, Object> body = new LinkedMultiValueMap<>();
////        FileSystemResource resource = new FileSystemResource(new File(image_path));
//        ByteArrayResource resource = new ByteArrayResource(imageData) {
//            @Override
//            public String getFilename() {
//                return "image";
//            }
//        };
//        body.add("file", resource);
//        body.add("factor", "1.2");
//
//        HttpEntity<MultiValueMap<String, Object>> requestEntity = new HttpEntity<>(body, headers);
//        String bytes;
//        ResponseEntity<String> entity = restTemplate.postForEntity(url, requestEntity, String.class);
//        bytes = entity.getBody();
        return bytes;
    }

    private static String getM(String user_agent, String m) {
        try (HttpClient httpClient = HttpClient.newBuilder()
                .followRedirects(HttpClient.Redirect.ALWAYS)
                .connectTimeout(Duration.ofSeconds(60L))
                .build()) {
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create("https://soutubot.moe"))
                    .timeout(Duration.ofMinutes(1))
                    .header("User-Agent", user_agent)
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            String body = response.body();
            // m: 2634375701776
            Optional<Matcher> matcher = RegexUtil.matcherFind("m:\\s*(\\d+)", body);
            if (matcher.isPresent()) {
                m = matcher.get().group(1);
            }
        } catch (Exception e) {
            throw new BotException(e.getMessage());
        }
        return m;
    }

    private static String getFinalKeyB64(String user_agent, long m) {
        long ts = Instant.now().getEpochSecond();

        long key = Math.round(Math.pow(ts, 2) + Math.pow(user_agent.length(), 2) + m) / 1000 * 1000;
        String keyStr = Long.toString(key);

        byte[] keyBytes = keyStr.getBytes(StandardCharsets.UTF_8);
        String keyB64 = Base64.getEncoder().encodeToString(keyBytes);

        StringBuilder reversedKeyB64 = new StringBuilder(keyB64).reverse();
        return reversedKeyB64.toString().replaceAll("=", "");
    }

}
