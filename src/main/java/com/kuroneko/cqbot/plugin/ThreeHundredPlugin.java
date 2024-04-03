package com.kuroneko.cqbot.plugin;

import cn.hutool.core.io.FileUtil;
import com.alibaba.fastjson2.JSONObject;
import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.utils.BotUtil;
import com.kuroneko.cqbot.utils.PuppeteerUtil;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import com.ruiyun.jvppeteer.core.page.Page;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.List;

@Slf4j
@Component
public class ThreeHundredPlugin extends BotPlugin {
    private final RestTemplate restTemplate;

    String imgPath = Constant.BASE_IMG_PATH + "300/";

    public ThreeHundredPlugin(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        FileUtil.mkdir(imgPath);
    }

    public MsgUtils getMsg(String text) {
        String roleName = "";
        String index = "排位赛";
        List<String> params = BotUtil.getParams(CmdConst.THREE_HUNDRED_KD, text);
        switch (params.size()) {
            case 2, 3, 4:
                index = params.get(1);
            case 1:
                roleName = params.get(0);
                break;
            case 0:
                return MsgUtils.builder().text("名称不存在");
        }
        String i = switch (index) {
            case "竞技场" -> "0";
            case "战场" -> "1";
            case "排位赛" -> "2";
            case "闪电战" -> "3";
            case "战场S1赛季" -> "4";
            case "新天赋" -> "5";
            default -> "2";
        };

        String entity = "AccountID=0&Guid=0&RoleName=" + URLEncoder.encode(roleName, StandardCharsets.UTF_8);
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        HttpEntity<String> requestEntity = new HttpEntity<>(entity, headers);
        JSONObject jsonObject = restTemplate.postForObject("https://300report.jumpw.com/api/battle/searchNormal?type=h5", requestEntity, JSONObject.class);
        assert jsonObject != null;
        if (jsonObject.getBoolean("success").equals(Boolean.FALSE)) {
            return MsgUtils.builder().text(jsonObject.getString("msg"));
        }
        String roleID = jsonObject.getJSONObject("data").getLong("RoleID").toString();
        String fun = """
                ()=>{
                    document.querySelectorAll(".select li a")[%s].click();
                    document.querySelectorAll(".select")[0].click();
                    return true;
                }
                """;
        String format = String.format(fun, i);
        String path = imgPath + roleID + "_" + i + ".png";
        String url = "https://300report.jumpw.com/#/MyScore?r=" + roleID + "&m=0";
        Page newPage = PuppeteerUtil.getNewPage(url, "networkidle0", 30000, 1920, 3000);
        try {
            newPage.waitForFunction(format);
            //等待页面切换图片加载
            newPage.waitFor("500");
        } catch (InterruptedException e) {
            log.error("300战绩查询失败", e);
            return MsgUtils.builder().text(roleName + "(" + roleID + ")" + "300战绩查询失败");
        }
        PuppeteerUtil.screenshot(newPage, path, "#app", "#app {height: 1200px;}");
        String absolutePath = new File(path).toURI().toASCIIString();
        log.info("{} 路径:{}", CmdConst.THREE_HUNDRED_KD, absolutePath);
        OneBotMedia media = OneBotMedia.builder().file(absolutePath).cache(false);
        return MsgUtils.builder().img(media);
    }

    @Override
    public int onAnyMessage(Bot bot, AnyMessageEvent event) {
        if (event.getRawMessage().startsWith(CmdConst.THREE_HUNDRED_KD)) {
            log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), CmdConst.THREE_HUNDRED_KD);
            MsgUtils msg = getMsg(event.getRawMessage());
            bot.sendMsg(event, msg.build(), false);
            return MESSAGE_BLOCK;
        }
        return MESSAGE_IGNORE;
    }
}
