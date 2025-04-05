package com.example.questplugin.managers;

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.model.QuestRarity;
import com.example.questplugin.model.QuestTemplate;
import com.example.questplugin.model.QuestTier;
import com.example.questplugin.model.Quest;

import java.util.*; import java.util.stream.Collectors;

public class QuestAssigner {

private final QuestPlugin plugin;
private final Random random = new Random();

public QuestAssigner(QuestPlugin plugin) {
    this.plugin = plugin;
}

public List<Quest> getRandomQuests(UUID playerId, QuestTier tier, int count) {
    List<QuestTemplate> allTemplates = plugin.getQuestLoader().getTemplatesByTier(tier);

    // Shuffle and pick randomly
    Collections.shuffle(allTemplates, random);

    // Avoid duplicates by using quest ID set
    Set<String> assignedIds = plugin.getQuestManager().getAllPlayerQuests(playerId).stream()
            .map(Quest::getId)
            .collect(Collectors.toSet());

    List<Quest> selected = new ArrayList<>();
    for (QuestTemplate template : allTemplates) {
        if (!assignedIds.contains(template.getId())) {
            selected.add(template.toQuest());
        }
        if (selected.size() >= count) break;
    }

    return selected;
}

public List<Quest> getRandomQuestsWeighted(UUID playerId, QuestTier tier, int count) {
    List<QuestTemplate> templates = plugin.getQuestLoader().getTemplatesByTier(tier);
    Set<String> assignedIds = plugin.getQuestManager().getAllPlayerQuests(playerId).stream()
            .map(Quest::getId)
            .collect(Collectors.toSet());

    // Build a weighted list based on rarity
    List<QuestTemplate> weightedList = new ArrayList<>();
    for (QuestTemplate template : templates) {
        if (assignedIds.contains(template.getId())) continue;
        int weight = getWeight(template.getQuestRarity());
        for (int i = 0; i < weight; i++) {
            weightedList.add(template);
        }
    }

    Collections.shuffle(weightedList, random);

    List<Quest> result = new ArrayList<>();
    for (QuestTemplate template : weightedList) {
        if (result.stream().noneMatch(q -> q.getId().equals(template.getId()))) {
            result.add(template.toQuest());
        }
        if (result.size() >= count) break;
    }

    return result;
}

private int getWeight(QuestRarity rarity) {
    return switch (rarity) {
        case COMMON -> 10;
        case RARE -> 5;
        case EPIC -> 2;
        case LEGENDARY -> 1;
    };
}

public List<Quest> assignDailyQuests(UUID uuid) {
    List<QuestTemplate> pool = new ArrayList<>(plugin.getQuestLoader().getTemplatesByTier(QuestTier.DAILY));
    int dailyLimit = plugin.getConfig().getInt("QuestLimits.DAILY", 10);
    List<QuestTemplate> shuffled = new ArrayList<>(pool);
    Collections.shuffle(shuffled);
    return shuffled.stream()
        .limit(dailyLimit)
        .map(QuestTemplate::toQuest)
        .collect(Collectors.toList());
}

public List<Quest> assignWeeklyQuests(UUID uuid) {
    List<QuestTemplate> pool = new ArrayList<>(plugin.getQuestLoader().getTemplatesByTier(QuestTier.WEEKLY));
    int weeklyLimit = plugin.getConfig().getInt("QuestLimits.WEEKLY", 7);
    List<QuestTemplate> shuffled = new ArrayList<>(pool);
    Collections.shuffle(shuffled);
    return shuffled.stream()
        .limit(weeklyLimit)
        .map(QuestTemplate::toQuest)
        .collect(Collectors.toList());
}

public List<Quest> assignGlobalQuests() {
    List<QuestTemplate> pool = new ArrayList<>(plugin.getQuestLoader().getTemplatesByTier(QuestTier.GLOBAL));
    int globalLimit = plugin.getConfig().getInt("QuestLimits.GLOBAL", 5);
    List<QuestTemplate> shuffled = new ArrayList<>(pool);
    Collections.shuffle(shuffled);
    return shuffled.stream()
        .limit(globalLimit)
        .map(QuestTemplate::toQuest)
        .collect(Collectors.toList());
}

}