package com.kuroneko.cqbot.annoPlugin;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.enums.Regex;
import com.kuroneko.cqbot.exception.ExceptionHandler;
import com.kuroneko.cqbot.utils.BotUtil;
import com.kuroneko.cqbot.utils.CacheUtil;
import com.kuroneko.cqbot.utils.PuppeteerUtil;
import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.mikuac.shiro.constant.ActionParams;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.action.common.ActionData;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.mikuac.shiro.enums.ActionPathEnum;
import com.ruiyun.jvppeteer.core.page.Page;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

@Shiro
@Slf4j
@Component
@RequiredArgsConstructor
public class BangumiPlugin {

    @AnyMessageHandler()
    @MessageHandlerFilter(cmd = Regex.BANGUMI_CALENDAR)
    public void bangumiCalendar(Bot bot, AnyMessageEvent event, Matcher matcher) {
        log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), Regex.BANGUMI_CALENDAR);
        ExceptionHandler.with(bot, event, () -> CacheUtil.getOrPut(Regex.BANGUMI_CALENDAR, 20, TimeUnit.MINUTES, () -> {
            String imgPath = STR."\{Constant.BASE_IMG_PATH}BANGUMI_CALENDAR.png";
            Page newPage = PuppeteerUtil.getNewPage(Constant.AGE_HOST_URL);
            PuppeteerUtil.screenshot(newPage
                    , imgPath
                    , "div.text_list_box.weekly_list.mb-4"
                    , ".global_notice_wrapper {display: none !important;} .head_content_wrapper {display: none !important;}"
            );
            OneBotMedia localMedia = BotUtil.getLocalMedia(imgPath, false);
            return MsgUtils.builder()
                    .img(localMedia)
                    .text("age：").text("https://rentry.la/agefans\n")
                    .text("次元城：").text("https://www.cycdm01.top/")
                    .build();
        }));
    }

    @AnyMessageHandler()
    @MessageHandlerFilter(cmd = "(\\[CQ:at,.*?\\]\\s*)?md2")
    public void md(Bot bot, AnyMessageEvent event, Matcher matcher) {
        log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), "md");
        JSONObject content = new JSONObject();
        content.put("content", STR."""
                @\{event.getSender().getNickname()}
                ---\s
                \{event.getSender().getUserId()}""");

        JSONObject rowsObj = new JSONObject();
        JSONArray rows = new JSONArray();
        JSONObject buttonsObj = new JSONObject();
        JSONArray buttons = new JSONArray();
        JSONObject buttonObj = new JSONObject();

        JSONObject renderData = new JSONObject();
        renderData.put("label", "+1");
        renderData.put("visited_label", "点了！");
        renderData.put("style", 1);
        JSONObject action = new JSONObject();
        action.put("type", 2);
        JSONObject permisson = new JSONObject();
        permisson.put("type", 2);
        action.put("data", "md");
        action.put("permission", permisson);
        action.put("unsupport_tips", "不支持");

        buttonObj.put("render_data", renderData);
        buttonObj.put("action", action);

        buttons.add(buttonObj);
        buttonsObj.put("buttons", buttons);
        rows.add(buttonsObj);
        rowsObj.put("rows", rows);

        JSONArray contents = new JSONArray();
//        JSONObject con = new JSONObject();
        JSONObject markdown = new JSONObject();
        markdown.put("type", "markdown");
        JSONObject d1 = new JSONObject();
        d1.put("content", content.toJSONString());
        markdown.put("data", d1);
        JSONObject keyboard = new JSONObject();
        keyboard.put("type", "keyboard");
        JSONObject d2 = new JSONObject();
        JSONObject d3 = new JSONObject();
        d3.put("rows",rows);
        d2.put("content", d3);
        keyboard.put("data", d2);
        contents.add(markdown);
        contents.add(keyboard);
