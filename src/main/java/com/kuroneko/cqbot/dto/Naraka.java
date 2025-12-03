package com.kuroneko.cqbot.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Data;

public class Naraka {
    @Data
    public static class Career {
        @JsonProperty("_id")
        private String _id;

        @JsonProperty("role_id")
        private String roleId;

        @JsonProperty("role_name")
        private String roleName;

        @JsonProperty("server_id")
        private int serverId;

        @JsonProperty("server_name")
        private String serverName;

        @JsonProperty("season_id")
        private int seasonId;

        @JsonProperty("role_level")
        private int roleLevel;

        @JsonProperty("head_icon")
        private int headIcon;

        @JsonProperty("head_icon_url")
        private String headIconUrl;

        @JsonProperty("game_time")
        private int gameTime;

        @JsonProperty("total_time")
        private int totalTime;

        @JsonProperty("game_mode")
        private int gameMode;

        @JsonProperty("grade_id")
        private int gradeId;

        @JsonProperty("grade_name")
        private String gradeName;

        @JsonProperty("grade_icon_url")
        private String gradeIconUrl;

        @JsonProperty("rank_score")
        private int rankScore;

        @JsonProperty("round")
        private int round;

        @JsonProperty("win")
        private int win;

        @JsonProperty("top_five")
        private int topFive;

        @JsonProperty("survive_time")
        private int surviveTime;

        @JsonProperty("kill")
        private int kill;

        @JsonProperty("damage")
        private int damage;

        @JsonProperty("cure")
        private int cure;

        @JsonProperty("rescue")
        private int rescue;

        @JsonProperty("dead")
        private int dead;

        @JsonProperty("max_kill")
        private int maxKill;

        @JsonProperty("max_damage")
        private int maxDamage;

        @JsonProperty("max_cure")
        private int maxCure;

        @JsonProperty("max_survive_time")
        private int maxSurviveTime;

        @JsonProperty("max_rescue")
        private int maxRescue;

        @JsonProperty("update_time")
        private String updateTime;
    }
}