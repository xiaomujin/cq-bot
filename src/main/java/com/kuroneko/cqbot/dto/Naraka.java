package com.kuroneko.cqbot.dto;

import com.alibaba.fastjson2.annotation.JSONField;
import lombok.Data;

public class Naraka {
    @Data
    public static class Career {
        @JSONField(name = "_id")
        private String _id;

        @JSONField(name = "role_id")
        private String roleId;

        @JSONField(name = "role_name")
        private String roleName;

        @JSONField(name = "server_id")
        private int serverId;

        @JSONField(name = "server_name")
        private String serverName;

        @JSONField(name = "season_id")
        private int seasonId;

        @JSONField(name = "role_level")
        private int roleLevel;

        @JSONField(name = "head_icon")
        private int headIcon;

        @JSONField(name = "head_icon_url")
        private String headIconUrl;

        @JSONField(name = "game_time")
        private int gameTime;

        @JSONField(name = "total_time")
        private int totalTime;

        @JSONField(name = "game_mode")
        private int gameMode;

        @JSONField(name = "grade_id")
        private int gradeId;

        @JSONField(name = "grade_name")
        private String gradeName;

        @JSONField(name = "grade_icon_url")
        private String gradeIconUrl;

        @JSONField(name = "rank_score")
        private int rankScore;

        @JSONField(name = "round")
        private int round;

        @JSONField(name = "win")
        private int win;

        @JSONField(name = "top_five")
        private int topFive;

        @JSONField(name = "survive_time")
        private int surviveTime;

        @JSONField(name = "kill")
        private int kill;

        @JSONField(name = "damage")
        private int damage;

        @JSONField(name = "cure")
        private int cure;

        @JSONField(name = "rescue")
        private int rescue;

        @JSONField(name = "dead")
        private int dead;

        @JSONField(name = "max_kill")
        private int maxKill;

        @JSONField(name = "max_damage")
        private int maxDamage;

        @JSONField(name = "max_cure")
        private int maxCure;

        @JSONField(name = "max_survive_time")
        private int maxSurviveTime;

        @JSONField(name = "max_rescue")
        private int maxRescue;

        @JSONField(name = "update_time")
        private String updateTime;
    }
}
