package com.kuroneko.cqbot.dto;

import com.mikuac.shiro.core.Bot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchMode {
    private Long userId;
    private Long groupId;
    private String mode;
    private Bot bot;
    private String ext;

    public Long getGroupId() {
        return groupId == null ? 0L : groupId;
    }
}
