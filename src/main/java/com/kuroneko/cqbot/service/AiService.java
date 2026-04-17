package com.kuroneko.cqbot.service;

import com.kuroneko.cqbot.core.cfg.ConfigManager;
import lombok.extern.slf4j.Slf4j;
import net.jodah.expiringmap.ExpirationPolicy;
import net.jodah.expiringmap.ExpiringMap;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.content.Media;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.util.CollectionUtils;
import org.springframework.util.MimeTypeUtils;
import org.springframework.util.StringUtils;
import reactor.core.publisher.Flux;

import java.net.MalformedURLException;
import java.net.URI;
import java.net.URL;
import java.util.List;
import java.util.Optional;
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

    /**
     * 获取 AI 回答，流式返回
     * 
     * @param request AI 请求对象
     * @return Flux<String> 流式 AI 回答文本片段
     */
    public Flux<String> getAiAnswer(AiRequest request) {
        if (!enabled) {
            return Flux.just("AI 功能未启用");
        }
        if (request == null || !StringUtils.hasText(request.text())) {
            return Flux.just("你想问什么呢");
        }

        String question = request.text().trim();
        String conversationId = String.valueOf(request.groupId());
        List<String> imageUrls = request.imageUrls();
        Optional<URI> uri = Optional.empty();
        if (!CollectionUtils.isEmpty(imageUrls)) {
            try {
                uri = Optional.of(URI.create(imageUrls.getFirst()));
            } catch (IllegalArgumentException e) {
                log.error("群 {} 用户 {} 图片地址 {} 格式不正确", request.groupId(), request.userId(), imageUrls.getFirst(), e);
            }
        }
        Object lock = conversationLocks.computeIfAbsent(conversationId, _ -> new Object());

        synchronized (lock) {
            activeConversations.put(conversationId, Boolean.TRUE);
            try {
                ChatClient.ChatClientRequestSpec spec = chatClient.prompt()
                        .advisors(advisorSpec -> advisorSpec.param(ChatMemory.CONVERSATION_ID, conversationId))
                        .system(buildSystemPrompt(request))
                        .user(question);
                uri.ifPresent(u -> spec.user(p -> p.media(new Media(MimeTypeUtils.IMAGE_PNG, u))));
                return spec.stream()
                        .content()
                        .doOnError(e -> log.error("群 {} 用户 {} AI 流式响应异常", request.groupId(), request.userId(), e))
                        .onErrorResume(e -> {
                            log.error("群 {} 用户 {} AI 提问失败: {}", request.groupId(), request.userId(), question, e);
                            return Flux.just("emmmm，AI 暂时不可用，请稍后再试");
                        });
            } catch (Exception e) {
                log.error("群 {} 用户 {} AI 提问失败: {}", request.groupId(), request.userId(), question, e);
                return Flux.just("emmmm，AI 暂时不可用，请稍后再试");
            }
        }
    }

    private String buildSystemPrompt(AiRequest request) {
        String systemPrompt = ConfigManager.ins.getAdminCfg().getSystemPrompt();
        String userContext = buildUserContext(request);
        return systemPrompt + "\n\n" + userContext;
    }

    private String buildUserContext(AiRequest request) {
        return """
                ---
                以下是当前会话元数据，仅作背景参考：
                - groupId 群号: %d
                - userId QQ号: %d
                - nickname 昵称: %s
                ---
                """.formatted(request.groupId(), request.userId(), sanitizeContext(request.nickname()));
    }

    private String sanitizeContext(String value) {
        return StringUtils.hasText(value) ? value.replaceAll("\\s+", " ").trim() : "unknown";
    }

    public record AiRequest(long groupId, long userId, String nickname, String text, List<String> imageUrls) {
    }

    private String normalizeAnswer(String answer) {
        return StringUtils.hasText(answer) ? answer.trim() : DEFAULT_ANSWER;
    }
}
