package com.example.questplugin.Listeners;

import com.example.questplugin.*;
import com.example.questplugin.model.QuestType;
import dev.aurelium.auraskills.api.event.skill.SkillLevelUpEvent;
import dev.aurelium.auraskills.api.event.skill.XpGainEvent;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listener class for handling AuraSkills-related events.
 */
public class AuraSkillsListener extends BaseListener implements Listener {

    /**
     * Constructor to initialize the listener and register it with Bukkit.
     *
     * @param plugin The main plugin instance.
     */
    public AuraSkillsListener(QuestPlugin plugin) {
        super(plugin);
    }

    /**
     * Event handler for when a player levels up a skill.
     *
     * @param event The SkillLevelUpEvent containing details about the skill level-up.
     */
    @EventHandler
    public void onAuraSkillLevelUp(SkillLevelUpEvent event) {
        Player player = event.getPlayer();
        String skillName = event.getSkill().name().toUpperCase();

        handleQuestTypeAndTarget(QuestType.GAIN_SKILL_LEVEL, skillName, player);
    }
    /**
     * Event handler for when a player gains XP in a skill.
     *
     * @param event The XpGainEvent containing details about the XP gain.
     */
    @EventHandler
    public void onAuraXpGain(XpGainEvent event) {
        Player player = event.getPlayer();
        String skillName = event.getSkill().name();

        handleQuestTypeAndTarget(QuestType.GAIN_SKILL_EXP, skillName, player);
    }

    @Override
    protected String getEventType() {
        return "AuraSkills";
    }
}
