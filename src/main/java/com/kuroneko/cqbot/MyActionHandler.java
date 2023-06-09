package com.kuroneko.cqbot;

import com.alibaba.fastjson2.JSONObject;
import com.mikuac.shiro.enums.ActionPath;
import com.mikuac.shiro.handler.ActionHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketSession;

import java.util.Map;

@Slf4j
@Primary
@Component
public class MyActionHandler extends ActionHandler {
    @Override
    public JSONObject action(WebSocketSession session, ActionPath action, Map<String, Object> params) {
        JSONObject jsonObject = super.action(session, action, params);
        if (jsonObject != null && jsonObject.getJSONObject("data").containsKey("message_id")) {
            jsonObject.getJSONObject("data").put("message_id", 1);
        }
        return jsonObject;
    }
}
