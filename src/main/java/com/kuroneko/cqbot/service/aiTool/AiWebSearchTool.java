package com.kuroneko.cqbot.service.aiTool;

import com.kuroneko.cqbot.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.node.ArrayNode;
import tools.jackson.databind.node.ObjectNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.*;

@Component
@Slf4j
public class AiWebSearchTool {
    private static final String DEFAULT_TAVILY_SEARCH_ENDPOINT = "https://api.tavily.com/search";
    private static final String DEFAULT_TAVILY_EXTRACT_ENDPOINT = "https://api.tavily.com/extract";
    private static final String TOPIC_GENERAL = "general";
    private static final String TOPIC_NEWS = "news";
    private static final String SEARCH_DEPTH = "advanced";
    private static final String EXTRACT_DEPTH = "basic";
    private static final String EXTRACT_FORMAT = "markdown";
    private static final int MAX_ALLOWED_RESULTS = 10;
    private static final int EXTRACT_CHUNKS_PER_SOURCE = 3;
    private static final List<String> FRESHNESS_KEYWORDS = List.of(
            "最新", "今天", "今日", "最近", "近期", "刚刚", "版本", "更新", "发布", "新闻",
            "latest", "news", "release", "released", "update", "updated", "announcement"
    );

    private final int defaultMaxResults;
    private final int timeoutMillis;
    private final String apiKey;
    private final String searchEndpoint;
    private final String extractEndpoint;
    private final HttpClient httpClient;

