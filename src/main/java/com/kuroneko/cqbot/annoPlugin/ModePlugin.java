package com.kuroneko.cqbot.annoPlugin;

import com.kuroneko.cqbot.dto.SearchMode;
import com.kuroneko.cqbot.enums.Regex;
import com.kuroneko.cqbot.exception.ExceptionHandler;
import com.kuroneko.cqbot.utils.BotUtil;
import com.mikuac.shiro.annotation.AnyMessageHandler;
import com.mikuac.shiro.annotation.MessageHandlerFilter;
import com.mikuac.shiro.annotation.common.Shiro;
import com.mikuac.shiro.core.Bot;
import com.mikuac.shiro.dto.event.message.AnyMessageEvent;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;

@Shiro
@Slf4j
@Component
@RequiredArgsConstructor
public class ModePlugin {
    private final ExpiringMap<String, SearchMode> expiringMap = ExpiringMap.builder()
            .variableExpiration()
            .expirationPolicy(ExpirationPolicy.CREATED)
            .asyncExpirationListener((key, value) -> onExpiration((SearchMode) value))
            .build();

    @AnyMessageHandler
    @MessageHandlerFilter(cmd = Regex.UNSET_SEARCH_MODE)
    public void unsetSearchMode(Bot bot, AnyMessageEvent event, Matcher matcher) {
        log.info("groupId：{} qq：{} 请求 {}", event.getGroupId(), event.getUserId(), Regex.UNSET_SEARCH_MODE);
        ExceptionHandler.with(bot, event, () -> {
            remove(event.getUserId(), event.getGroupId());
            return "";
        });
    }

    private void remove(Long userId, Long groupId) {
        String key = getKey(userId, groupId);
        SearchMode searchMode = expiringMap.get(key);
        if (searchMode == null) {
            return;
        }
        expiringMap.remove(key);
        BotUtil.at(searchMode.getBot(), searchMode.getUserId(), searchMode.getGroupId(), "不客气哟！");
    }


    public void removeNow(Long userId, Long groupId) {
        String key = getKey(userId, groupId);
        expiringMap.remove(key);
    }

    // 过期通知
    private void onExpiration(SearchMode value) {
        BotUtil.at(value.getBot(), value.getUserId(), value.getGroupId(), "您已经很久没有发送图片啦，帮您退出" + value.getMode() + "模式了哟～\n下次记得说：谢谢 \n来退出");
    }

    public Boolean isSearchMode(String key) {
        return expiringMap.get(key) != null;
    }

    public SearchMode getSearchMode(Long userId, Long groupId) {
        String key = getKey(userId, groupId);
        return expiringMap.get(key);
    }

    public void setSearchMode(String mode, Long userId, Long groupId, Bot bot, String ext) {
        String key = getKey(userId, groupId);
        SearchMode info = new SearchMode(userId, groupId, mode, bot, ext);
        if (isSearchMode(key)) {
            BotUtil.at(bot, userId, groupId, "当前已经处于 " + mode + " 模式啦，请直接发送图片。");
            return;
        }
        expiringMap.put(key, info, 100L, TimeUnit.SECONDS);
        BotUtil.at(bot, userId, groupId, "您已进入 " + mode + " 模式，请发送图片。");
    }

    public void resetExpiration(Long userId, Long groupId) {
        String key = getKey(userId, groupId);
        expiringMap.resetExpiration(key);
    }

    public Boolean check(String mode, Long userId, Long groupId) {
        String key = getKey(userId, groupId);
        // 判断当前检索模式与 SearchModeBean 中是否一致，否则会执行所有检索插件
        SearchMode searchMode = expiringMap.get(key);
        if (searchMode == null) {
            return false;
        }
        // 判断是否处于搜索模式
        return searchMode.getMode().equals(mode);
    }

    private String getKey(Long userId, Long groupId) {
        return userId.toString() + "_" + groupId;
    }

}

