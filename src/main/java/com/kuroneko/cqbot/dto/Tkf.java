package com.kuroneko.cqbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

import java.util.List;

public class Tkf {
    @Data
    public static class Map {
        @JsonProperty("name")
        private String name;

        @JsonProperty("bosses")
        private List<Boss> bosses;
    }

    @Data
    public static class Boss {
        @JsonProperty("spawnChance")
        private Double spawnChance;

        @JsonProperty("boss")
        private BossDetails boss;
    }

    @Data
    public static class BossDetails {
        @JsonProperty("name")
        private String name;
    }
}