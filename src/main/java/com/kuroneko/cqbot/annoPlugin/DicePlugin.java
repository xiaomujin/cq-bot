package com.kuroneko.cqbot.annoPlugin;

import com.kuroneko.cqbot.enums.Regex;
import com.kuroneko.cqbot.exception.ExceptionHandler;
import com.kuroneko.cqbot.utils.BotUtil;
import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.common.utils.MsgUtils;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.ThreadLocalRandom;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Shiro
@Slf4j
@Component
@RequiredArgsConstructor
public class DicePlugin {
    private final Pattern numP = Pattern.compile("\\d+");

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = Regex.DICE)
    public void handler(Bot bot, AnyMessageEvent event, Matcher matcher) {
        log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), Regex.DICE);
        ExceptionHandler.with(bot, event, () -> {
            String cmd = matcher.group("cmd");
            List<String> params = BotUtil.getParams(cmd, event.getRawMessage());
            MsgUtils msg = switch (params.size()) {
                case 1 -> buildMsg1(params.getFirst());
                case 2 -> buildMsg2(params.getFirst(), params.get(1));
                default -> MsgUtils.builder().text("""
                        请正确输入指令
                        [.r 数字] 或者 [.r 数字 数字]"""
                );
            };
            assert msg != null;
            return msg.reply(event.getMessageId()).build();
        });
    }

    private MsgUtils buildMsg2(String first, String second) {
        long num1 = parseLong(first);
        long num2 = parseLong(second);
        if (num1 - num2 > 0) {
            long temp = num1;
            num1 = num2;
            num2 = temp;
        }
        long nextLong = random(num1, num2);
        return MsgUtils.builder().text("范围：[" + num1 + "-" + num2 + "]\n结果：" + nextLong);
    }

    private MsgUtils buildMsg1(String first) {
        return buildMsg2("1", first);
    }


    /**
     * 取值范围：[min,max]
     */
    public long random(long min, long max) {
        if (max - min == 0) {
            return min;
        }
        return min + ThreadLocalRandom.current().nextLong(max - min + 1);
    }

    /**
     * 取值范围：[min,max]
     */
    public long parseLong(String s) {
        Matcher matcher = numP.matcher(s);
        if (matcher.find()) {
            return Long.parseLong(matcher.group());
        }
        return 0L;
    }

}

