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
    private static final String BING_ENDPOINT = "https://cn.bing.com/search";
    private static final String BING_REFERER = "https://cn.bing.com/";
    private static final String DESKTOP_USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/123.0.0.0 Safari/537.36";
    private static final Pattern ABSOLUTE_URL_PATTERN = Pattern.compile("https?://[^\\s\"'<>]+", Pattern.CASE_INSENSITIVE);

    private final int defaultMaxResults;
    private final int timeoutMillis;

    public AiWebSearchTool(@Value("${bot.ai.web-search.max-results:5}") int defaultMaxResults,
                           @Value("${bot.ai.web-search.timeout-millis:10000}") int timeoutMillis) {
        this.defaultMaxResults = Math.max(1, Math.min(defaultMaxResults, 10));
        this.timeoutMillis = Math.max(1000, timeoutMillis);
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
            Document document = fetch(buildSearchUrl(normalizedQuery));
            List<SearchResult> results = parseResults(document, limit);
            if (!results.isEmpty()) {
                return formatResults(normalizedQuery, results);
            }
            log.warn("web_search 未解析到结果, query={}", normalizedQuery);
            return "未找到与“" + normalizedQuery + "”相关的网页结果。";
        } catch (Exception e) {
            log.warn("web_search 搜索源执行失败, query={}", normalizedQuery, e);
            return "web_search 执行失败，请稍后再试";
        }
    }

    private Document fetch(String url) throws Exception {
        return Jsoup.connect(url)
                .userAgent(DESKTOP_USER_AGENT)
                .referrer(BING_REFERER)
                .header("Accept-Language", "zh-CN,zh;q=0.9,en;q=0.8")
                .timeout(timeoutMillis)
                .followRedirects(true)
                .get();
    }

    private String buildSearchUrl(String query) {
        return BING_ENDPOINT + "?q=" + URLEncoder.encode(query, StandardCharsets.UTF_8);
    }

    private List<SearchResult> parseResults(Document document, int limit) {
        List<SearchResult> results = parseBingResults(document, limit);
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
