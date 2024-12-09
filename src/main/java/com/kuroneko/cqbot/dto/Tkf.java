package com.kuroneko.cqbot.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

import java.util.List;

public class Tkf {
    @Data
    public static class Map {
        @JSONField(name = "name")
        private String name;

        @JSONField(name = "bosses")
        private List<Boss> bosses;
    }

    @Data
    public static class Boss {
        @JSONField(name = "spawnChance")
        private Double spawnChance;

        @JSONField(name = "boss")
        private BossDetails boss;
    }

    @Data
    public static class BossDetails {
        @JSONField(name = "name")
        private String name;
    }
}
