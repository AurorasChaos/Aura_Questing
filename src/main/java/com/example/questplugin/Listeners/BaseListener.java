package com.example.questplugin.Listeners;

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.managers.QuestManager;
import com.example.questplugin.managers.RewardHandler;
import com.example.questplugin.model.Quest;
import com.example.questplugin.model.QuestTemplate;
import com.example.questplugin.model.QuestType;

import org.bukkit.entity.Player;


public abstract class BaseListener {

    protected final QuestPlugin plugin;
    protected final RewardHandler rewardHandler;
    protected final QuestManager questManager;

    public BaseListener(QuestPlugin plugin) {
        this.plugin = plugin;
        this.rewardHandler = plugin.getRewardHandler();
        this.questManager = plugin.getQuestManager();
    }

    /**
     * Increment progress and notify the player of quest completion.
     *
     * @param player The player who completed the task.
     * @param obj  The objective to update.
     */
    protected void incrementProgressAndNotify(Player player, QuestTemplate.Objective obj, Quest quest) {
        obj.incrementProgress(1);
        if (obj.getProgress() >= obj.getTargetAmount()){
            quest.incrementProgress(1);
        }
        plugin.getQuestNotifier().notifyProgress(player, obj, quest);
        rewardHandler.giveReward(player, quest, false);
    }

    /**
     * Handle quests of a specific type and target.
     *
     * @param type   The type of quest to handle.
     * @param target The target value for the quest (e.g., mob type, block type).
     * @param player The player who performed the action.
     */
    protected void handleQuestTypeAndTarget(QuestType type, String target, Player player) {
        // Handle player quests
        for (Quest quest : questManager.getPlayerQuests(player.getUniqueId())){
            if (!quest.isCompleted()){
                quest.getQuestObjectives().forEach(obj -> {
                    if (obj.getType() == type && obj.getTargetKey().equalsIgnoreCase(target)){
                        incrementProgressAndNotify(player, obj, quest);
                        plugin.debug("[" + type.toString() + "] Updated progress for " + quest.getTier() +  " quest " + quest.getId() + ": " + obj.getProgress());
                    }
                });
            }
        }
        // Handle global quests
        for (Quest quest : questManager.getGlobalQuests()){
            if (!quest.isCompleted()){
                quest.getQuestObjectives().forEach(obj -> {
                    if (obj.getType() == type && obj.getTargetKey().equalsIgnoreCase(target)){
                        incrementProgressAndNotify(player, obj, quest);
                        plugin.debug("[" + type.toString() + "] Updated progress for global quest " + quest.getId() + ": " + obj.getProgress());
                    }
                });
            }
        }
    }

    /**
     * Get the event type for logging and debugging.
     *
     * @return The event type as a string.
     */
    protected abstract String getEventType();
}
