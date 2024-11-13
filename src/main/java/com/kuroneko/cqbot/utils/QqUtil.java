package com.kuroneko.cqbot.utils;

import com.kuroneko.cqbot.core.cfg.ConfigManager;
import com.mikuac.shiro.dto.event.message.GroupMessageEvent;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@NoArgsConstructor
public class QqUtil {

    public boolean verifyQq(Long userId) {
        List<Long> adminList = ConfigManager.ins.getAdminCfg().getAdminList();
        return !adminList.contains(userId);
//        if (event.getSender().getRole().equals("member")) {
//
//        } else {
//            return false;
//        }
    }
}
