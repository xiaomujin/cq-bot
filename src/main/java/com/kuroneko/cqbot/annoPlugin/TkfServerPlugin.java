package com.kuroneko.cqbot.annoPlugin;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.kuroneko.cqbot.config.ProjectConfig;
import com.kuroneko.cqbot.entity.TkfTask;
import com.kuroneko.cqbot.entity.TkfTaskTarget;
import com.kuroneko.cqbot.enums.Regex;
import com.kuroneko.cqbot.exception.ExceptionHandler;
import com.kuroneko.cqbot.lagrange.markdown.Keyboard;
import com.kuroneko.cqbot.service.HelpService;
import com.kuroneko.cqbot.service.TarKovMarketService;
import com.kuroneko.cqbot.service.TkfTaskService;
import com.kuroneko.cqbot.service.TkfTaskTargetService;
import com.kuroneko.cqbot.utils.BotUtil;
import com.kuroneko.cqbot.utils.CacheUtil;
import com.kuroneko.cqbot.utils.HttpUtil;
import com.kuroneko.cqbot.vo.TarKovMarketVo;
import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

@Shiro
@Slf4j
@Component
@RequiredArgsConstructor
public class TkfServerPlugin {
    private final TarKovMarketService tarKovMarketService;
    private final TkfTaskService tkfTaskService;
    private final TkfTaskTargetService tkfTaskTargetService;
    private final HelpService helpService;

    @AnyMessageHandler()
    @MessageHandlerFilter(cmd = Regex.TKF_SERVER_INFO)
    public void tkfServerInfo(Bot bot, AnyMessageEvent event, Matcher matcher) {
        ExceptionHandler.with(bot, event, () -> CacheUtil.getOrPut(Regex.TKF_SERVER_INFO, 20, TimeUnit.MINUTES, tarKovMarketService::getTkfServerStatusMsg));
    }

    @AnyMessageHandler()
    @MessageHandlerFilter(cmd = Regex.TKF_MARKET_SEARCH)
    public void tkfMarketSearch(Bot bot, AnyMessageEvent event, Matcher matcher) {
        ExceptionHandler.with(bot, event, () -> {
            String text = matcher.group("text").trim();
            if (text.isEmpty()) {
                return helpService.tkfMarketSearchHelp();
            }
            return CacheUtil.getOrPut(Regex.TKF_MARKET_SEARCH + text, 5, TimeUnit.MINUTES, () -> {
                Collection<TarKovMarketVo> search = tarKovMarketService.search(text);
                MsgUtils msg = MsgUtils.builder();
                if (search.isEmpty()) {
                    return msg.text("没有找到 ").text(text).build();
                }
                search.forEach(it -> {
                    if (StrUtil.isNotBlank(it.getWikiIcon())) {
                        msg.img(it.getWikiIcon());
                    } else if (StrUtil.isNotBlank(it.getWikiImg())) {
                        msg.img(it.getWikiImg());
                    }
                    msg.text("\n名称：").text(STR."\{it.getCnName()}\n");
                    msg.text("24h:").text(STR."\{it.getChange24()}%").text(" 7d:").text(STR."\{it.getChange7d()}%\n");
                    msg.text("基础价格：").text(STR."\{it.getBasePrice()}₽\n");
                    msg.text(it.getTraderName()).text("：").text(STR."\{it.getTraderPrice()}\{it.getTraderPriceCur()}\n");
                    if (it.isCanSellOnFlea()) {
                        msg.text("跳蚤日价：").text(STR."\{it.getAvgDayPrice()}₽\n");
                        msg.text("跳蚤周价：").text(STR."\{it.getAvgWeekPrice()}₽\n");
                        msg.text("单格：").text(STR."\{it.getAvgDayPrice() / it.getSize()}₽");
                    } else {
                        msg.text("单格：").text(STR."\{it.getTraderPrice() / it.getSize()}\{it.getTraderPriceCur()}\n");
                        msg.text("跳蚤禁售!");
                    }
                });
                return msg.build();
            });
        });
    }

