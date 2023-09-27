package com.kuroneko.cqbot.vo;


import lombok.Data;

import java.time.Instant;

@Data
public class UpdateCache {
    private Long groupId;
    private Long userID;
    private long startTime;

    public UpdateCache(Long groupId, Long userID) {
        this.groupId = groupId;
        this.userID = userID;
        this.startTime = Instant.now().getEpochSecond();
    }
}
