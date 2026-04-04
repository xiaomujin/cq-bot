package com.kuroneko.cqbot.service;

import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpiringMap;
import net.jodah.expiringmap.ExpirationPolicy;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

@Service
@Slf4j
public class AiService {
    private static final String DEFAULT_ANSWER = "我暂时不知道该怎么回答。";

    private final ChatClient chatClient;
    private final ChatMemory chatMemory;
    private final boolean enabled;
    private final ExpiringMap<String, Boolean> activeConversations;
    private final ConcurrentHashMap<String, Object> conversationLocks = new ConcurrentHashMap<>();

    public AiService(ObjectProvider<ChatClient> chatClientProvider,
                     ObjectProvider<ChatMemory> chatMemoryProvider,
                     @Value("${bot.ai.enabled:false}") boolean enabled,
                     @Value("${bot.ai.session-expire-minutes:30}") long sessionExpireMinutes) {
        ChatClient resolvedChatClient = chatClientProvider.getIfAvailable();
        ChatMemory resolvedChatMemory = chatMemoryProvider.getIfAvailable();
        this.chatClient = resolvedChatClient;
        this.chatMemory = resolvedChatMemory;
        this.enabled = enabled;
        long expireMinutes = Math.max(1, sessionExpireMinutes);
        this.activeConversations = ExpiringMap.builder()
                .expiration(expireMinutes, TimeUnit.MINUTES)
                .expirationPolicy(ExpirationPolicy.ACCESSED)
                .expirationListener((conversationId, ignored) -> {
                    String conversationKey = String.valueOf(conversationId);
                    if (resolvedChatMemory != null) {
                        resolvedChatMemory.clear(conversationKey);
                    }
                    this.conversationLocks.remove(conversationKey);
                })
                .build();
    }

    public boolean isEnabled() {
        return enabled && chatClient != null && chatMemory != null;
    }

    public String getAiAnswer(long groupId, String text) {
        if (!enabled) {
            return "AI 功能未启用";
        }
        if (!StringUtils.hasText(text)) {
            return "你想问什么呢";
        }

        String question = text.trim();
        String conversationId = String.valueOf(groupId);
        Object lock = conversationLocks.computeIfAbsent(conversationId, key -> new Object());

        synchronized (lock) {
            activeConversations.put(conversationId, Boolean.TRUE);
            try {
                String answer = chatClient.prompt()
                        .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                        .user(question)
                        .call()
                        .content();
                return normalizeAnswer(answer);
            } catch (Exception e) {
                log.error("群 {} AI 提问失败: {}", groupId, question, e);
                return "emmmm，AI 暂时不可用，请稍后再试";
            }
        }
    }

    private String normalizeAnswer(String answer) {
        return StringUtils.hasText(answer) ? answer.trim() : DEFAULT_ANSWER;
    }
}
