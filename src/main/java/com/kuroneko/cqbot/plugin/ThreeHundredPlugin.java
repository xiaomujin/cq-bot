package com.kuroneko.cqbot.plugin;

import com.alibaba.fastjson2.JSONObject;
import com.kuroneko.cqbot.constant.CmdConst;
import com.kuroneko.cqbot.constant.Constant;
import com.kuroneko.cqbot.utils.JvppeteerUtil;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.common.utils.OneBotMedia;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.BotPlugin;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

import java.io.File;
import java.net.URLEncoder;
import java.nio.charset.Charset;

@Slf4j
@Component
public class ThreeHundredPlugin extends BotPlugin {
    private final RestTemplate restTemplate;

    String imgPath = Constant.BASE_IMG_PATH + "300/";

    public ThreeHundredPlugin(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
        File file = new File(imgPath);
        if (!file.exists()) {
            file.mkdirs();
        }
    }

    private MsgUtils getMsg(String text) {
        String query = "";
        String index = "排位赛";
        if (text.length() > 3) {
            query = text.substring(CmdConst.THREE_HUNDRED_KD.length()).trim();
        }
        String[] strings = query.split(" ");
        switch (strings.length) {
            case 2:
                index = strings[1];
            case 1:
                query = strings[0];
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

        String entity = "AccountID=0&Guid=0&RoleName=" + URLEncoder.encode(query, Charset.defaultCharset());
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
        String path = imgPath + roleID + ".png";
        JvppeteerUtil.screenshot("https://300report.jumpw.com/#/MyScore?r=" + roleID + "&m=0", path, "#app", "#app {height: 1200px;}", format);
        log.info(roleID);
//        ZeroMagnetVo zeroMagnetVo = threeHundredService.getZeroMagnetVo(query, index);
//        return MsgUtils.builder()
//                .text("title:" +zeroMagnetVo.getTitle() + Constant.XN)
//                .text(zeroMagnetVo.getMagnet() + Constant.XN)
//                .text("size:" + zeroMagnetVo.getSize());
        OneBotMedia media = OneBotMedia.builder().file("http://localhost:8081/getImage?path=" + URLEncoder.encode(path, Charset.defaultCharset())).cache(false);
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
