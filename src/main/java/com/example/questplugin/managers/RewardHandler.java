package com.example.questplugin.managers;

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.model.Quest;
import com.example.questplugin.util.QuestNotifier;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import dev.aurelium.auraskills.api.user.SkillsUser;
import dev.aurelium.auraskills.api.skill.Skills;

public class RewardHandler {
    private final QuestPlugin plugin;
    private final QuestNotifier questNotifier;

    public RewardHandler(QuestPlugin plugin) {
        this.plugin = plugin;
        this.questNotifier = plugin.getQuestNotifier();
    }

    public boolean giveReward(Player player, Quest quest, boolean redeemReward) {
        if (!quest.canClaim()) return false;
        if (!redeemReward){
            if (quest.canClaim()){
                questNotifier.notifyCompletion(player, quest);
            }
            return false;
        }

        double multiplier = quest.getRarity().getMultiplier();

        plugin.debug("[Reward] Claiming reward for quest: " + quest.getId());

        if (plugin.getEconomy() != null) {
            plugin.debug("[QuestPlugin] Gave " +quest.getCurrencyReward() + " to " + player.getName());
            plugin.getEconomy().depositPlayer(player, quest.getCurrencyReward() * multiplier);
        }

        try {
            SkillsUser user = plugin.getAuraSkillsApi().getUser(player.getUniqueId());
        
            if (user != null) {
                Skills skill = Skills.valueOf(quest.getSkillType().toUpperCase());
                user.addSkillXp(skill, quest.getSkillXp());
        
                player.sendMessage(ChatColor.AQUA + "You gained " + quest.getSkillXp() + " XP in " + skill.name().toLowerCase());
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("[QuestPlugin] Unknown skill enum: " + quest.getSkillType());
        } catch (Exception e) {
            plugin.getLogger().severe("[QuestPlugin] Failed to apply skill XP: " + e.getMessage());
            e.printStackTrace();
        }

        questNotifier.notifyCompletion(player, quest);
        quest.claimReward();
        plugin.getLeaderboardManager().recordCompletion(player.getUniqueId(), quest);
        return true;
    }
}
