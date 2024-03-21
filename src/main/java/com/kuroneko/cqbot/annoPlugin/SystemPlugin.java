package com.kuroneko.cqbot.annoPlugin;

import com.kuroneko.cqbot.enums.Regex;
import com.kuroneko.cqbot.utils.RedisUtil;
import com.kuroneko.cqbot.vo.UpdateCache;
import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;
import java.util.regex.Matcher;

@Shiro
@Slf4j
@Component
@AllArgsConstructor
public class SystemPlugin {
    private final RedisUtil redisUtil;

    @AnyMessageHandler()
    @MessageHandlerFilter(cmd = Regex.SYS_UPDATE)
    public void update(Bot bot, AnyMessageEvent event, Matcher matcher) {
        Long qq = event.getSender().getUserId();
        List<Long> list = Arrays.asList(1419229777L, 728109103L);
        if (!list.contains(qq)) {
            MsgUtils msg = MsgUtils.builder();
            msg.text("只有主人才能更新哦！");
            bot.sendMsg(event, msg.build(), false);
            return;
        }
        redisUtil.set("SYS_UPDATE", new UpdateCache(event.getGroupId(), qq));
        MsgUtils msg = MsgUtils.builder();
        msg.text("开始更新，预计需要3分钟！");
        bot.sendMsg(event, msg.build(), false);

        ProcessBuilder pb = new ProcessBuilder("sh", "/mnt/qqbot/wsserver/release.sh");
        pb.redirectOutput(ProcessBuilder.Redirect.INHERIT);
        pb.redirectError(ProcessBuilder.Redirect.INHERIT);
        pb.redirectInput(ProcessBuilder.Redirect.INHERIT);
        try {
            pb.start();
        } catch (IOException e) {
            bot.sendMsg(event, MsgUtils.builder().text(e.getMessage()).build(), false);
            log.error("重启失败", e);
        }

    }
}
