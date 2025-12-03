package com.kuroneko.cqbot.config;

import org.springframework.boot.autoconfigure.AutoConfigureBefore;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import tools.jackson.databind.DeserializationFeature;
import tools.jackson.databind.json.JsonMapper;

import java.text.SimpleDateFormat;
import java.util.TimeZone;

@Configuration
@AutoConfigureBefore(JacksonAutoConfiguration.class)
public class JacksonConfig {
    public static final String DATE_TIME_FORMAT = "yyyy-MM-dd HH:mm:ss";

    @Bean
    JsonMapper jacksonJsonMapper(JsonMapper.Builder builder) {
        return builder
                .defaultDateFormat(new SimpleDateFormat(DATE_TIME_FORMAT))
                .defaultTimeZone(TimeZone.getDefault())
                .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false)
//                .changeDefaultVisibility((v) -> v.withGetterVisibility(JsonAutoDetect.Visibility.NONE))
                .build();
    }
}