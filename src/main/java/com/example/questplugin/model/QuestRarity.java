package com.example.questplugin.model;

public enum QuestRarity {
    COMMON("§7", "Common", 1.0),
    RARE("§a", "Rare", 1.25),
    EPIC("§d", "Epic", 1.5),
    LEGENDARY("§6", "Legendary", 2.0);

    private final String color;
    private final String name;
    private final double rewardMultiplier;

    QuestRarity(String color, String name, double rewardMultiplier) {
        this.color = color;
        this.name = name;
        this.rewardMultiplier = rewardMultiplier;
    }

    public String getColor() {
        return color;
    }

    public String getDisplayName() {
        return color + "§l" + name;
    }

    public double getMultiplier() {
        return rewardMultiplier;
    }

    public static QuestRarity fromString(String input) {
        for (QuestRarity r : values()) {
            if (r.name().equalsIgnoreCase(input)) return r;
        }
        return COMMON;
    }
}
