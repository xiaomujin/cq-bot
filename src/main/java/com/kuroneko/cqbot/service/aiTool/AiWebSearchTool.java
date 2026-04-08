package com.kuroneko.cqbot.service.aiTool;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.parser.Parser;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.lang.Nullable;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;

import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Component
@Slf4j
public class AiWebSearchTool {
    private static final String BAIDU_ENDPOINT = "https://m.baidu.com/s";
    private static final String BING_ENDPOINT = "https://www.bing.com/search";
    private static final String DEFAULT_ENDPOINT = BING_ENDPOINT;
    private static final String BAIDU_REFERER = "https://m.baidu.com/";
    private static final String BING_REFERER = "https://www.bing.com/";
    private static final String MOBILE_USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 18_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.3 Mobile/15E148 Safari/604.1";
    private static final String DESKTOP_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36";
    private static final Pattern ABSOLUTE_URL_PATTERN = Pattern.compile("https?://[^\\s\"'<>]+", Pattern.CASE_INSENSITIVE);

    private final int defaultMaxResults;
    private final int timeoutMillis;
    private final String endpoint;

    public AiWebSearchTool(@Value("${bot.ai.web-search.max-results:5}") int defaultMaxResults,
                           @Value("${bot.ai.web-search.timeout-millis:10000}") int timeoutMillis,
                           @Value("${bot.ai.web-search.endpoint:" + DEFAULT_ENDPOINT + "}") String endpoint) {
        this.defaultMaxResults = Math.max(1, Math.min(defaultMaxResults, 10));
        this.timeoutMillis = Math.max(1000, timeoutMillis);
        this.endpoint = StringUtils.hasText(endpoint) ? endpoint.trim() : DEFAULT_ENDPOINT;
    }

    @Tool(name = "web_search", description = "搜索互联网最新信息并返回简明结果。适用于新闻、版本发布、价格、官网、文档、当前事件、比赛结果等需要实时网络信息的问题。query 是搜索关键词；maxResults 是返回结果数量，范围 1 到 10，可选。")
    public String webSearch(@ToolParam(description = "搜索关键词，应尽量具体，必要时包含时间、版本、网站名或主体") String query,
                            @Nullable @ToolParam(description = "返回结果数量，范围 1 到 10，可选", required = false) Integer maxResults) {
        if (!StringUtils.hasText(query)) {
            return "搜索关键词不能为空";
        }
        String normalizedQuery = query.trim();
        log.info("web_search 执行, query={}", normalizedQuery);
        int limit = Math.max(1, Math.min(maxResults == null ? defaultMaxResults : maxResults, 10));
        boolean fetchFailed = false;

        for (String candidateEndpoint : buildCandidateEndpoints()) {
            try {
                Document document = fetch(buildSearchUrl(candidateEndpoint, normalizedQuery), candidateEndpoint);
                List<SearchResult> results = parseResults(document, candidateEndpoint, limit);
                if (!results.isEmpty()) {
                    return formatResults(normalizedQuery, results);
                }
                log.warn("web_search 未解析到结果, query={}, endpoint={}", normalizedQuery, candidateEndpoint);
            } catch (Exception e) {
                fetchFailed = true;
                log.warn("web_search 搜索源执行失败, query={}, endpoint={}", normalizedQuery, candidateEndpoint, e);
            }
        }

        return fetchFailed
                ? "web_search 执行失败，请稍后再试"
                : "未找到与“" + normalizedQuery + "”相关的网页结果。";
    }

    private List<String> buildCandidateEndpoints() {
        List<String> endpoints = new ArrayList<>();
        addEndpoint(endpoints, endpoint);
        addEndpoint(endpoints, BING_ENDPOINT);
        addEndpoint(endpoints, BAIDU_ENDPOINT);
        return endpoints;
    }

    private void addEndpoint(List<String> endpoints, String candidate) {
        if (StringUtils.hasText(candidate) && !endpoints.contains(candidate)) {
            endpoints.add(candidate.trim());
        }
    }

    private Document fetch(String url, String baseUrl) throws Exception {
        return Jsoup.connect(url)
                .userAgent(resolveUserAgent(baseUrl))
                .referrer(resolveReferer(baseUrl))
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .timeout(timeoutMillis)
                .followRedirects(true)
                .get();
    }

