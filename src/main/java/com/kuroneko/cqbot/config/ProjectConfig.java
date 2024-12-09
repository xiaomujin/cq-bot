package com.kuroneko.cqbot.config;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;

@Component
public class ProjectConfig {
    public static ServerProperties serverProperties;
    public static Integer port;
    public static String url;
    public static final String USER_AGENT = "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/116.0.0.0 Safari/537.36";

    private ProjectConfig(ServerProperties serverProperties) {
        ProjectConfig.serverProperties = serverProperties;
        ProjectConfig.port = ProjectConfig.serverProperties.getPort();
        ProjectConfig.url = "http://localhost:" + ProjectConfig.port + "/";
    }
}
