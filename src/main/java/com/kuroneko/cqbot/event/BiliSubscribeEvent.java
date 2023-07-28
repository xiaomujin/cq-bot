package com.kuroneko.cqbot.event;

import com.kuroneko.cqbot.vo.BiliDynamicVo;
import org.springframework.context.ApplicationEvent;

public class BiliSubscribeEvent extends ApplicationEvent {
    public BiliSubscribeEvent(BiliDynamicVo.BiliDynamicCard source) {
        super(source);
    }
}