    @AnyMessageHandler()
    @MessageHandlerFilter(cmd = Regex.UPDATE_TKF_TASK)
    public void updateTkfTask(Bot bot, AnyMessageEvent event, Matcher matcher) {
        ExceptionHandler.with(bot, event, () -> {
            if (!ProjectConfig.adminList.contains(event.getSender().getUserId())) {
                return "";
            }
            log.info("开始更新任务数据");
            bot.sendMsg(event, "开始更新任务数据", false);
            String request = getQueryRes(QUERY_TASKS);
            JSONArray jsonArray = JSON.parseObject(request).getJSONObject("data").getJSONArray("tasks");
            if (jsonArray.isEmpty()) {
                return "更新失败";
            }
            log.info("数据请求成功：size:{}", jsonArray.size());
            ArrayList<TkfTask> tkfTasks = new ArrayList<>();
            ArrayList<TkfTaskTarget> taskTargets = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                Long id = i + 1L;
                JSONObject jsonObject = jsonArray.getJSONObject(i);
                String name = jsonObject.getString("name");
                TkfTask tkfTask = TkfTask.builder()
                        .id(id)
                        .name(name)
                        .sName(name.replaceAll("[\\s-]", ""))
                        .traderName(jsonObject.getJSONObject("trader").getString("name"))
                        .traderImg(jsonObject.getJSONObject("trader").getString("imageLink"))
                        .taskImg(jsonObject.getString("taskImageLink"))
                        .minLevel(jsonObject.getInteger("minPlayerLevel"))
                        .isKappa(jsonObject.getBoolean("kappaRequired"))
                        .isLightkeeper(jsonObject.getBoolean("lightkeeperRequired"))
                        .build();
                tkfTasks.add(tkfTask);
                if (!jsonObject.containsKey("objectives")) {
                    continue;
                }
                JSONArray objectives = jsonObject.getJSONArray("objectives");
                for (int j = 0; j < objectives.size(); j++) {
                    JSONObject target = objectives.getJSONObject(j);
                    Integer count = target.getInteger("count");
                    String typename = target.getString("__typename");
                    if ("TaskObjectiveSkill".equalsIgnoreCase(typename)) {
                        count = target.getJSONObject("skillLevel").getInteger("level");
                    }
                    TkfTaskTarget tkfTaskTarget = TkfTaskTarget.builder()
                            .parentId(id)
                            .type(target.getString("type"))
                            .description(target.getString("description"))
                            .isOptional(target.getBoolean("optional"))
                            .count(count)
                            .isRaid(target.getBoolean("foundInRaid"))
                            .build();
                    taskTargets.add(tkfTaskTarget);
                }
            }
            log.info("数据组装完成：tkfTasks.size:{} taskTargets.size:{}", tkfTasks.size(), taskTargets.size());
            tkfTaskService.remove(null);
            tkfTaskTargetService.remove(null);
            tkfTaskService.saveBatch(tkfTasks, 600);
            tkfTaskTargetService.saveBatch(taskTargets, 600);
            log.info("数据入库完成");
            return "更新成功";
        });

    }

    private static final String QUERY_TASKS = """
            {
              tasks(lang: zh) {
                id
                name
                trader {
                  name
                  imageLink
                }
                taskImageLink
                minPlayerLevel
                kappaRequired
                lightkeeperRequired
                objectives {
                  id
                  type
                  description
                  optional
                  __typename
                  ... on TaskObjectiveItem {
                    count
                    foundInRaid
                  }
                  ... on TaskObjectiveShoot {
                    count
                  }
                  ... on TaskObjectiveQuestItem {
                    count
                  }
                  ... on TaskObjectiveExtract {
                    count
                  }
                  ... on TaskObjectivePlayerLevel {
                    count: playerLevel
                  }
                  ... on TaskObjectiveSkill {
                    skillLevel {
                      level
                    }
                  }
                  ... on TaskObjectiveTraderLevel {
                    count: level
                  }
                  ... on TaskObjectiveTraderStanding {
                    count: value
                  }
                  ... on TaskObjectiveExperience {
                    count
                  }
                  ... on TaskObjectiveUseItem {
                    count
                  }
                }
              }
            }
            """;

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
                gridImageLink
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
                      item {
                        id
                        name
                      }
                    }
                  }
                }
                craftsUsing {
                  station{
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
    private static final String QUERY_ITEM_ID = """
            {
              item(id: "%s", lang: zh) {
                id
                name
                shortName
                basePrice
                updated
                width
                height
                iconLink
                gridImageLink
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
                      item {
                        id
                        name
                      }
                    }
                  }
                }
                craftsUsing {
                  station{
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

    private static String getQueryRes(String query) {
        HashMap<String, String> queryMap = new HashMap<>();
        queryMap.put("query", query);
        String url = "https://api.tarkov.dev/graphql";
        HttpClient httpClient = HttpUtil.getHttpClient();
        HttpRequest httpRequest = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Content-Type", "application/json")
                .header("Accept", "application/json")
                .POST(HttpRequest.BodyPublishers.ofString(JSON.toJSONString(queryMap)))
                .build();
        return HttpUtil.request(httpClient, url, httpRequest);
    }

    @AnyMessageHandler()
    @MessageHandlerFilter(cmd = Regex.SEARCH_TKF_TASK, at = AtEnum.NEED)
    public void searchTkfTaskAt(Bot bot, AnyMessageEvent event, Matcher matcher) {
        searchTkfTask(bot, event, matcher);
    }

    @AnyMessageHandler()
    @MessageHandlerFilter(cmd = Regex.SEARCH_TKF_TASK, at = AtEnum.NOT_NEED)
    public void searchTkfTaskNoAt(Bot bot, AnyMessageEvent event, Matcher matcher) {
        searchTkfTask(bot, event, matcher);
    }

    private void searchTkfTask(Bot bot, AnyMessageEvent event, Matcher matcher) {
        log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), Regex.SEARCH_TKF_TASK);
        String name = matcher.group("name").trim();
        String sName = name.replaceAll("[\\s-]", "");
        ExceptionHandler.with(bot, event, () -> CacheUtil.getOrPut(sName, 3, TimeUnit.MINUTES, () -> {
            List<TkfTask> tkfTasks = tkfTaskService.lambdaQuery().like(TkfTask::getSName, sName).list();
            if (tkfTasks.isEmpty()) {
                return STR."未找到任务: \{sName}";
            }
            tkfTasks.sort(Comparator.comparing(
                    tkfTask -> tkfTask.getSName().length()
            ));
            TkfTask first = tkfTasks.getFirst();
            List<TkfTaskTarget> tkfTaskTargets = tkfTaskTargetService.lambdaQuery().eq(TkfTaskTarget::getParentId, first.getId()).list();
            String mdText = getMdText(tkfTaskTargets, first);

            Keyboard keyboard;
            if (tkfTasks.size() > 1) {
                keyboard = Keyboard.Builder();
                for (int i = 1; i < tkfTasks.size(); i++) {
                    TkfTask tkfTask = tkfTasks.get(i);
                    keyboard.addRow().addButton(tkfTask.getName(), STR."查任务 \{tkfTask.getSName()}", true, List.of(event.getUserId()));
                    if (i >= 5) {
                        break;
                    }
                }
            } else {
                keyboard = Keyboard.Builder().addRow().addButton("我也查查", STR."查任务 ", false);
            }

            return BotUtil.getMarkdownMsg(bot, event, mdText, keyboard);
        }));
    }

    private static String getMdText(List<TkfTaskTarget> tkfTaskTargets, TkfTask first) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean tip = false;
        for (int i = 0; i < tkfTaskTargets.size(); i++) {
            TkfTaskTarget tkfTaskTarget = tkfTaskTargets.get(i);
            if (tkfTaskTarget.getIsOptional()) {
                tkfTaskTarget.setDescription(STR."( 可选 ) \{tkfTaskTarget.getDescription()}");
            }
            String num = tkfTaskTarget.getCount().toString();
            if (tkfTaskTarget.getIsRaid()) {
                tip = true;
                num = STR."( \{num}√ )";
            } else {
                num = STR."( \{num} )";
            }
            stringBuilder.append(STR."> \{i + 1}. \{tkfTaskTarget.getDescription()}\{tkfTaskTarget.getCount() > 0 ? num : ""}\n");
        }
        if (tip) {
            stringBuilder.append("> Tip: √ 表示需要在战局中找到。");
        }
        String mdText = STR."""
                ![\{first.getTraderName()} #30px #30px](\{first.getTraderImg()}): \{first.getName()}
                ***
                \{first.getIsKappa() ? "3x4任务: ( √ )" : "~~3x4任务~~: ( × )"}
                \{first.getIsLightkeeper() ? "灯塔商人: ( √ )" : "~~灯塔商人~~: ( × )"}
                开启等级: \{first.getMinLevel()}
                任务目标:
                ***
                \{stringBuilder.toString()}""";
        return mdText;
    }


    @AnyMessageHandler()
    @MessageHandlerFilter(cmd = Regex.TKF_MARKET_SEARCH, at = AtEnum.NEED)
    public void tkfMarketSearchAt(Bot bot, AnyMessageEvent event, Matcher matcher) {
        log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), Regex.TKF_MARKET_SEARCH);
        ExceptionHandler.with(bot, event, () -> {
            String text = matcher.group("text").trim();
            if (text.isEmpty()) {
                return "";
            }
            return CacheUtil.getOrPut(text, 3, TimeUnit.MINUTES, () -> {
                JSONArray jsonArray;
                if (text.length() == 24) {
                    String formatted = QUERY_ITEM_ID.formatted(text);
                    String request = getQueryRes(formatted);
                    JSONObject jsonObject = JSON.parseObject(request).getJSONObject("data").getJSONObject("item");
                    if (jsonObject == null) {
                        return STR."查询失败或物品不存在：\{text}";
                    }
                    jsonArray = JSONArray.of(jsonObject);
                } else {
                    String formatted = QUERY_ITEMS.formatted(text);
                    String request = getQueryRes(formatted);
                    jsonArray = JSON.parseObject(request).getJSONObject("data").getJSONArray("items");
                    if (jsonArray.isEmpty()) {
                        return STR."查询失败或物品不存在：\{text}";
                    }
                }

                JSONObject item = jsonArray.getJSONObject(0);

                Integer avg24hPrice = item.getInteger("avg24hPrice");
                Integer lastLowPrice = item.getInteger("lastLowPrice");
                String updated = item.getString("updated");
                String id = item.getString("id");
                OffsetDateTime offsetDateTime = OffsetDateTime.parse(updated, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
                LocalDateTime localDateTime = offsetDateTime.toLocalDateTime();
                String formatTime = LocalDateTimeUtil.formatNormal(localDateTime);
                JSONArray sellFor = item.getJSONArray("sellFor");
                Integer maxPrice = 0;
                for (Object o : sellFor) {
                    JSONObject sFor = (JSONObject) o;
                    String typename = sFor.getJSONObject("vendor").getString("__typename");
                    Integer priceRUB = sFor.getInteger("priceRUB");
                    if ("TraderOffer".equalsIgnoreCase(typename) && priceRUB > maxPrice) {
                        maxPrice = priceRUB;
                    }
                }
                JSONArray usedInTasks = item.getJSONArray("usedInTasks");
                StringBuilder taskSB = new StringBuilder();
                for (Object usedInTask : usedInTasks) {
                    JSONObject task = (JSONObject) usedInTask;
                    Integer needNum = 0;
                    Boolean foundInRaid = false;
                    task.getString("name");
                    JSONArray objectives = task.getJSONArray("objectives");
                    for (Object objective : objectives) {
                        JSONObject obj = (JSONObject) objective;
                        if (obj.getString("type").equalsIgnoreCase("giveItem")) {
                            JSONArray objItems = obj.getJSONArray("item");
                            for (Object objItem : objItems) {
                                JSONObject objItemJ = (JSONObject) objItem;
                                if (objItemJ.getString("id").equalsIgnoreCase(id)) {
                                    needNum = obj.getInteger("count");
                                    foundInRaid = obj.getBoolean("foundInRaid");
                                    break;
                                }
                            }
                        }
                    }

                    taskSB.append(STR."> \{task.getString("name")}( \{needNum}\{foundInRaid ? "√" : ""} )\n");
                }
                if (taskSB.isEmpty()) {
                    taskSB.append("> 无");
                }

                StringBuilder craftSB = new StringBuilder();
                JSONArray craftsUses = item.getJSONArray("craftsUsing");
                for (Object craftsUs : craftsUses) {
                    JSONObject craft = (JSONObject) craftsUs;
                    String name = craft.getJSONObject("station").getString("name");
                    String level = craft.getString("level");
                    Integer count = 0;
                    JSONArray requiredItems = craft.getJSONArray("requiredItems");
                    for (Object requiredItem : requiredItems) {
                        JSONObject requiredItemJ = (JSONObject) requiredItem;
                        if (requiredItemJ.getJSONObject("item").getString("id").equalsIgnoreCase(id)) {
                            count = requiredItemJ.getInteger("count");
                            break;
                        }
                    }
                    craftSB.append(STR."> \{name} lv.\{level} ( \{count} )\n");
                }
                if (craftSB.isEmpty()) {
                    craftSB.append("> 无");
                }

                String mdText = STR."""
                ![icon #35px #35px](\{item.getString("iconLink")}): \{item.getString("name")}
                ***
                24h均价: \{avg24hPrice != 0 ? STR."\{avg24hPrice} ₽" : "跳蚤禁售"}
                跳蚤现价: \{avg24hPrice != 0 ? STR."\{lastLowPrice} ₽" : "跳蚤禁售"}
                商人价格: \{maxPrice} ₽
                任务需求:

                \{taskSB.toString()}

                藏身处需求:

                \{craftSB.toString()}
                ***
                \{formatTime}
                """;

                Keyboard keyboard = Keyboard.Builder();
                if (jsonArray.size() > 1) {
                    for (int i = 1; i < jsonArray.size(); i++) {
                        String idOther = jsonArray.getJSONObject(i).getString("id");
                        String nameOther = jsonArray.getJSONObject(i).getString("name");
                        keyboard.addRow().addButton(nameOther, STR."跳蚤 \{idOther}", true, List.of(event.getUserId()));
                        if (i >= 5) {
                            break;
                        }
                    }
                } else {
                    keyboard.addRow().addButton("我也要查", "跳蚤 ");
                }
                return BotUtil.getMarkdownMsg(bot, event, mdText, keyboard);
            });
        });
    }

}