    private String resolveUserAgent(String baseUrl) {
        return isBaiduEndpoint(baseUrl) ? MOBILE_USER_AGENT : DESKTOP_USER_AGENT;
    }

    private String resolveReferer(String baseUrl) {
        return isBaiduEndpoint(baseUrl) ? BAIDU_REFERER : BING_REFERER;
    }

    private String buildSearchUrl(String baseUrl, String query) {
        String separator = baseUrl.contains("?") ? "&" : "?";
        if (isBaiduEndpoint(baseUrl)) {
            return baseUrl + separator + "word=" + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&ie=utf-8";
        }
        return baseUrl + separator + "q=" + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&setlang=zh-cn";
    }

    private boolean isBaiduEndpoint(String baseUrl) {
        return baseUrl != null && baseUrl.contains("baidu.com");
    }

    private List<SearchResult> parseResults(Document document, String baseUrl, int limit) {
        List<SearchResult> results = isBaiduEndpoint(baseUrl)
                ? parseBaiduResults(document, limit)
                : parseBingResults(document, limit);
        if (!results.isEmpty()) {
            return results;
        }

        results = isBaiduEndpoint(baseUrl)
                ? parseBingResults(document, limit)
                : parseBaiduResults(document, limit);
        if (!results.isEmpty()) {
            return results;
        }

        return parseGenericResults(document, limit);
    }

    private List<SearchResult> parseBingResults(Document document, int limit) {
        List<SearchResult> results = new ArrayList<>();
        for (Element block : document.select("li.b_algo, .b_algo")) {
            SearchResult result = extractBingResult(block);
            if (result == null || containsUrl(results, result.url())) {
                continue;
            }
            results.add(result);
            if (results.size() >= limit) {
                break;
            }
        }
        return results;
    }

    private SearchResult extractBingResult(Element block) {
        if (block == null) {
            return null;
        }
        Element anchor = block.selectFirst("h2 a[href], a[href]");
        if (anchor == null) {
            return null;
        }
        String title = cleanText(anchor.text());
        String url = extractUrl(anchor, block);
        if (!StringUtils.hasText(title) || !StringUtils.hasText(url)) {
            return null;
        }
        return new SearchResult(title, extractSnippet(block, ".b_caption p, .b_snippet, p"), url);
    }

    private List<SearchResult> parseBaiduResults(Document document, int limit) {
        List<SearchResult> results = new ArrayList<>();
        for (Element block : document.select("article, .result, .c-result, .result-container, .c-container, .c-result-content")) {
            SearchResult result = extractBaiduResult(block);
            if (result == null || containsUrl(results, result.url())) {
                continue;
            }
            results.add(result);
            if (results.size() >= limit) {
                break;
            }
        }
        return results;
    }

    private SearchResult extractBaiduResult(Element block) {
        if (block == null) {
            return null;
        }
        Element titleElement = block.selectFirst("h3, .cosc-title, .c-title, .result-title");
        if (titleElement == null) {
            return null;
        }

        String title = cleanText(titleElement.text());
        String url = extractUrl(
                block.selectFirst("[rl-link-data-url], [data-url], [rl-link-href], a[href]"),
                titleElement,
                block
        );
        if (!StringUtils.hasText(title) || !StringUtils.hasText(url)) {
            return null;
        }

        return new SearchResult(title, extractSnippet(block, ".tts-b-item, .result-desc, .c-line-clamp1, .c-line-clamp2, .c-line-clamp3, .c-color-text, .c-font-normal, .c-gap-top-small, .c-span-last, div[class*=content], div[class*=abstract], span[class*=content]"), url);
    }

    private List<SearchResult> parseGenericResults(Document document, int limit) {
        List<SearchResult> results = new ArrayList<>();
        for (Element block : document.select("article, li.b_algo, .b_algo, .result, .c-result, .result-container, .c-container, .c-result-content")) {
            SearchResult result = extractGenericResult(block);
            if (result == null || containsUrl(results, result.url())) {
                continue;
            }
            results.add(result);
            if (results.size() >= limit) {
                break;
            }
        }
        return results;
    }

