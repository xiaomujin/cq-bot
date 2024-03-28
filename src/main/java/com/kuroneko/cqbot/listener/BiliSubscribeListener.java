package com.kuroneko.cqbot.listener;

import com.kuroneko.cqbot.constant.RedisKey;
import com.kuroneko.cqbot.event.BiliSubscribeEvent;
import com.kuroneko.cqbot.service.BiLiService;
import com.kuroneko.cqbot.utils.RedisUtil;
import com.kuroneko.cqbot.vo.BiliDynamicVo;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.BotContainer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.data.util.CastUtils;
import org.springframework.stereotype.Component;

import java.util.Set;

@Slf4j
@Component
@AllArgsConstructor
public class BiliSubscribeListener implements ApplicationListener<BiliSubscribeEvent> {
    private final BiLiService biLiService;
    private final BotContainer botContainer;
    private final RedisUtil redisUtil;

    @Override
    public void onApplicationEvent(BiliSubscribeEvent event) {
        BiliDynamicVo.BiliDynamicCard source = CastUtils.cast(event.getSource());
        String uid = source.getModules().getModule_author().getMid();
        String dynamicId = source.getId_str();
        Set<Number> list = redisUtil.members(RedisKey.BILI_SUB + ":" + uid);
        String path = biLiService.getNewScreenshot(dynamicId, uid);
        MsgUtils msg = biLiService.buildDynamicMsg(path, dynamicId);
        botContainer.robots.forEach((id, bot) -> list.forEach(groupId -> {
            bot.sendGroupMsg(groupId.longValue(), msg.build(), false);
            try {
                Thread.sleep(3600);
            } catch (InterruptedException e) {
                log.error("sleep err", e);
            }
        }));
    }
}
