package com.kuroneko.cqbot.vo;

import lombok.Data;

import java.util.List;

@Data
public class AIRsp {
    private String id;
    private String object;
    private long created;
    private String model;
    private List<Choices> choices;
    private Usage usage;
}
