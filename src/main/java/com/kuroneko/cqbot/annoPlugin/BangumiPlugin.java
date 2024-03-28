package com.kuroneko.cqbot.annoPlugin;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONArray;
import com.alibaba.fastjson2.JSONObject;
import com.alibaba.fastjson2.TypeReference;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.enums.Regex;
import com.kuroneko.cqbot.exception.ExceptionHandler;
import com.kuroneko.cqbot.lagrange.markdown.Keyboard;
import com.kuroneko.cqbot.lagrange.markdown.Markdown;
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
import com.mikuac.shiro.enums.AtEnum;
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
    @MessageHandlerFilter(cmd = "md2", at = AtEnum.NEED)
    public void md(Bot bot, AnyMessageEvent event, Matcher matcher) {
        log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), "md2");
        String con = """
                ---\s
                [点我喵喵叫](mqqapi://aio/inlinecmd?command=喵呜&reply=false&enter=true)""";

        JSONArray contents = new JSONArray();
        Markdown markdown = Markdown.Builder().setContent(con);
        contents.add(markdown);
        Keyboard keyboard = Keyboard.Builder().addButton("+1", "md2");
        contents.add(keyboard);

        ExceptionHandler.with(bot, event, () -> CacheUtil.getOrPut("md", 1, TimeUnit.SECONDS, () -> {

            List<Map<String, Object>> maps = generateForwardMsg("100000", "小助手", contents);
            System.out.println(JSON.toJSONString(maps));

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
        JSONObject result = BotUtil.actionHandler.action(bot.getSession(), ActionPathEnum.SEND_FORWARD_MSG, params);
        return result != null ? result.to(new TypeReference<ActionData<String>>() {
        }.getType()) : null;
    }
}

