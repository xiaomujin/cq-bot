package com.kuroneko.cqbot.config;

import org.springframework.boot.autoconfigure.web.ServerProperties;
import org.springframework.stereotype.Component;

@Component
public class ProjectConfig {
    public static ServerProperties serverProperties;
    public static Integer port;
    public static String url;

    private ProjectConfig(ServerProperties serverProperties) {
        ProjectConfig.serverProperties = serverProperties;
        ProjectConfig.port = ProjectConfig.serverProperties.getPort();
        ProjectConfig.url = STR."http://localhost:\{ProjectConfig.port}/";
    }
}