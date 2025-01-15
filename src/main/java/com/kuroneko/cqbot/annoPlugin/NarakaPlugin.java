package com.kuroneko.cqbot.annoPlugin;

import com.kuroneko.cqbot.core.annotation.BotHandler;
import com.kuroneko.cqbot.core.annotation.BotMsgHandler;
import com.kuroneko.cqbot.core.cfg.ConfigManager;
import com.kuroneko.cqbot.core.dto.MsgInfo;
import com.kuroneko.cqbot.enums.Regex;
import com.kuroneko.cqbot.enums.sysPluginRegex;
import com.kuroneko.cqbot.exception.ExceptionHandler;
import com.kuroneko.cqbot.service.NarakaService;
import com.kuroneko.cqbot.utils.BotUtil;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.regex.Matcher;

@BotHandler
@Slf4j
@Component
@RequiredArgsConstructor
public class NarakaPlugin {

    private final NarakaService narakaService;

    @BotMsgHandler(model = sysPluginRegex.NARAKA_SYSTEM, cmd = Regex.NARAKA_RECORD)
    public void recordHandler(MsgInfo msgInfo, Bot bot, AnyMessageEvent event, Matcher matcher) {
        log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), "永劫战绩");
        String text = matcher.group("text");
        var msgId = event.getMessageId();
        ExceptionHandler.with(bot, event, () -> {
            List<String> params = BotUtil.getParams(text);
            String name = null;
            int gameMode = 2;
            int seasonId = ConfigManager.ins.getAdminCfg().getNarakaSeasonId();
            switch (params.size()) {
                case 2:
                    gameMode = getGameMode(params.get(1));
                case 1:
                    name = params.getFirst();
            }
            if (name == null) {
                return "缺少玩家昵称";
            }
            bot.setGroupReaction(event.getGroupId(), msgId, "424", true);
            return narakaService.getRecord(name, gameMode, seasonId);
        }, () -> bot.setGroupReaction(event.getGroupId(), msgId, "424", false));
    }

    private int getGameMode(String mode) {
        return switch (mode) {
            case "1" -> 1;
            case "2" -> 12;
            case "3" -> 2;
            default -> 2;
        };
    }


}

