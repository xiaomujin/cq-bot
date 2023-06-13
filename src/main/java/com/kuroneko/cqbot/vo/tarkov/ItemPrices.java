package com.kuroneko.cqbot.vo.tarkov;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ItemPrices {
    private String type;
    private int price;
    private int fee;
    private String field;
    private String trader;
    private String cur;
    private int level;
    private Instant priceUpdated;
}
