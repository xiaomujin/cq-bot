package com.kuroneko.cqbot.utils;

import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;
import lombok.extern.slf4j.Slf4j;

import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@Slf4j
public class MsgShiroUtil {
    private MsgShiroUtil() {
    }

    public static boolean isAtMe(List<ArrayMsg> arrayMsg, long id) {
        return MsgShiroUtil.getAtList(arrayMsg).contains(id);
    }

    public static List<Long> getAtList(List<ArrayMsg> arrayMsg) {
        return ShiroUtils.getAtList(arrayMsg);
    }

    public static String getText(List<ArrayMsg> arrayMsg) {
        return MsgShiroUtil.getText(arrayMsg, ",").trim();
    }

    public static String getText(List<ArrayMsg> arrayMsg, String join) {
        return arrayMsg.stream()
                .filter(it -> MsgTypeEnum.text == it.getType())
                .map(it -> it.getData().get("text"))
                .collect(Collectors.joining(join));
    }

    public static Optional<String> getOneParam(String cmd, String msg) {
        Optional<String> text = Optional.empty();
        if (msg.trim().length() > cmd.length()) {
            text = Optional.of(msg.substring(cmd.length()).trim());
        }
        log.info("{}:getOneParam:{}", cmd, text);
        return text;
    }
}
