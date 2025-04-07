/**
 * Represents the rarity levels of quests in the game.
 */
package com.example.questplugin.model;

public enum QuestRarity {
    /**
     * The common rarity level.
     */
    COMMON("§7", "Common", 1.0),

    /**
     * The rare rarity level.
     */
    RARE("§a", "Rare", 1.25),

    /**
     * The epic rarity level.
     */
    EPIC("§d", "Epic", 1.5),

    /**
     * The legendary rarity level.
     */
    LEGENDARY("§6", "Legendary", 2.0);

    private final String color;
    private final String name;
    private final double rewardMultiplier;

    /**
     * Constructs a new QuestRarity with the specified properties.
     *
     * @param color             the color code for the rarity
     * @param name              the display name of the rarity
     * @param rewardMultiplier  the multiplier for rewards associated with this rarity
     */
    QuestRarity(String color, String name, double rewardMultiplier) {
        this.color = color;
        this.name = name;
        this.rewardMultiplier = rewardMultiplier;
    }

    /**
     * Gets the color code of the rarity.
     *
     * @return the color code
     */
    public String getColor() {
        return color;
    }

    /**
     * Gets the display name of the rarity.
     *
     * @return the display name
     */
    public String getDisplayName() {
        return color + "§l" + name;
    }

    /**
     * Gets the reward multiplier for this rarity.
     *
     * @return the reward multiplier
     */
    public double getMultiplier() {
        return rewardMultiplier;
    }

    /**
     * Parses a string input to return the corresponding QuestRarity.
     *
     * @param input the input string to parse
     * @return the parsed QuestRarity, or COMMON if not found
     */
    public static QuestRarity fromString(String input) {
        for (QuestRarity r : values()) {
            if (r.name().equalsIgnoreCase(input)) return r;
        }
        return COMMON;
    }
}
