package com.example.questplugin.model;

/**
 * Represents the tier of a quest.
 */
public enum QuestTier {
    /**
     * Daily quests that can be completed every day.
     */
    DAILY,

    /**
     * Weekly quests that can be completed once per week.
     */
    WEEKLY,

    /**
     * Global quests that are available to players at any time.
     */
    GLOBAL,
    /**
     * All quests, including daily, weekly, and global quests.
     */
    ALL
}

