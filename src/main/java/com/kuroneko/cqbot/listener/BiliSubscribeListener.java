package com.kuroneko.cqbot.listener;

import com.kuroneko.cqbot.core.cfg.ConfigManager;
import com.kuroneko.cqbot.event.BiliSubscribeEvent;
import com.kuroneko.cqbot.service.BiLiService;
import com.kuroneko.cqbot.vo.BiliDynamicVo;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.BotContainer;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationListener;
import org.springframework.data.util.CastUtils;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@AllArgsConstructor
public class BiliSubscribeListener implements ApplicationListener<BiliSubscribeEvent> {
    private final BiLiService biLiService;
    private final BotContainer botContainer;

    @Override
    public void onApplicationEvent(BiliSubscribeEvent event) {
        BiliDynamicVo.BiliDynamicCard source = CastUtils.cast(event.getSource());
        String uid = source.getModules().getModule_author().getMid();
        String dynamicId = source.getId_str();
        List<Long> list = ConfigManager.ins.getBiliCfg().getSubListByUid(uid);
        String path = biLiService.getNewScreenshot(dynamicId, uid);
        MsgUtils msg = biLiService.buildDynamicMsg(path, source);
        botContainer.robots.forEach((id, bot) -> list.forEach(groupId -> {
            bot.sendGroupMsg(groupId, msg.build(), false);
            try {
                Thread.sleep(3600);
            } catch (InterruptedException e) {
                log.error("sleep err", e);
            }
        }));
    }
}
