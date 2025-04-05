package com.example.questplugin.model;

import com.example.questplugin.util.EntityCategoryMatcher;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class Quest {

    private final String id;
    private final String description;
    private final double currencyReward;
    private final QuestTier tier;
    private final QuestRarity rarity;
    private final int targetAmount;

    private final UUID playerUUID;
    private final List<QuestTemplate.Objective> objectives = new ArrayList<>();

    private int progress = 0;
    private boolean rewardClaimed = false;

    private final String skillType;
    private final int skillXp;

    public Quest(QuestTemplate template, UUID playerUUID) {
        this.playerUUID = playerUUID;
        for (QuestTemplate.Objective obj : template.getObjectives()){
            objectives.add(new QuestTemplate.Objective(obj.getObjectiveType(), obj.getObjectiveTargetKey(), obj.getObjectiveTargetAmount()));
        }
        this.targetAmount = objectives.size();
        this.id = template.getId();
        this.description = template.getDescription();
        this.currencyReward = template.getCurrenyReward();
        this.skillType = template.getSkillType();
        this.skillXp = template.getSkillXp();
        this.tier = template.getQuestTier();
        this.rarity = template.getQuestRarity();
    }
    
    public String getId() { return id; }
    public String getDescription() { return description; }
    public int getCurrentProgress() { return progress; }
    public boolean isCompleted() { return progress >= targetAmount; }
    public boolean isRewardClaimed() { return rewardClaimed; }
    public double getCurrencyReward() { return currencyReward; }
    public QuestTier getTier() { return tier; }
    public QuestRarity getRarity() { return rarity; }
    public String getSkillType() { return skillType; }
    public int getTargetAmount(){return  targetAmount;}
    public List<QuestTemplate.Objective> getQuestObjectives() {return objectives;}
    public int getSkillXp() { return skillXp; }
    public UUID getPlayerUUID() {return playerUUID;}

    public void incrementProgress(int amount) {
        if (!isCompleted()) {
            this.progress += amount;
        }
    }

    public void setCurrentProgress(int amount) {
        this.progress = amount;
    }

    public void claimReward() {
        this.rewardClaimed = true;
    }

    public void setRewardClaimed(){
        rewardClaimed = true;
    }

    public boolean canClaim() {
        for (QuestTemplate.Objective objective : objectives){
            if (objective.getProgress() < objective.getObjectiveTargetAmount()){
                return false;
            }
        }
        return isCompleted() && !isRewardClaimed();
    }
}
