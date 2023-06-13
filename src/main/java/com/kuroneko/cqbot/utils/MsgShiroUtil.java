package com.kuroneko.cqbot.utils;

import com.mikuac.shiro.common.utils.ShiroUtils;
import com.mikuac.shiro.enums.MsgTypeEnum;
import com.mikuac.shiro.model.ArrayMsg;

import java.util.List;
import java.util.stream.Collectors;

public class MsgShiroUtil {
    public static boolean isAtMe(List<ArrayMsg> arrayMsg, long id) {
        return MsgShiroUtil.getAtList(arrayMsg).contains(id);
    }

    public static List<Long> getAtList(List<ArrayMsg> arrayMsg) {
        return ShiroUtils.getAtList(arrayMsg);
    }

    public static String getText(List<ArrayMsg> arrayMsg) {
        return MsgShiroUtil.getText(arrayMsg, ",");
    }

    public static String getText(List<ArrayMsg> arrayMsg, String join) {
        return arrayMsg.stream()
                .filter(it -> MsgTypeEnum.text == it.getType())
                .map(it -> it.getData().get("text"))
                .collect(Collectors.joining(join));
    }
}
