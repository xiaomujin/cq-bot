package com.kuroneko.cqbot.config.localCfg;

import com.kuroneko.cqbot.core.cfg.ConfigSave;
import com.kuroneko.cqbot.core.process.CommonProcessor;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.io.Serializable;
import java.time.Instant;

@Data
@EqualsAndHashCode(callSuper = true)
public class UpdateCfg extends ConfigSave implements Serializable {
    private Long groupId;
    private Long userID;
    private long startTime;
    private boolean isRead;

    public void update(Long groupId, Long userID, boolean isRead) {
        CommonProcessor.getInstance().execute(() -> {
            this.groupId = groupId;
            this.startTime = Instant.now().getEpochSecond();
            this.userID = userID;
            this.isRead = isRead;
            save();
        });
    }

    public void read(boolean isRead) {
        CommonProcessor.getInstance().execute(() -> {
            this.isRead = isRead;
            save();
        });
    }
}