//        con.put("content",contents);

        ExceptionHandler.with(bot, event, () -> CacheUtil.getOrPut("md", 1, TimeUnit.SECONDS, () -> {
//            List<Map<String, Object>> maps = generateForwardMsg("100000", "小助手", List.of(
//                    STR."[CQ:markdown,content=\{content.toJSONString()}][CQ:keyboard,content=\{rowsObj.toJSONString()}]"
//            ));
            List<Map<String, Object>> maps = generateForwardMsg("100000", "小助手", contents);
            System.out.println(JSON.toJSONString(maps));

            JSONObject mk = JSON.parseObject("""
                    {
                        "type": "node",
                        "data": {
                            "name": "小助手",
                            "uin": "100000",
                            "content": [
                                {
                                    "type": "markdown",
                                    "data": {
                                        "content": "{\\"content\\":\\"123456\\"}"
                                    }
                                },
                                {
                                    "type": "keyboard",
                                    "data": {
                                        "content": {
                                            "rows": [
                                                {
                                                    "buttons": [
                                                        {
                                                            "render_data": {
                                                                "label": "点击前",
                                                                "visited_label": "点击后",
                                                                "style": 1
                                                            },
                                                            "action": {
                                                                "type": 2,
                                                                "data": "/echo 2222",
                                                                "permission": {
                                                                    "type": 2,
                                                                    "specify_user_ids": [
                                                                        "100000"
                                                                    ]
                                                                },
                                                                "unsupport_tips": "这是一个markdown消息喵~"
                                                            }
                                                        }
                                                    ]
                                                }
                                            ]
                                        }
                                    }
                                }
                            ]
                        }
                    }""");

//            ActionData<String> actionData = sendForwardMsg(bot, event, maps);// data -> VCRrRDOF4xHq5ClbLb0XC0TSe3X7oWtg/mrjoNprYHyUYtudgYuubT9SftypV0eh
            Map<String, Object> m = mk.to(new TypeReference<Map<String, Object>>() {
            }.getType());
            List<Map<String, Object>> maps1 = List.of(m);
            System.out.println(JSON.toJSONString(maps1));
            ActionData<String> actionData = sendForwardMsg(bot, event, maps);// data -> VCRrRDOF4xHq5ClbLb0XC0TSe3X7oWtg/mrjoNprYHyUYtudgYuubT9SftypV0eh
            bot.sendMsg(event, STR."[CQ:longmsg,id=\{actionData.getData()}]", false);
            return "";
        }));
    }

    public List<Map<String, Object>> generateForwardMsg(String uin, String name, List<String> contents) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        contents.forEach((msg) -> {
            System.out.println(msg);
            Map<String, Object> node = new HashMap<>();
            node.put("type", "node");
            Map<String, Object> data = new HashMap<>();
            data.put("name", name);
            data.put("uin", uin);
            data.put("content", msg);
            node.put("data", data);
            nodes.add(node);
        });
        return nodes;
    }

    public List<Map<String, Object>> generateForwardMsg(String uin, String name, Object contents) {
        List<Map<String, Object>> nodes = new ArrayList<>();
        Map<String, Object> node = new HashMap<>();
        node.put("type", "node");
        Map<String, Object> data = new HashMap<>();
        data.put("name", name);
        data.put("uin", uin);
        data.put("content", contents);
        node.put("data", data);
        nodes.add(node);
        return nodes;
    }

    public ActionData<String> sendForwardMsg(Bot bot, AnyMessageEvent event, List<Map<String, Object>> msg) {
        JSONObject params = new JSONObject();
        params.put(ActionParams.MESSAGES, msg);
        if (ActionParams.GROUP.equals(event.getMessageType())) {
            params.put(ActionParams.GROUP_ID, event.getGroupId());
        }
        if (ActionParams.PRIVATE.equals(event.getMessageType())) {
            params.put(ActionParams.USER_ID, event.getUserId());
        }
//        JSONObject result = BotUtil.actionHandler.action(bot.getSession(), ActionPathEnum.SEND_FORWARD_MSG, params);
//        return result != null ? result.to(new TypeReference<ActionData<String>>() {
//        }.getType()) : null;
        return null;
    }
}