    public AiWebSearchTool(@Value("${bot.ai.web-search.max-results:5}") int defaultMaxResults,
                           @Value("${bot.ai.web-search.timeout-millis:10000}") int timeoutMillis,
                           @Value("${bot.ai.web-search.api-key:}") String apiKey) {
        this.defaultMaxResults = Math.max(1, Math.min(defaultMaxResults, MAX_ALLOWED_RESULTS));
        this.timeoutMillis = Math.max(1000, timeoutMillis);
        this.apiKey = cleanText(apiKey);
        this.searchEndpoint = DEFAULT_TAVILY_SEARCH_ENDPOINT;
        this.extractEndpoint = DEFAULT_TAVILY_EXTRACT_ENDPOINT;
        this.httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofMillis(this.timeoutMillis))
                .build();
    }

    @Tool(name = "web_search", description = "搜索互联网最新信息并返回简明结果。适用于新闻、版本发布、价格、官网、文档、当前事件、比赛结果等需要实时网络信息的问题。query 是搜索关键词；maxResults 是返回结果数量，范围 1 到 10，可选。")
    public String webSearch(@ToolParam(description = "搜索关键词，应尽量具体，必要时包含时间、版本、网站名或主体") String query,
                            @ToolParam(description = "返回结果数量，范围 1 到 10，可选", required = false) Integer maxResults) {
        if (!StringUtils.hasText(query)) {
            return buildLocalErrorResponse("搜索关键词不能为空");
        }
        String normalizedQuery = cleanText(query);
        boolean freshnessSensitive = isFreshnessSensitiveQuery(normalizedQuery);
        int limit = Math.max(1, Math.min(maxResults == null ? defaultMaxResults : maxResults, MAX_ALLOWED_RESULTS));
        log.info("web_search 执行, query={}", normalizedQuery);

        if (!StringUtils.hasText(apiKey)) {
            log.warn("web_search 缺少 Tavily API key, query={}", normalizedQuery);
            return buildLocalErrorResponse("web_search 缺少 Tavily API key");
        }

        try {
            TavilyResponse searchResponse = executeTavilySearch(normalizedQuery, freshnessSensitive, limit);
            if (!searchResponse.isSuccessful()) {
                log.warn("web_search Tavily search 失败, query={}, statusCode={}", normalizedQuery, searchResponse.statusCode());
                return StringUtils.hasText(searchResponse.body()) ? searchResponse.body() : buildLocalErrorResponse("web_search 执行失败，请稍后再试");
            }
            String responseBody = StringUtils.hasText(searchResponse.body()) ? searchResponse.body() : buildLocalErrorResponse("web_search 执行失败，请稍后再试");
//            responseBody = enrichSearchResponseWithExtract(responseBody, normalizedQuery);
            log.info("web_search 执行完成, query={}", normalizedQuery);
            return responseBody;
        } catch (Exception e) {
            log.warn("web_search Tavily 执行失败, query={}", normalizedQuery, e);
            return buildLocalErrorResponse("web_search 执行失败，请稍后再试");
        }
    }

    protected TavilyResponse executeTavilySearch(String query, boolean freshnessSensitive, int maxResults) throws Exception {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("query", query);
        requestBody.put("topic", freshnessSensitive ? TOPIC_NEWS : TOPIC_GENERAL);
        requestBody.put("search_depth", SEARCH_DEPTH);
        requestBody.put("include_answer", false);
        requestBody.put("include_images", false);
        requestBody.put("include_favicon", false);
        requestBody.put("include_raw_content", false);
        requestBody.put("max_results", maxResults);
        return executeTavilyRequest(searchEndpoint, requestBody);
    }

    protected TavilyResponse executeTavilyExtract(List<String> urls, String query) throws Exception {
        Map<String, Object> requestBody = new LinkedHashMap<>();
        requestBody.put("urls", urls);
        requestBody.put("query", query);
        requestBody.put("chunks_per_source", EXTRACT_CHUNKS_PER_SOURCE);
        requestBody.put("extract_depth", EXTRACT_DEPTH);
        requestBody.put("include_images", true);
        requestBody.put("include_favicon", true);
        requestBody.put("format", EXTRACT_FORMAT);
        return executeTavilyRequest(extractEndpoint, requestBody);
    }

    boolean isFreshnessSensitiveQuery(String query) {
        if (!StringUtils.hasText(query)) {
            return false;
        }
        String normalized = query.toLowerCase(Locale.ROOT);
        for (String keyword : FRESHNESS_KEYWORDS) {
            if (normalized.contains(keyword.toLowerCase(Locale.ROOT))) {
                return true;
            }
        }
        return false;
    }

    private TavilyResponse executeTavilyRequest(String endpoint, Map<String, Object> requestBody) throws Exception {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(endpoint))
                .timeout(Duration.ofMillis(timeoutMillis))
                .header("Authorization", "Bearer " + apiKey)
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtil.toString(requestBody), StandardCharsets.UTF_8))
                .build();
        HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
        return new TavilyResponse(response.statusCode(), response.body());
    }

    private String enrichSearchResponseWithExtract(String searchResponseBody, String query) {
        JsonNode searchRootNode = JsonUtil.toNode(searchResponseBody);
        if (!(searchRootNode instanceof ObjectNode searchRoot)) {
            return searchResponseBody;
        }
        if (!(searchRoot.path("results") instanceof ArrayNode searchResults) || searchResults.isEmpty()) {
            return searchResponseBody;
        }
        List<String> urls = collectResultUrls(searchResults);
        if (urls.isEmpty()) {
            return searchResponseBody;
        }

        try {
            TavilyResponse extractResponse = executeTavilyExtract(urls, query);
            if (!extractResponse.isSuccessful() || !StringUtils.hasText(extractResponse.body())) {
                log.warn("web_search Tavily extract 失败, query={}, statusCode={}", query, extractResponse.statusCode());
                return searchResponseBody;
            }
            JsonNode extractRootNode = JsonUtil.toNode(extractResponse.body());
            if (!(extractRootNode instanceof ObjectNode extractRoot)) {
                return searchResponseBody;
            }
            if (!(extractRoot.path("results") instanceof ArrayNode extractResults) || extractResults.isEmpty()) {
                return searchResponseBody;
            }
            Map<String, ObjectNode> extractResultsByUrl = indexExtractResults(extractResults);
            for (JsonNode searchResultNode : searchResults) {
                if (!(searchResultNode instanceof ObjectNode searchResult)) {
                    continue;
                }
                ObjectNode extractResult = extractResultsByUrl.get(cleanText(searchResult.path("url").asText("")));
                if (extractResult == null) {
                    continue;
                }
                copyExtractField(extractResult, searchResult, "raw_content");
                copyExtractField(extractResult, searchResult, "favicon");
                copyExtractField(extractResult, searchResult, "images");
            }
            return JsonUtil.toString(searchRoot);
        } catch (Exception e) {
            log.warn("web_search Tavily extract 执行失败, query={}", query, e);
            return searchResponseBody;
        }
    }

    private List<String> collectResultUrls(ArrayNode results) {
        List<String> urls = new ArrayList<>();
        Set<String> seenUrls = new LinkedHashSet<>();
        for (JsonNode resultNode : results) {
            String url = cleanText(resultNode.path("url").asText(""));
            if (StringUtils.hasText(url) && seenUrls.add(url)) {
                urls.add(url);
            }
        }
        return urls;
    }

    private Map<String, ObjectNode> indexExtractResults(ArrayNode extractResults) {
        Map<String, ObjectNode> resultsByUrl = new LinkedHashMap<>();
        for (JsonNode extractResultNode : extractResults) {
            if (!(extractResultNode instanceof ObjectNode extractResult)) {
                continue;
            }
            String url = cleanText(extractResult.path("url").asText(""));
            if (StringUtils.hasText(url)) {
                resultsByUrl.put(url, extractResult);
            }
        }
        return resultsByUrl;
    }

    private void copyExtractField(ObjectNode extractResult, ObjectNode searchResult, String fieldName) {
        JsonNode fieldValue = extractResult.get(fieldName);
        if (fieldValue != null && !fieldValue.isNull()) {
            searchResult.set(fieldName, fieldValue.deepCopy());
        }
    }

    private String buildLocalErrorResponse(String message) {
        ObjectNode root = JsonUtil.mapper.createObjectNode();
        root.putObject("detail").put("error", message);
        return JsonUtil.toString(root);
    }

    private String cleanText(String text) {
        return text == null ? "" : text.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
    }

    record TavilyResponse(int statusCode, String body) {
        boolean isSuccessful() {
            return statusCode >= 200 && statusCode < 300;
        }
    }
}
