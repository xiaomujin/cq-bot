package com.kuroneko.cqbot.core.dto;

import lombok.Builder;
import lombok.Data;

import java.util.List;
import java.util.Set;

@Data
@Builder
public class MsgInfo {
    private boolean isAt;
    private String msg;
    private List<String> imgList;
    private Set<Long> atList;
}
