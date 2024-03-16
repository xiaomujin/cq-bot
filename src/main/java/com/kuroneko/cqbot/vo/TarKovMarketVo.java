package com.kuroneko.cqbot.vo;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TarKovMarketVo {
    private String uid;
    private String enImg;
//    private String enName;
//    private List<String> tags;
    private String url;
    private double change24;
    private double change7d;
    private int price;
    private Instant updated;
    private int size;
    private int avgDayPrice;
    private int avgWeekPrice;
//    private String wikiName;
//    private String wikiUrl;
    private String wikiIcon;
    private String wikiImg;
    private Instant priceUpdated;
    private String shortName;
    private String traderName;
    private int traderPrice;
//    private String ruImg;
//    private String ruName;
    private String bsgId;
    private String cnName;
    private String cnShortName;
//    private String ruShortName;
//    private String deName;
//    private String deShortName;
//    private String frName;
//    private String frShortName;
//    private String esName;
//    private String esShortName;
    private String traderPriceCur;
    private int basePrice;
//    private String czName;
//    private String czShortName;
//    private String huName;
//    private String huShortName;
//    private String trName;
//    private String trShortName;
    private String grid;
    private boolean canSellOnFlea;
    private String name;
    private String search;
    private int pricePerSlot;
    private int avgDayPricePerSlot;
    private int avgWeekPricePerSlot;
//    private boolean updatedLongTimeAgo;
//    private boolean haveMarketData;
//    private int traderPriceRub;
//    private List<ItemPrices> buyPrices;
//    private List<ItemPrices> sellPrices;
//    private int fee;
//    private int trader_bb_profit;
//    private String cat;
//    private String gridClass;
//    private String gridSize;
//    private int gridSort;
//    private List<String> quests;
//    private boolean fir;
//    private List<String> hideoutModules;
//    private List<String> crafts;
//    private List<String> barters;
//    private int questsLength;
//    private int hideoutModulesLength;
//    private int craftsLength;
//    private int bartersLength;
}