    private SearchResult extractGenericResult(Element block) {
        if (block == null) {
            return null;
        }
        Element titleElement = block.selectFirst("h2 a[href], h3 a[href], h2, h3, a[href]");
        if (titleElement == null) {
            return null;
        }

        String title = cleanText(titleElement.text());
        String url = extractUrl(titleElement, block);
        if (!StringUtils.hasText(title) || !StringUtils.hasText(url)) {
            return null;
        }

        return new SearchResult(title, extractSnippet(block, ".b_caption p, .b_snippet, .result-desc, .c-color-text, p, div[class*=content], div[class*=abstract], span[class*=content]"), url);
    }

    private String extractUrl(Element... elements) {
        for (Element element : elements) {
            String url = extractUrl(element);
            if (StringUtils.hasText(url)) {
                return url;
            }
        }
        return "";
    }

    private String extractUrl(Element element) {
        if (element == null) {
            return "";
        }

        String direct = firstNonBlank(
                normalizeUrl(element.attr("abs:href")),
                normalizeUrl(element.attr("href")),
                normalizeUrl(element.attr("rl-link-data-url")),
                normalizeUrl(element.attr("data-url")),
                normalizeUrl(element.attr("rl-link-href")),
                extractUrlFromSerializedData(element.attr("rl-link-data-log")),
                extractUrlFromSerializedData(element.attr("data-log")),
                extractUrlFromSerializedData(element.attr("data-click-info"))
        );
        if (StringUtils.hasText(direct)) {
            return direct;
        }

        Element nested = element.selectFirst("[rl-link-data-url], [data-url], [rl-link-href], a[href]");
        if (nested != null && nested != element) {
            return extractUrl(nested);
        }
        return "";
    }

    private String extractUrlFromSerializedData(String raw) {
        if (!StringUtils.hasText(raw)) {
            return "";
        }
        String decoded = Parser.unescapeEntities(raw, true).replace("\\/", "/").replace("&amp;", "&");
        Matcher matcher = ABSOLUTE_URL_PATTERN.matcher(decoded);
        while (matcher.find()) {
            String url = normalizeUrl(matcher.group());
            if (StringUtils.hasText(url)) {
                return url;
            }
        }
        return "";
    }

    private String extractSnippet(Element container, String selectors) {
        if (container == null) {
            return "";
        }
        Element snippetElement = StringUtils.hasText(selectors) ? container.selectFirst(selectors) : null;
        String snippet = snippetElement != null ? snippetElement.text() : container.text();
        return shorten(cleanText(snippet), 220);
    }

    private String normalizeUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return "";
        }

        String candidate = Parser.unescapeEntities(url.trim(), true).replace("&amp;", "&");
        if (!StringUtils.hasText(candidate)
                || candidate.startsWith("javascript:")
                || candidate.startsWith("#")) {
            return "";
        }
        if (candidate.startsWith("//")) {
            return "https:" + candidate;
        }
        if (candidate.startsWith("/")) {
            return "https://m.baidu.com" + candidate;
        }
        return candidate;
    }

    private boolean containsUrl(List<SearchResult> results, String url) {
        for (SearchResult result : results) {
            if (result.url().equals(url)) {
                return true;
            }
        }
        return false;
    }

    private String formatResults(String query, List<SearchResult> results) {
        StringBuilder sb = new StringBuilder();
        sb.append("以下是关于“").append(query).append("”的联网搜索结果：");
        for (int i = 0; i < results.size(); i++) {
            SearchResult result = results.get(i);
            sb.append("\n").append(i + 1).append(". ").append(shorten(result.title(), 120));
            if (StringUtils.hasText(result.snippet())) {
                sb.append("\n   摘要：").append(result.snippet());
            }
            sb.append("\n   链接：").append(result.url());
        }
        log.info("web_search 执行完成, query={}, resultCount={}", query, results.size());
        return sb.toString();
    }

    private String cleanText(String text) {
        return text == null ? "" : text.replace('\u00A0', ' ').replaceAll("\\s+", " ").trim();
    }

    private String shorten(String text, int maxLength) {
        if (!StringUtils.hasText(text) || text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength - 1) + "…";
    }

    private String firstNonBlank(String... values) {
        for (String value : values) {
            if (StringUtils.hasText(value)) {
                return value;
            }
        }
        return "";
    }

    private record SearchResult(String title, String snippet, String url) {
    }
}
