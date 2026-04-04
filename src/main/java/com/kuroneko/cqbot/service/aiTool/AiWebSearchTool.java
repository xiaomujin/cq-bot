package com.kuroneko.cqbot.service.aiTool;

import lombok.extern.slf4j.Slf4j;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
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

@Component
@Slf4j
public class AiWebSearchTool {
    private static final String DEFAULT_ENDPOINT = "https://m.baidu.com/s";
    private static final String DEFAULT_REFERER = "https://m.baidu.com/";
    private static final String DEFAULT_USER_AGENT = "Mozilla/5.0 (iPhone; CPU iPhone OS 18_3 like Mac OS X) AppleWebKit/605.1.15 (KHTML, like Gecko) Version/18.3 Mobile/15E148 Safari/604.1";

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

        try {
            Document document = fetch(buildSearchUrl(endpoint, normalizedQuery));
            List<SearchResult> results = parseResults(document, limit);
            if (results.isEmpty()) {
                return "未找到与“" + normalizedQuery + "”相关的网页结果。";
            }
            return formatResults(normalizedQuery, results);
        } catch (Exception e) {
            log.error("web_search 执行失败, query={}", normalizedQuery, e);
            return "web_search 执行失败：" + e.getMessage();
        }
    }

    private Document fetch(String url) throws Exception {
        return Jsoup.connect(url)
                .userAgent(DEFAULT_USER_AGENT)
                .referrer(DEFAULT_REFERER)
                .timeout(timeoutMillis)
                .followRedirects(true)
                .get();
    }

    private String buildSearchUrl(String baseUrl, String query) {
        String separator = baseUrl.contains("?") ? "&" : "?";
        return baseUrl + separator + "word=" + URLEncoder.encode(query, StandardCharsets.UTF_8) + "&ie=utf-8";
    }

    private List<SearchResult> parseResults(Document document, int limit) {
        List<SearchResult> results = new ArrayList<>();
        for (Element block : document.select("article, .result, .c-result, .result-container, .c-container, .c-result-content")) {
            SearchResult result = extractResult(block);
            if (result == null || containsUrl(results, result.url())) {
                continue;
            }
            results.add(result);
            if (results.size() >= limit) {
                break;
            }
        }

        if (!results.isEmpty()) {
            return results;
        }

        for (Element link : document.select("h3 a[href], a[href] h3, a.c-blocka[href], a[href][data-click]")) {
            Element anchor = link.tagName().equals("a") ? link : link.parent();
            if (anchor == null) {
                continue;
            }
            String title = cleanText(link.text());
            String url = normalizeUrl(firstNonBlank(anchor.attr("abs:href"), anchor.attr("href")));
            if (!StringUtils.hasText(title) || !StringUtils.hasText(url) || containsUrl(results, url)) {
                continue;
            }
            String snippet = extractSnippet(anchor.closest("article, div, section, body"));
            results.add(new SearchResult(title, snippet, url));
            if (results.size() >= limit) {
                break;
            }
        }
        return results;
    }

    private SearchResult extractResult(Element block) {
        if (block == null) {
            return null;
        }
        Element titleElement = block.selectFirst("h3 a[href], a[href] h3, a.c-blocka[href], a[href][data-click], a[href]");
        if (titleElement == null) {
            return null;
        }

        Element anchor = titleElement.tagName().equals("a") ? titleElement : titleElement.parent();
        if (anchor == null) {
            return null;
        }

        String title = cleanText(titleElement.text());
        String url = normalizeUrl(firstNonBlank(anchor.attr("abs:href"), anchor.attr("href")));
        if (!StringUtils.hasText(title) || !StringUtils.hasText(url)) {
            return null;
        }

        return new SearchResult(title, extractSnippet(block), url);
    }

    private String extractSnippet(Element container) {
        if (container == null) {
            return "";
        }
        Element snippetElement = container.selectFirst(".c-line-clamp1, .c-line-clamp2, .c-line-clamp3, .c-color-text, .c-font-normal, .result-desc, .c-gap-top-small, .c-span-last, div[class*=content], div[class*=abstract], span[class*=content]");
        String snippet = snippetElement != null ? snippetElement.text() : container.text();
        return shorten(cleanText(snippet), 220);
    }

    private String normalizeUrl(String url) {
        if (!StringUtils.hasText(url)) {
            return "";
        }

        String candidate = url.trim();
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
        log.info("web_search 执行完成, query={}, results={}", query, sb);
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

    private String firstNonBlank(String first, String second) {
        return StringUtils.hasText(first) ? first : second;
    }

    private record SearchResult(String title, String snippet, String url) {
    }
}
