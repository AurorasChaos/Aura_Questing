package com.example.questplugin.managers;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.model.Quest;
import com.example.questplugin.util.QuestNotifier;

import dev.aurelium.auraskills.api.skill.Skills;
import dev.aurelium.auraskills.api.user.SkillsUser;

/**
 * The RewardHandler class handles all reward-related operations for quests in the QuestPlugin.
 */
public class RewardHandler {
    private final QuestPlugin plugin; // Reference to the main plugin instance
    private final QuestNotifier questNotifier; // Handles notifications about quest completions

    /**
     * Constructs a new RewardHandler with the specified plugin.
     *
     * @param plugin The main plugin instance that this handler is associated with.
     */
    public RewardHandler(QuestPlugin plugin) {
        this.plugin = plugin;
        this.questNotifier = plugin.getQuestNotifier();
    }

    /**
     * Gives a reward to the player for completing a quest, optionally redeeming it immediately.
     *
     * @param player The player who completed the quest and should receive the reward.
     * @param quest  The quest from which the reward is taken.
     * @param redeemReward A boolean indicating whether to redeem the reward immediately or not.
     * @return True if the reward was successfully given, false otherwise.
     */
    public boolean giveReward(Player player, Quest quest, boolean redeemReward) {
        // Check if the quest can be claimed
        if (!quest.canClaim()) return false;

        // If not redeeming the reward, notify about completion and exit
        if (!redeemReward){
            if (quest.canClaim()){
                questNotifier.notifyCompletion(player, quest);
            }
            return false;
        }

        double multiplier = quest.getRarity().getMultiplier(); // Get the reward multiplier based on quest rarity

        plugin.debug("[Reward] Claiming reward for quest: " + quest.getId()); // Debug log for claiming rewards

        // Give currency reward if economy is available
        if (plugin.getEconomy() != null) {
            plugin.debug("[QuestPlugin] Gave " +quest.getCurrencyReward() + " to " + player.getName());
            plugin.getEconomy().depositPlayer(player, quest.getCurrencyReward() * multiplier);
        }

        // Add skill XP if AuraSkills API is available and valid skill type is specified
        try {
            SkillsUser user = plugin.getAuraSkillsApi().getUser(player.getUniqueId());
        
            if (user != null) {
                Skills skill = Skills.valueOf(quest.getSkillType().toUpperCase()); // Convert to uppercase for comparison
                user.addSkillXp(skill, quest.getSkillXp());
        
                player.sendMessage(ChatColor.AQUA + "You gained " + quest.getSkillXp() + " XP in " + skill.name().toLowerCase());
            }
        } catch (IllegalArgumentException e) {
            plugin.getLogger().warning("[QuestPlugin] Unknown skill enum: " + quest.getSkillType()); // Log unknown skill type
        } catch (Exception e) {
            plugin.getLogger().severe("[QuestPlugin] Failed to apply skill XP: " + e.getMessage()); // Log failure to add skill XP
            e.printStackTrace();
        }

        questNotifier.notifyCompletion(player, quest); // Notify about the completion of the quest
        quest.claimReward(); // Mark the reward as claimed
        plugin.getLeaderboardManager().recordCompletion(player.getUniqueId(), quest); // Record the quest completion in leaderboards
        return true;
    }
}

