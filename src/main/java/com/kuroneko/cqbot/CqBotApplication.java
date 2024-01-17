package com.kuroneko.cqbot;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CqBotApplication {

    public static void main(String[] args) {
        System.setProperty("sun.net.http.allowRestrictedHeaders", "true");
//        System.setProperty("jdk.httpclient.HttpClient.log", "all");
        SpringApplication.run(CqBotApplication.class, args);
    }

}
