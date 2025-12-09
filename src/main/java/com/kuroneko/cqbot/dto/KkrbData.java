package com.kuroneko.cqbot.dto;

import lombok.Data;

import java.util.List;

@Data
public class KkrbData<T> {
    private int code;
    private String msg;
    private T data;

    @Data
    public static class OVData {
        private BdData bdData;
        private SpData spData;
        private SphData sphData;
        private List<AriiItem> ariiData;
        private List<BcicItem> bcicData;
    }

    @Data
    public static class BdData {
        private BdItem db;
        private BdItem cgxg;
        private BdItem bks;
        private BdItem htjd;
        private BdItem cxjy;
    }

    @Data
    public static class BdItem {
        private String password;
        private String updated;
        private boolean overridden;
    }

    @Data
    public static class SpData {
        private SpItem tech;
        private SpItem workbench;
        private SpItem pharmacy;
        private SpItem armory;
    }

    @Data
    public static class SphData {
        private SphItem tech;
        private SphItem workbench;
        private SphItem pharmacy;
        private SphItem armory;
    }

    @Data
    public static class SpItem {
        private String itemName;
        private long itemID;
        private int itemGrade;
        private List<ItemForge> itemForge;
        private String pic;
        private String place;
        private String placeName;
        private List<Material> totalMaterialLists;
        private double totalMaterialValue;
        private int perCount;
        private double singlePrice;
        private double profit;
        private double yesterdayHighestPrice;
        private double yesterdayHighestProfit;
        private double yesterdayTotalMaterialValue;
        private String yesterdayHighestTime;
    }

    @Data
    public static class SphItem {
        private String itemName;
        private long itemID;
        private int itemGrade;
        private String pic;
        private String place;
        private String placeName;
        private int requiredLevel;
        private String productionTime;
        private List<Material> totalMaterialLists;
        private double totalMaterialValue;
        private int perCount;
        private double singlePrice;
        private double profit;
        private double yesterdayHighestPrice;
        private double yesterdayHighestProfit;
        private double yesterdayTotalMaterialValue;
        private String yesterdayHighestTime;
    }

    @Data
    public static class ItemForge {
        private int requiredLevel;
        private String productionTime;
        private double hourlyProfit;
    }

    @Data
    public static class Material {
        private String itemName;
        private long itemID;
        private String pic;
        private int count;
        private double perPrice;
        private double totalPrice;
    }

    @Data
    public static class AriiItem {
        private long activityID;
        private String activityName;
        private String activityType;
        private String activityTime;
        private long itemID;
        private String itemName;
        private String pic;
        private int itemGrade;
        private double currectPrice;
        private double activitySuggestedPrice;
    }

    @Data
    public static class BcicItem {
        private String name;
        private String pic;
        private String energy;
    }
}