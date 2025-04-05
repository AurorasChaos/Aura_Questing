package com.example.questplugin.Listeners;

import java.util.List;
import java.util.UUID;

import com.example.questplugin.model.Quest;
import com.example.questplugin.QuestPlugin;
import com.example.questplugin.model.QuestTier;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;

public class PlayerJoinListener implements Listener {

    private final QuestPlugin plugin;

    public PlayerJoinListener(QuestPlugin plugin) {
        this.plugin = plugin;
    }

@EventHandler
public void onPlayerJoin(PlayerJoinEvent event) {
    Player player = event.getPlayer();
    UUID uuid = player.getUniqueId();

    Bukkit.getScheduler().runTaskLater(plugin, () -> {
        List<Quest> daily = plugin.getQuestManager().getPlayerDailyQuests(uuid);
        List<Quest> weekly = plugin.getQuestManager().getPlayerWeeklyQuests(uuid);

        if (daily.isEmpty()) {
            List<Quest> newDaily = plugin.getQuestAssigner().getRandomQuestsWeighted(uuid, QuestTier.DAILY, 5);
            plugin.getQuestManager().assignNewDailyQuests(uuid, newDaily);
            plugin.debug("[JoinAssign] Assigned new DAILY quests to " + uuid);
        }

        if (weekly.isEmpty()) {
            List<Quest> newWeekly = plugin.getQuestAssigner().getRandomQuestsWeighted(uuid, QuestTier.WEEKLY, 7);
            plugin.getQuestManager().assignNewWeeklyQuests(uuid, newWeekly);
            plugin.debug("[JoinAssign] Assigned new WEEKLY quests to " + uuid);
        }

        // Assign global quests
        plugin.getQuestManager().assignGlobalQuests(uuid, plugin.getQuestManager().getGlobalQuests());
    }, 20L); // Delay to ensure data is loaded
}
} 
