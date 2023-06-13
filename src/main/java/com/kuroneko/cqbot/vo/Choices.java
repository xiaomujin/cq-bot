package com.kuroneko.cqbot.vo;

import lombok.Data;

@Data
public class Choices {
    private String text;
    private int index;
    private String logprobs;
    private String finish_reason;
}
