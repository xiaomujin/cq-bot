package com.kuroneko.cqbot;

import com.alibaba.fastjson2.JSONObject;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.handler.event.MessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

@Slf4j
@Primary
@Component
public class MyMessageEvent extends MessageEvent {
    @Override
    public void handler(Bot bot, JSONObject resp) {
        resp.put("message_id", 1);
        resp.put("font", 1);
        if (resp.getJSONObject("sender").getLongValue("user_id") == 1419229777L) {
            String build = MsgUtils.builder().img("https://infinityicon.infinitynewtab.com/user-share-icon/d8b62f4d64bda8800b1c788cd5ba3c68.png").text("1234").build();
            bot.sendGroupMsg(571632659L, build, false);
        }
        log.info(resp.toString());
        super.handler(bot, resp);
    }
}
