package com.kuroneko.cqbot.annoPlugin;

import cn.hutool.core.date.LocalDateTimeUtil;
import cn.hutool.core.util.StrUtil;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.core.cfg.ConfigManager;
import com.kuroneko.cqbot.entity.TkfTask;
import com.kuroneko.cqbot.entity.TkfTaskTarget;
import com.kuroneko.cqbot.enums.Regex;
import com.kuroneko.cqbot.exception.ExceptionHandler;
import com.kuroneko.cqbot.service.HelpService;
import com.kuroneko.cqbot.service.TarKovMarketService;
import com.kuroneko.cqbot.service.TkfTaskService;
import com.kuroneko.cqbot.service.TkfTaskTargetService;
import com.kuroneko.cqbot.utils.BotUtil;
import com.kuroneko.cqbot.utils.CacheUtil;
import com.kuroneko.cqbot.utils.HttpUtil;
import com.kuroneko.cqbot.utils.JsonUtil;
import com.kuroneko.cqbot.utils.PuppeteerUtil;
import com.kuroneko.cqbot.vo.TarKovMarketVo;
import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.Keyboard;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.AtEnum;
import com.ruiyun.jvppeteer.api.core.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import tools.jackson.databind.JsonNode;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.stream.Collectors;

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
                    msg.text("\n名称：").text(it.getChName() + "\n");
                    msg.text("24h:").text(it.getChange24() + "%").text(" 7d:").text(it.getChange7d() + "%\n");
                    msg.text("基础价格：").text(it.getBasePrice() + "₽\n");
                    msg.text(it.getTraderName()).text("：").text(it.getTraderPrice() + it.getTraderPriceCur() + "\n");
                    if (it.isCanSellOnFlea()) {
                        msg.text("跳蚤日价：").text(it.getAvgDayPrice() + "₽\n");
                        msg.text("跳蚤周价：").text(it.getAvgWeekPrice() + "₽\n");
                        msg.text("单格：").text(it.getAvgDayPrice() / it.getSize() + "₽");
                    } else {
                        msg.text("单格：").text(it.getTraderPrice() / it.getSize() + it.getTraderPriceCur() + "\n");
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
            if (!ConfigManager.ins.getAdminCfg().getAdminList().contains(event.getSender().getUserId())) {
                return "";
            }
            log.info("开始更新任务数据");
            bot.sendMsg(event, "开始更新任务数据", false);
            String request = getQueryRes(QUERY_TASKS);
            JsonNode jsonArray = JsonUtil.toNode(request).get("data").get("tasks");
            if (jsonArray.isEmpty()) {
                return "更新失败";
            }
            log.info("数据请求成功：size:{}", jsonArray.size());
            ArrayList<TkfTask> tkfTasks = new ArrayList<>();
            ArrayList<TkfTaskTarget> taskTargets = new ArrayList<>();
            for (int i = 0; i < jsonArray.size(); i++) {
                Long id = i + 1L;
                JsonNode jsonObject = jsonArray.get(i);
                String name = jsonObject.get("name").asString();
                TkfTask tkfTask = TkfTask.builder()
                        .id(id)
                        .name(name)
                        .sName(name.replaceAll("[\\s-]", ""))
                        .traderName(jsonObject.get("trader").get("name").asString())
                        .traderImg(jsonObject.get("trader").get("imageLink").asString())
                        .taskImg(jsonObject.get("taskImageLink").asString())
                        .minLevel(jsonObject.get("minPlayerLevel").asInt())
                        .isKappa(jsonObject.get("kappaRequired").asBoolean())
                        .isLightkeeper(jsonObject.get("lightkeeperRequired").asBoolean())
                        .idStr(jsonObject.get("id").asString())
                        .build();
                JsonNode taskRequirements = jsonObject.get("taskRequirements");
                StringBuilder taskSb = new StringBuilder();
                if (taskRequirements != null && !taskRequirements.isEmpty()) {
                    ArrayList<String> perTaskId = new ArrayList<>();
                    for (int j = 0; j < taskRequirements.size(); j++) {
                        JsonNode taskRequirement = taskRequirements.get(j);
                        JsonNode task = taskRequirement.get("task");
                        ArrayList<String> status = new ArrayList<>();
                        if (taskRequirement.get("status").isArray()) {
                            for (JsonNode node : taskRequirement.get("status")) {
                                status.add(node.asString());
                            }
                        }
                        String collect = status.stream().map(
                                        s -> s.replace("active", "进行中")
                                                .replace("complete", "完成")
                                                .replace("failed", "失败"))
                                .collect(Collectors.joining(",", "(", ")"));
                        perTaskId.add(task.get("id").asString());
                        taskSb.append("> ").append(task.get("name").asString().trim()).append(" ").append(collect).append("  \n");
                    }
                    tkfTask.setPreTaskId(String.join("|", perTaskId));
                }
                if (taskSb.isEmpty()) {
                    taskSb.append("> 无\n");
                }
                tkfTask.setPreTask(taskSb.toString());

                StringBuilder sb = new StringBuilder();
                sb.append("> EXP ( ").append(jsonObject.get("experience").asString()).append(" )  \n");
                JsonNode finishRewards = jsonObject.get("finishRewards");
                if (finishRewards != null) {
                    JsonNode traderStanding = finishRewards.get("traderStanding");
                    if (traderStanding != null && !traderStanding.isEmpty()) {
                        for (int j = 0; j < traderStanding.size(); j++) {
                            JsonNode traderStand = traderStanding.get(j);
                            String tName = traderStand.get("trader").get("name").asString();
                            String count = traderStand.get("standing").asString();
                            sb.append("> ").append(tName).append(" ( ").append(count).append(" )  \n");
                        }
                    }

                    JsonNode items = finishRewards.get("items");
                    if (items != null && !items.isEmpty()) {
                        for (int j = 0; j < items.size(); j++) {
                            JsonNode itemsJSONObject = items.get(j);
                            String tName = itemsJSONObject.get("item").get("name").asString();
                            String count = itemsJSONObject.get("count").asString();
                            sb.append("> ").append(tName).append(" ( ").append(count).append(" )  \n");
                        }
                    }

                    JsonNode skillLevelReward = finishRewards.get("skillLevelReward");
                    if (skillLevelReward != null && !skillLevelReward.isEmpty()) {
                        for (int j = 0; j < skillLevelReward.size(); j++) {
                            JsonNode skillLevel = skillLevelReward.get(j);
                            String tName = skillLevel.get("name").asString();
                            String count = skillLevel.get("level").asString();
                            sb.append("> ").append(tName).append(" ( ").append(count).append(" )  \n");
                        }
                    }
                }
                if (sb.isEmpty()) {
                    sb.append("> 无  \n");
                }
                tkfTask.setFinishReward(sb.toString());

                tkfTasks.add(tkfTask);
                if (!jsonObject.has("objectives")) {
                    continue;
                }
                JsonNode objectives = jsonObject.get("objectives");
                for (int j = 0; j < objectives.size(); j++) {
                    JsonNode target = objectives.get(j);
                    Integer count = target.has("count") ? target.get("count").asInt(0) : 0;
                    String typename = target.get("__typename").asString();
                    if ("TaskObjectiveSkill".equalsIgnoreCase(typename)) {
                        count = target.get("skillLevel").get("level").asInt();
                    }
                    TkfTaskTarget tkfTaskTarget = TkfTaskTarget.builder()
                            .parentId(id)
                            .type(target.get("type").asString())
                            .description(target.get("description").asString())
                            .isOptional(target.get("optional").asBoolean(false))
                            .count(count)
                            .isRaid(target.get("foundInRaid").asBoolean(false))
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
                 experience
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
                 finishRewards {
                   traderStanding {
                     trader {
                       name
                     }
                     standing
                   }
                   items {
                     item {
                       name
                     }
                     count
                   }
                   skillLevelReward {
                     name
                     level
                   }
                 }
                 taskRequirements {
                   task {
                     id
                     name
                   }
                   status
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
                      items {
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
                      items {
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
                .POST(HttpRequest.BodyPublishers.ofString(JsonUtil.toString(queryMap)))
                .build();
        return HttpUtil.request(httpClient, url, httpRequest);
    }

    @AnyMessageHandler()
    @MessageHandlerFilter(cmd = Regex.SEARCH_TKF_TASK, at = AtEnum.BOTH)
    public void searchTkfTaskAt(Bot bot, AnyMessageEvent event, Matcher matcher) {
        searchTkfTask(bot, event, matcher);
    }

    private void searchTkfTask(Bot bot, AnyMessageEvent event, Matcher matcher) {
        log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), Regex.SEARCH_TKF_TASK);
        String name = matcher.group("name").trim();
        String sName = name.replaceAll("[\\s-]", "");
        ExceptionHandler.with(bot, event, () -> {
            List<TkfTask> tkfTasks = tkfTaskService.lambdaQuery().like(TkfTask::getSName, sName).list();
            if (tkfTasks.isEmpty()) {
                return "未找到任务: " + sName;
            }
            tkfTasks.sort(Comparator.comparing(
                    tkfTask -> tkfTask.getSName().length()
            ));
            TkfTask first = tkfTasks.getFirst();
            List<TkfTaskTarget> tkfTaskTargets = tkfTaskTargetService.lambdaQuery().eq(TkfTaskTarget::getParentId, first.getId()).list();
            String mdText = getMdText(tkfTaskTargets, first);

            Keyboard keyboard;
            if (tkfTasks.size() > 1) {
                keyboard = Keyboard.builder();
                for (int i = 1; i < tkfTasks.size(); i++) {
                    TkfTask tkfTask = tkfTasks.get(i);
                    Keyboard.Button button = Keyboard.textButtonBuilder()
                            .label(tkfTask.getName())
                            .data("查任务 " + tkfTask.getSName())
                            .enter(true)
                            .build();
                    keyboard.addRow().addButton(button);
                    if (i >= 5) {
                        break;
                    }
                }
            } else {
                Keyboard.Button button = Keyboard.textButtonBuilder().label("我也要查").data("查任务 ").build();
                keyboard = Keyboard.builder().addRow().addButton(button);
            }
            String id = first.getIdStr();
            CacheUtil.put(id, mdText, 5L, TimeUnit.SECONDS);
            Page page = PuppeteerUtil.getNewPage(BotUtil.getLocalHost() + "Markdown/" + id, 600, 200);
            String imgPath = Constant.BASE_IMG_PATH + "md/" + id + ".png";
            PuppeteerUtil.screenshot(page, imgPath);
            //            List<ArrayMsg> arrayMsgs = ArrayMsgUtils.builder().markdown(mdText).keyboard(keyboard).buildList();
            return MsgUtils.builder().img(BotUtil.getLocalMedia(imgPath)).build();
        });
    }

    private static String getMdText(List<TkfTaskTarget> tkfTaskTargets, TkfTask first) {
        StringBuilder stringBuilder = new StringBuilder();
        boolean tip = false;
        for (int i = 0; i < tkfTaskTargets.size(); i++) {
            TkfTaskTarget tkfTaskTarget = tkfTaskTargets.get(i);
            if (tkfTaskTarget.getIsOptional()) {
                tkfTaskTarget.setDescription("( 可选 ) " + tkfTaskTarget.getDescription());
            }
            String num = String.valueOf(tkfTaskTarget.getCount());
            if (tkfTaskTarget.getIsRaid()) {
                tip = true;
                num = "( " + num + "√ )";
            } else {
                num = "( " + num + " )";
            }
            stringBuilder.append("> ").append(i + 1).append(". ").append(tkfTaskTarget.getDescription()).append(tkfTaskTarget.getCount() != null && tkfTaskTarget.getCount() > 0 ? num : "").append("  \n");
        }
        if (tip) {
            stringBuilder.append("> Tip: √ 表示需要在战局中找到。\n");
        }
        return "![" + first.getTraderName() + " #30px #30px](" + first.getTraderImg() + "): " + first.getName() + "\n" +
                "***\n" +
                (first.getIsKappa() ? "3x4任务: ( √ )" : "~~3x4任务~~: ( × )") + "   \n" +
                (first.getIsLightkeeper() ? "灯塔商人: ( √ )" : "~~灯塔商人~~: ( × )") + "   \n" +
                "开启等级: " + first.getMinLevel() + "   \n" +
                "任务目标:\n" +
                "***\n" +
                stringBuilder + "\n" +
                "任务奖励:\n" +
                "***\n" +
                first.getFinishReward() + "\n" +
                "前置任务:\n" +
                "***\n" +
                first.getPreTask() + "\n";
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
            JsonNode jsonArray;
            if (text.length() == 24) {
                String formatted = QUERY_ITEM_ID.formatted(text);
                String request = getQueryRes(formatted);
                JsonNode jsonObject = JsonUtil.toNode(request).get("data").get("item");
                if (jsonObject == null || jsonObject.isNull()) {
                    return "查询失败或物品不存在：" + text;
                }
                jsonArray = JsonUtil.toNode("[" + JsonUtil.toString(jsonObject) + "]");
            } else {
                String formatted = QUERY_ITEMS.formatted(text);
                String request = getQueryRes(formatted);
                jsonArray = JsonUtil.toNode(request).get("data").get("items");
                if (jsonArray.isEmpty()) {
                    return "查询失败或物品不存在：" + text;
                }
            }

            JsonNode item = jsonArray.get(0);

            int avg24hPrice = item.has("avg24hPrice") ? item.get("avg24hPrice").asInt(0) : 0;
            int lastLowPrice = item.has("lastLowPrice") ? item.get("lastLowPrice").asInt(0) : 0;
            String updated = item.get("updated").asString();
            String id = item.get("id").asString();
            OffsetDateTime offsetDateTime = OffsetDateTime.parse(updated, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
            LocalDateTime localDateTime = offsetDateTime.toLocalDateTime();
            String formatTime = LocalDateTimeUtil.formatNormal(localDateTime);
            JsonNode sellFor = item.get("sellFor");
            Integer maxPrice = 0;
            for (JsonNode sFor : sellFor) {
                String typename = sFor.get("vendor").get("__typename").asString();
                Integer priceRUB = sFor.get("priceRUB").asInt(0);
                if ("TraderOffer".equalsIgnoreCase(typename) && priceRUB > maxPrice) {
                    maxPrice = priceRUB;
                }
            }
            JsonNode usedInTasks = item.get("usedInTasks");
            StringBuilder taskSB = new StringBuilder();
            for (JsonNode task : usedInTasks) {
                Integer needNum = 0;
                Boolean foundInRaid = false;
                task.get("name").asString();
                JsonNode objectives = task.get("objectives");
                for (JsonNode obj : objectives) {
                    if (obj.get("type").asString().equalsIgnoreCase("giveItem")) {
                        JsonNode objItems = obj.get("items");
                        for (JsonNode objItem : objItems) {
                            if (objItem.get("id").asString().equalsIgnoreCase(id)) {
                                needNum = obj.get("count").asInt(0);
                                foundInRaid = obj.get("foundInRaid").asBoolean(false);
                                break;
                            }
                        }
                    }
                }

                taskSB.append("> ").append(task.get("name").asString()).append("( ").append(needNum).append(foundInRaid ? "√" : "").append(" )  \n");
            }
            if (taskSB.isEmpty()) {
                taskSB.append("> 无  \n");
            }

            StringBuilder craftSB = new StringBuilder();
            JsonNode craftsUses = item.get("craftsUsing");
            for (JsonNode craft : craftsUses) {
                String name = craft.get("station").get("name").asString();
                String level = craft.get("level").asString();
                Integer count = 0;
                JsonNode requiredItems = craft.get("requiredItems");
                for (JsonNode requiredItem : requiredItems) {
                    if (requiredItem.get("item").get("id").asString().equalsIgnoreCase(id)) {
                        count = requiredItem.get("count").asInt(0);
                        break;
                    }
                }
                craftSB.append("> ").append(name).append(" lv.").append(level).append(" ( ").append(count).append(" )  \n");
            }
            if (craftSB.isEmpty()) {
                craftSB.append("> 无  ");
            }

            String mdText = "![icon #35px #35px](" + item.get("iconLink").asString() + "): " + item.get("name").asString() + "\n" +
                    "***\n" +
                    "24h均价: " + (avg24hPrice != 0 ? avg24hPrice + " ₽" : "跳蚤禁售") + "   \n" +
                    "跳蚤现价: " + (avg24hPrice != 0 ? lastLowPrice + " ₽" : "跳蚤禁售") + "   \n" +
                    "商人价格: " + maxPrice + " ₽   \n任务需求:\n\n" + taskSB + "\n\n" +
                    "藏身处需求:\n\n" +
                    craftSB + "\n" +
                    "***\n" +
                    formatTime + "\n";

            Keyboard keyboard = Keyboard.builder();
            if (jsonArray.size() > 1) {
                for (int i = 1; i < jsonArray.size(); i++) {
                    String idOther = jsonArray.get(i).get("id").asString();
                    String nameOther = jsonArray.get(i).get("name").asString();
                    Keyboard.Button button = Keyboard.textButtonBuilder()
                            .label(nameOther)
                            .data("跳蚤 " + idOther)
                            .enter(true)
                            .build();
                    keyboard.addRow().addButton(button);
                    if (i >= 5) {
                        break;
                    }
                }
            } else {
                Keyboard.Button button = Keyboard.textButtonBuilder().label("我也要查").data("跳蚤 ").build();
                keyboard.addRow().addButton(button);
            }
//            List<ArrayMsg> arrayMsgs = ArrayMsgUtils.builder().markdown(mdText).keyboard(keyboard).buildList();
            String imgId = item.get("id").asString();
            CacheUtil.put(imgId, mdText, 5L, TimeUnit.SECONDS);
            Page page = PuppeteerUtil.getNewPage(BotUtil.getLocalHost() + "Markdown/" + imgId, 500, 200);
            String imgPath = Constant.BASE_IMG_PATH + "md/" + imgId + ".png";
            PuppeteerUtil.screenshot(page, imgPath);
            return MsgUtils.builder().img(BotUtil.getLocalMedia(imgPath)).build();
        });
    }

}