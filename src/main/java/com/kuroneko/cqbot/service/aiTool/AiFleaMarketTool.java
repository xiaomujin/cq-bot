package com.kuroneko.cqbot.service.aiTool;

import cn.hutool.core.date.LocalDateTimeUtil;
import com.kuroneko.cqbot.utils.HttpUtil;
import com.kuroneko.cqbot.utils.JsonUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;

@Component
@Slf4j
public class AiFleaMarketTool {

    private static final String GRAPHQL_URL = "https://api.tarkov.dev/graphql";

    private static final String QUERY_ITEMS = """
            {
              items(name: "%s", lang: zh) {
                id
                name
                shortName
                basePrice
                updated
                width
                height
                iconLink
                avg24hPrice
                lastLowPrice
                low24hPrice
                sellFor {
                  priceRUB
                  vendor {
                    __typename
                  }
                }
                usedInTasks {
                  id
                  name
                  objectives {
                    type
                    ... on TaskObjectiveItem {
                      count
                      foundInRaid
                      items {
                        id
                        name
                      }
                    }
                  }
                }
                craftsUsing {
                  station {
                    name
                  }
                  level
                  requiredItems {
                    item {
                      id
                      name
                    }
                    count
                  }
                }
              }
            }
            """;

    @Tool(name = "tarkov_flea_market", description = "查询逃离塔科夫(Escape from Tarkov)跳蚤市场物品价格信息。适用于查询物品价格、市场行情、任务需求、藏身处制造配方等。返回物品的24h均价、当前最低价、商人收购价、任务需求和制造配方。")
    public String searchFleaMarket(@ToolParam(description = "物品名称（中文或英文均可），不包含“跳蚤”“查跳蚤”等触发查询的提示词。") String itemName) {
        log.info("tarkov_flea_market 执行, itemName={}", itemName);
        try {
            String query = QUERY_ITEMS.formatted(itemName);
            String response = executeGraphQL(query);
            JsonNode root = JsonUtil.toNode(response);
            JsonNode items = root.get("data").get("items");

            if (items == null || items.isEmpty()) {
                return "未找到物品: " + itemName;
            }

            int limit = Math.min(items.size(), 3);
            StringBuilder result = new StringBuilder();
            for (int i = 0; i < limit; i++) {
                if (i > 0) result.append("\n====================\n");
                result.append(formatItem(items.get(i)));
            }
            if (items.size() > 3) {
                result.append("\n共找到 ").append(items.size()).append(" 个匹配物品，以上为前3个。");
            }
            return result.toString();
        } catch (Exception e) {
            log.error("tarkov_flea_market 查询失败, itemName={}", itemName, e);
            return "查询失败: " + e.getMessage();
        }
    }

