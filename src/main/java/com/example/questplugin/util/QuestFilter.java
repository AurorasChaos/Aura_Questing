package com.example.questplugin.util;

import com.example.questplugin.model.Quest;

import java.util.List;

public enum QuestFilter {
    ALL("All"),
    COMPLETED("Completed"),
    UNCLAIMED("Unclaimed");

    private final String label;

    QuestFilter(String label) {
        this.label = label;
    }

    public <T extends Quest> List<T> apply(List<T> quests) {
        return switch (this) {
            case COMPLETED -> quests.stream().filter(Quest::isCompleted).toList();
            case UNCLAIMED -> quests.stream().filter(q -> q.isCompleted() && !q.isRewardClaimed()).toList();
            case ALL -> quests;
        };
    }

    public String label() {
        return label;
    }

    public QuestFilter next() {
        return switch (this) {
            case ALL -> COMPLETED;
            case COMPLETED -> UNCLAIMED;
            case UNCLAIMED -> ALL;
        };
    }
}
