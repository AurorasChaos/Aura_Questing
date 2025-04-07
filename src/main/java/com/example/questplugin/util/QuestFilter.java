/**
 * Represents different filters that can be applied to quests.
 */
package com.example.questplugin.util;

import com.example.questplugin.model.Quest;

import java.util.List;

/**
 * The {@code QuestFilter} enum defines different filters that can be applied to quests. Each filter has a label for display purposes and
 * a method to apply the filter to a list of quests.
 */
public enum QuestFilter {

    /**
     * Represents all quests, regardless of their completion status or reward claim status.
     */
    ALL("All"),

    /**
     * Represents completed quests. A completed quest is one where the player has fulfilled the required conditions but may not have claimed
     * its reward yet.
     */
    COMPLETED("Completed"),

    /**
     * Represents unclaimed quests. An unclaimed quest is one that has been completed by the player, but whose reward has not been claimed yet.
     */
    UNCLAIMED("Unclaimed");

    private final String label;

    QuestFilter(String label) {
        this.label = label;
    }

    /**
     * Applies the filter to a list of quests and returns a new filtered list.
     *
     * @param quests The list of quests to apply the filter to. This can contain any subclass of {@link Quest}.
     * @return A new list containing only the quests that match the filter criteria, or an empty list if no quests match.
     */
    public <T extends Quest> List<T> apply(List<T> quests) {
        return switch (this) {
            case COMPLETED -> quests.stream().filter(Quest::isCompleted).toList();
            case UNCLAIMED -> quests.stream().filter(q -> q.isCompleted() && !q.isRewardClaimed()).toList();
            case ALL -> quests;
        };
    }

    /**
     * Gets the display label for this filter.
     *
     * @return The display label for this filter.
     */
    public String label() {
        return label;
    }

    /**
     * Gets the next filter after this one in the cycle {@link QuestFilter#ALL}, {@link QuestFilter#COMPLETED}, and {@link QuestFilter#UNCLAIMED}.
     *
     * @return The next filter in the cycle.
     */
    public QuestFilter next() {
        return switch (this) {
            case ALL -> COMPLETED;
            case COMPLETED -> UNCLAIMED;
            case UNCLAIMED -> ALL;
        };
    }
}