    private String formatItem(JsonNode item) {
        StringBuilder sb = new StringBuilder();

        String name = item.get("name").asString();
        String shortName = item.has("shortName") ? item.get("shortName").asString() : "";
        int basePrice = item.has("basePrice") ? item.get("basePrice").asInt(0) : 0;
        int avg24hPrice = item.has("avg24hPrice") ? item.get("avg24hPrice").asInt(0) : 0;
        int lastLowPrice = item.has("lastLowPrice") ? item.get("lastLowPrice").asInt(0) : 0;
        int low24hPrice = item.has("low24hPrice") ? item.get("low24hPrice").asInt(0) : 0;
        int width = item.has("width") ? item.get("width").asInt(1) : 1;
        int height = item.has("height") ? item.get("height").asInt(1) : 1;
        int size = width * height;
        String updated = item.has("updated") ? item.get("updated").asString() : "";

        sb.append("【").append(name).append("】");
        if (!shortName.isEmpty()) {
            sb.append(" (").append(shortName).append(")");
        }
        sb.append("\n");

        // 价格信息
        sb.append("- 基础价格: ").append(basePrice).append(" ₽\n");
        sb.append("- 24h均价: ").append(avg24hPrice != 0 ? avg24hPrice + " ₽" : "跳蚤禁售").append("\n");
        sb.append("- 当前最低价: ").append(lastLowPrice != 0 ? lastLowPrice + " ₽" : "跳蚤禁售").append("\n");
        sb.append("- 24h最低价: ").append(low24hPrice != 0 ? low24hPrice + " ₽" : "跳蚤禁售").append("\n");

        // 单格价值
        if (size > 1 && avg24hPrice != 0) {
            sb.append("- 单格价值: ").append(avg24hPrice / size).append(" ₽\n");
        }

        // 商人最高收购价
        JsonNode sellFor = item.get("sellFor");
        int maxTraderPrice = 0;
        if (sellFor != null) {
            for (JsonNode sFor : sellFor) {
                String typename = sFor.get("vendor").get("__typename").asString();
                int priceRUB = sFor.get("priceRUB").asInt(0);
                if ("TraderOffer".equalsIgnoreCase(typename) && priceRUB > maxTraderPrice) {
                    maxTraderPrice = priceRUB;
                }
            }
        }
        if (maxTraderPrice > 0) {
            sb.append("- 商人最高收购价: ").append(maxTraderPrice).append(" ₽\n");
        }

        // 更新时间
        if (!updated.isEmpty()) {
            try {
                OffsetDateTime offsetDateTime = OffsetDateTime.parse(updated, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                LocalDateTime localDateTime = offsetDateTime.toLocalDateTime();
                sb.append("- 数据更新时间: ").append(LocalDateTimeUtil.formatNormal(localDateTime)).append("\n");
            } catch (Exception ignored) {
            }
        }

        // 任务需求
        JsonNode usedInTasks = item.get("usedInTasks");
        if (usedInTasks != null && !usedInTasks.isEmpty()) {
            sb.append("\n任务需求:\n");
            String itemId = item.get("id").asString();
            for (JsonNode task : usedInTasks) {
                int needNum = 0;
                boolean foundInRaid = false;
                JsonNode objectives = task.get("objectives");
                if (objectives != null) {
                    for (JsonNode obj : objectives) {
                        if ("giveItem".equalsIgnoreCase(obj.get("type").asString())) {
                            JsonNode objItems = obj.get("items");
                            if (objItems != null) {
                                for (JsonNode objItem : objItems) {
                                    if (objItem.get("id").asString().equalsIgnoreCase(itemId)) {
                                        needNum = obj.get("count").asInt(0);
                                        foundInRaid = obj.get("foundInRaid").asBoolean(false);
                                        break;
                                    }
                                }
                            }
                        }
                    }
                }
                sb.append("  * ").append(task.get("name").asString());
                if (needNum > 0) {
                    sb.append(" (需要 ").append(needNum).append(" 个)");
                }
                if (foundInRaid) {
                    sb.append(" [需战局中找到]");
                }
                sb.append("\n");
            }
        }

        // 藏身处制造
        JsonNode craftsUsing = item.get("craftsUsing");
        if (craftsUsing != null && !craftsUsing.isEmpty()) {
            sb.append("\n藏身处制造:\n");
            String itemId = item.get("id").asString();
            for (JsonNode craft : craftsUsing) {
                String stationName = craft.get("station").get("name").asString();
                String level = craft.get("level").asString();
                int count = 0;
                JsonNode requiredItems = craft.get("requiredItems");
                if (requiredItems != null) {
                    for (JsonNode requiredItem : requiredItems) {
                        if (requiredItem.get("item").get("id").asString().equalsIgnoreCase(itemId)) {
                            count = requiredItem.get("count").asInt(0);
                            break;
                        }
                    }
                }
                sb.append("  * ").append(stationName).append(" Lv.").append(level);
                if (count > 0) {
                    sb.append(" (需要 ").append(count).append(" 个)");
                }
                sb.append("\n");
            }
        }

        return sb.toString();
    }

    private String executeGraphQL(String query) {
        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put("query", query);
        HttpClient httpClient = HttpUtil.getHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(GRAPHQL_URL))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtil.toString(queryMap)))
                .build();
        return HttpUtil.request(httpClient, GRAPHQL_URL, httpRequest);
    }
}
