package com.example.questplugin.model;

/**
 * Represents the type of objective in a quest.
 */
public enum QuestType {
    /**
     * Objective where the player needs to kill mobs.
     */
    KILL_MOB,

    /**
     * Objective where the player needs to gather items.
     */
    GATHER_ITEM,

    /**
     * Objective where the player needs to craft specific items.
     */
    CRAFT_ITEM,

    /**
     * Objective where the player needs to walk a certain distance.
     */
    WALK_DISTANCE,

    /**
     * Objective where the player needs to explore specific biomes.
     */
    EXPLORE_BIOME,

    /**
     * Objective where the player needs to consume specific items.
     */
    CONSUME_ITEM,

    /**
     * Objective where the player needs to gain skill experience points.
     */
    GAIN_SKILL_EXP,

    /**
     * Objective where the player needs to gain skill levels.
     */
    GAIN_SKILL_LEVEL,

    /**
     * Objective where the player needs to fish for specific items.
     */
    FISH,

    /**
     * Objective where the player needs to place specific blocks.
     */
    PLACE_BLOCK,

    /**
     * Objective where the player needs to mine specific blocks.
     */
    MINE_BLOCK,

    /**
     * Objective where the player needs to trade with NPCs.
     */
    TRADE,

    /**
     * Objective where the player needs to breed animals.
     */
    BREED_ANIMAL,

    /**
     * Objective where the player needs to smelt specific items.
     */
    SMELT_ITEM,

    /**
     * Objective where the player needs to tame specific entities.
     */
    TAME_ENTITY,

    /**
     * Objective where the player needs to brew potions.
     */
    BREW_ITEM,

    /**
     * Objective where the player needs to enchant items.
     */
    ENCHANT_ITEM,

    /**
     * Custom objective type that can be defined by the plugin developer.
     */
    CUSTOM
}

