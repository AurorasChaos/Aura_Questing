package com.example.questplugin.model;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Represents a quest that can be completed by a player.
 */
public class Quest {

    /**
     * The unique identifier for the quest.
     */
    private final String id;

    /**
     * A description of the quest.
     */
    private final String description;

    /**
     * The currency reward the player will receive upon completion of the quest.
     */
    private final double currencyReward;

    /**
     * The tier of the quest, indicating its difficulty or level.
     */
    private final QuestTier tier;

    /**
     * The rarity of the quest, indicating its uniqueness or desirability.
     */
    private final QuestRarity rarity;

    /**
     * The target amount of objectives that must be completed to claim the reward.
     */
    private final int targetAmount;

    /**
     * The UUID of the player who is currently working on this quest.
     */
    private final UUID playerUUID;

    /**
     * A list of objectives that the player must complete to finish the quest.
     */
    private final List<QuestTemplate.Objective> objectives = new ArrayList<>();

    /**
     * The current progress of the quest in completing objectives.
     */
    private int progress = 0;

    /**
     * Indicates whether the reward for this quest has already been claimed.
     */
    private boolean rewardClaimed = false;

    /**
     * The type of skill required to complete the quest.
     */
    private final String skillType;

    /**
     * The amount of XP awarded upon completion of the quest.
     */
    private final int skillXp;

    /**
     * Creates a new Quest instance based on a template and a player's UUID.
     *
     * @param template   the template defining the quest's details
     * @param playerUUID the UUID of the player assigned to this quest
     */
    public Quest(QuestTemplate template, UUID playerUUID) {
        this.playerUUID = playerUUID;
        for (QuestTemplate.Objective obj : template.getObjectives()) {
            objectives.add(new QuestTemplate.Objective(obj.getType(), obj.getTargetKey(), obj.getTargetAmount(), obj.getDescription()));
        }
        this.targetAmount = objectives.size();
        this.id = template.getId();
        this.description = template.getDescription();
        this.currencyReward = template.getCurrencyReward();
        this.skillType = template.getSkillType();
        this.skillXp = template.getSkillXp();
        this.tier = template.getTier();
        this.rarity = template.getRarity();
    }
    
    /**
     * Returns the unique identifier for the quest.
     *
     * @return the quest's ID
     */
    public String getId() {
        return id;
    }

    /**
     * Returns a description of the quest.
     *
     * @return the quest's description
     */
    public String getDescription() {
        return description;
    }

    /**
     * Returns the current progress of completing objectives in the quest.
     *
     * @return the current progress
     */
    public int getCurrentProgress() {
        return progress;
    }

    /**
     * Checks if the quest has been completed based on the objective progress.
     *
     * @return true if the quest is completed, false otherwise
     */
    public boolean isCompleted() {
        return progress >= targetAmount;
    }

    /**
     * Checks if the reward for this quest has already been claimed.
     *
     * @return true if the reward is claimed, false otherwise
     */
    public boolean isRewardClaimed() {
        return rewardClaimed;
    }

    /**
     * Returns the currency reward the player will receive upon completion of the quest.
     *
     * @return the currency reward amount
     */
    public double getCurrencyReward() {
        return currencyReward;
    }

    /**
     * Returns the tier of the quest, indicating its difficulty or level.
     *
     * @return the quest's tier
     */
    public QuestTier getTier() {
        return tier;
    }

    /**
     * Returns the rarity of the quest, indicating its uniqueness or desirability.
     *
     * @return the quest's rarity
     */
    public QuestRarity getRarity() {
        return rarity;
    }

    /**
     * Returns the type of skill required to complete the quest.
     *
     * @return the skill type
     */
    public String getSkillType() {
        return skillType;
    }

    /**
     * Returns the target amount of objectives that must be completed to claim the reward.
     *
     * @return the target amount of objectives
     */
    public int getTargetAmount() {
        return targetAmount;
    }

    /**
     * Returns a list of objectives that the player must complete to finish the quest.
     *
     * @return the list of objectives
     */
    public List<QuestTemplate.Objective> getQuestObjectives() {
        return objectives;
    }

    /**
     * Returns the amount of XP awarded upon completion of the quest.
     *
     * @return the skill XP amount
     */
    public int getSkillXp() {
        return skillXp;
    }

    /**
     * Returns the UUID of the player who is currently working on this quest.
     *
     * @return the player's UUID
     */
    public UUID getPlayerUUID() {
        return playerUUID;
    }
    /**
     * Increments the progress of completing objectives in the quest by a specified amount.
     *
     * @param amount the amount to increment the progress by
     */
    public void incrementProgress(int amount) {
        if (!isCompleted()) {
            this.progress += amount;
        }
    }

    /**
     * Sets the current progress of completing objectives in the quest.
     *
     * @param amount the new progress amount
     */
    public void setCurrentProgress(int amount) {
        this.progress = amount;
    }

    /**
     * Claims the reward for this quest, indicating that it has been completed and the reward is claimed.
     */
    public void claimReward() {
        this.rewardClaimed = true;
    }

    /**
     * Sets the reward claim status for this quest to true.
     */
    public void setRewardClaimed() {
        rewardClaimed = true;
    }

    /**
     * Checks if the player can claim the reward for this quest based on objective progress and completion status.
     *
     * @return true if the reward can be claimed, false otherwise
     */
    public boolean canClaim() {
        for (QuestTemplate.Objective objective : objectives) {
            if (objective.getProgress() < objective.getTargetAmount()) {
                return false;
            }
        }
        return isCompleted() && !isRewardClaimed();
    }
}
