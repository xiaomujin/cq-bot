package com.kuroneko.cqbot.event;

import com.alibaba.fastjson2.JSONObject;
import com.kuroneko.cqbot.utils.RedisUtil;
import com.kuroneko.cqbot.vo.UpdateCache;
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
    private final RedisUtil redisUtil;

    @Override
    public void online(Bot bot) {
//        JSONObject sysUpdate = redisUtil.get("SYS_UPDATE");
//        if (sysUpdate == null) {
//            return;
//        }
//        redisUtil.delete("SYS_UPDATE");
//        UpdateCache updateCache = sysUpdate.to(UpdateCache.class);
//        long startTime = updateCache.getStartTime();
//        long now = Instant.now().getEpochSecond();
//        MsgUtils msg = MsgUtils.builder();
//        long m = (now - startTime) / 60L;
//        long s = (now - startTime) - m * 60L;
//        msg.at(updateCache.getUserID()).text("重启耗时" + m + "分钟" + s + "秒");
//        if (updateCache.getGroupId() != null) {
//            bot.sendGroupMsg(updateCache.getGroupId(), msg.build(), false);
//        } else if (updateCache.getUserID() != null) {
//            bot.sendPrivateMsg(updateCache.getUserID(), msg.build(), false);
//        }
    }
}
