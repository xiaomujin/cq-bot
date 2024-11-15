package com.kuroneko.cqbot.event;

import com.kuroneko.cqbot.config.localCfg.UpdateCfg;
import com.kuroneko.cqbot.core.cfg.ConfigManager;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.core.CoreEvent;
import lombok.AllArgsConstructor;
import org.springframework.context.annotation.Primary;
import org.springframework.stereotype.Component;

import java.time.Instant;

@Primary
@Component
@AllArgsConstructor
public class BotCoreEvent extends CoreEvent {

    @Override
    public void online(Bot bot) {
        UpdateCfg updateCfg = ConfigManager.ins.getUpdateCfg();
        if (updateCfg.isRead()) {
            return;
        }
        long startTime = updateCfg.getStartTime();
        long now = Instant.now().getEpochSecond();
        MsgUtils msg = MsgUtils.builder();
        long m = (now - startTime) / 60L;
        long s = (now - startTime) - m * 60L;
        msg.text(" 重启耗时" + m + "分钟" + s + "秒");
        if (updateCfg.getGroupId() != null) {
            msg.at(updateCfg.getUserID());
            bot.sendGroupMsg(updateCfg.getGroupId(), msg.build(), false);
        } else if (updateCfg.getUserID() != null) {
            bot.sendPrivateMsg(updateCfg.getUserID(), msg.build(), false);
        }
        ConfigManager.ins.getUpdateCfg().read(true);
    }
}
