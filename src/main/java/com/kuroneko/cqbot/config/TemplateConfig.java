package com.kuroneko.cqbot.config;

import com.kuroneko.cqbot.core.cfg.ConfigManager;
import com.kuroneko.cqbot.service.aiTool.AiHelpTool;
import com.kuroneko.cqbot.service.aiTool.AiTimeTool;
import com.kuroneko.cqbot.service.aiTool.AiWebSearchTool;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.ChatMemoryRepository;
import org.springframework.ai.chat.memory.InMemoryChatMemoryRepository;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.openai.OpenAiChatModel;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.client.ClientHttpRequestFactory;
import org.springframework.http.client.ClientHttpResponse;
import org.springframework.web.client.ResponseErrorHandler;
import org.springframework.web.client.RestTemplate;

import java.io.IOException;
import java.net.URI;

@Configuration
public class TemplateConfig {

    @Bean
    public ClientHttpRequestFactory simpleClientHttpRequestFactory() {
        SSLConfig sslConfig = new SSLConfig();
        sslConfig.setConnectTimeout(90 * 1000);
        sslConfig.setReadTimeout(90 * 1000);
        return sslConfig;
    }

    @Bean
    public RestTemplate getRestTemplate(ClientHttpRequestFactory factory) {
        RestTemplate restTemplate = new RestTemplate(factory);
        // List<HttpMessageConverter<?>> messageConverters = restTemplate.getMessageConverters();
        restTemplate.setErrorHandler(new ResponseErrorHandler() {
            @Override
            public boolean hasError(ClientHttpResponse response) throws IOException {
                return false;
            }

            @Override
            public void handleError(URI url, HttpMethod method, ClientHttpResponse response) throws IOException {

            }
        });
        return restTemplate;
    }

    @Bean
    public ChatMemory chatMemory(ObjectProvider<ChatMemoryRepository> chatMemoryRepositoryProvider,
                                 @Value("${bot.ai.max-history-pairs:8}") int maxHistoryPairs) {
        ChatMemoryRepository chatMemoryRepository = chatMemoryRepositoryProvider
                .getIfAvailable(InMemoryChatMemoryRepository::new);
        return MessageWindowChatMemory.builder()
                .chatMemoryRepository(chatMemoryRepository)
                .maxMessages(Math.max(1, maxHistoryPairs) * 2)
                .build();
    }

    @Bean
    @ConditionalOnProperty(prefix = "bot.ai", name = "enabled", havingValue = "true")
    public ChatClient aiChatClient(OpenAiChatModel chatModel,
                                   ChatMemory chatMemory,
                                   AiWebSearchTool aiWebSearchTool,
                                   AiTimeTool aiTimeTool,
                                   AiHelpTool aiHelpTool) {
        String systemPrompt = ConfigManager.ins.getAdminCfg().getSystemPrompt();
        return ChatClient.builder(chatModel)
                .defaultSystem(systemPrompt)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultTools(aiWebSearchTool, aiTimeTool, aiHelpTool)
                .build();
    }

//    @Bean
//    public RedisTemplate<String, Object> redisTemplate(RedisConnectionFactory redisConnectionFactory) {
//        RedisTemplate<String, Object> redisTemplate = new RedisTemplate<>();
//        redisTemplate.setConnectionFactory(redisConnectionFactory);
//        StringRedisSerializer stringRedisSerializer = new StringRedisSerializer();
//        redisTemplate.setKeySerializer(stringRedisSerializer);
//        redisTemplate.setHashKeySerializer(stringRedisSerializer);
//        FastJsonRedisSerializer<Object> fastJsonRedisSerializer = new FastJsonRedisSerializer<>(Object.class);
//        redisTemplate.setValueSerializer(fastJsonRedisSerializer);
//        redisTemplate.setHashValueSerializer(fastJsonRedisSerializer);
//        redisTemplate.afterPropertiesSet();
//        return redisTemplate;
//    }
}
