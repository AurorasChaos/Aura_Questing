package com.example.questplugin.model;

import org.bukkit.configuration.ConfigurationSection;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
/**
 * Represents a template for a quest.
 * This class is used to load and manage quest data from configuration files.
 */
public class QuestTemplate {
    private final String id;
    private final String description;
    private final double currencyReward;
    private final String skillType;
    private final int skillXp;
    private final QuestTier tier;
    private final QuestRarity rarity;
    private final List<Objective> objectives = new ArrayList<>();

    /**
     * Constructs a new QuestTemplate from a ConfigurationSection.
     *
     * @param section The ConfigurationSection containing the quest data.
     */
    public QuestTemplate(ConfigurationSection section) {
        this.id = section.getString("id");
        this.description = section.getString("description");
        this.currencyReward = section.getDouble("currency");
        this.skillType = section.getString("skill_type");
        this.skillXp = section.getInt("skill_xp");
        this.tier = QuestTier.valueOf(section.getString("tier"));
        this.rarity = QuestRarity.valueOf(section.getString("rarity"));

        ConfigurationSection objectivesSection = section.getConfigurationSection("objectives");
        if (objectivesSection != null) {
            for (String objectiveKey : objectivesSection.getKeys(false)) {
                ConfigurationSection objSection = objectivesSection.getConfigurationSection(objectiveKey);
                if (objSection != null) {
            objectives.add(new Objective(
                    QuestType.valueOf(objSection.getString("type")),
                    objSection.getString("target_key"),
                    objSection.getInt("target_amount"),
                    objSection.getString("description")
            ));
        }
    }
        } else {
            throw new IllegalArgumentException("Quest does not have objectives section.");
        }
    }

    /**
     * Gets the ID of the quest.
     *
     * @return The ID of the quest.
     */
    public String getId() {
        return id;
    }

    /**
     * Gets the description of the quest.
     *
     * @return The description of the quest.
     */
    public String getDescription() {
        return description;
    }

    /**
     * Gets the currency reward for completing the quest.
     *
     * @return The currency reward for completing the quest.
     */
    public double getCurrencyReward() {
        return currencyReward;
    }

    /**
     * Gets the type of skill required for this quest.
     *
     * @return The type of skill required for this quest.
     */
    public String getSkillType() {
        return skillType;
    }

    /**
     * Gets the experience points required to complete this quest.
     *
     * @return The experience points required to complete this quest.
     */
    public int getSkillXp() {
        return skillXp;
    }

    /**
     * Gets the tier of the quest.
     *
     * @return The tier of the quest.
     */
    public QuestTier getTier() {
        return tier;
    }

    /**
     * Gets the rarity of the quest.
     *
     * @return The rarity of the quest.
     */
    public QuestRarity getRarity() {
        return rarity;
    }

    /**
     * Gets a list of objectives associated with this quest.
     *
     * @return A list of objectives associated with this quest.
     */
    public List<Objective> getObjectives() {
        return new ArrayList<>(objectives); // Return defensive copy to prevent external modification
    }

    /**
     * Converts this QuestTemplate to an instance of the Quest class.
     *
     * @return A new Quest object based on this template.
     */
    public Quest toQuest() {
        return new Quest(this, java.util.UUID.randomUUID());
    }

    /**
     * Represents an objective within a quest.
     */
    public static class Objective {
        private final QuestType type;
        private final String targetKey;
        private final int targetAmount;
        private int progress = 0;
        private final String description;

        /**
         * Constructs a new Objective with the given parameters.
         *
         * @param type          The type of the objective.
         * @param targetKey     The key associated with the objective's target.
         * @param targetAmount  The target amount for this objective.
         * @param description   A description of the objective (optional).
         */
        public Objective(QuestType type, String targetKey, int targetAmount, String description) {
            if (Objects.equals(description, "")) {
                this.description = type + " " + targetKey + " " + targetAmount;
            } else {
                this.description = description;
            }
            this.type = type;
            this.targetKey = targetKey;
            this.targetAmount = targetAmount;
        }

        /**
         * Gets the type of the objective.
         *
         * @return The type of the objective.
         */
        public QuestType getType() {
            return type;
        }

        /**
         * Gets the key associated with the objective's target.
         *
         * @return The key associated with the objective's target.
         */
        public String getTargetKey() {
            return targetKey;
        }

        /**
         * Gets the target amount for this objective.
         *
         * @return The target amount for this objective.
         */
        public int getTargetAmount() {
            return targetAmount;
        }

        /**
         * Gets the current progress of the objective.
         *
         * @return The current progress of the objective.
         */
        public int getProgress() {
            return progress;
        }

        /**
         * Increments the progress of the objective by the specified amount.
         *
         * @param amountToProgress The amount to increment the progress by.
         */
        public void incrementProgress(int amountToProgress) {
            this.progress += amountToProgress;
        }

        /**
         * Gets a description of the objective.
         *
         * @return A description of the objective.
         */
        public String getDescription() {
            return description;
        }

        /**
         * Sets the progress of the objective to the specified value.
         *
         * @param progress The new progress value.
         */
        public void setProgress(int progress) {
            this.progress = progress;
        }
    }
}
