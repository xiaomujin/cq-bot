package com.kuroneko.cqbot.modulePlugin.tkf;

import com.kuroneko.cqbot.core.dto.MsgInfo;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;


@Slf4j
@Component
public class TKFEvent {


    /**
     * 元事件分发
     */
    public void handler(MsgInfo msgInfo, Bot bot, AnyMessageEvent event) {
        String msg = msgInfo.getMsg();
        // 注解执行对应方法
    }

    public void say(MsgInfo msgInfo, Bot bot, AnyMessageEvent event) {
        System.out.println(msgInfo);
        System.out.println(Thread.currentThread());
    }

}
