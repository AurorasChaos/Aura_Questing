package com.example.questplugin.Listeners;

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.events.QuestCompleteEvent;
import com.example.questplugin.model.Quest;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.plugin.PluginManager;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class QuestCompletionListener implements Listener {
    
    private final String notificationPermission = "questplugin.notify";
    private final String rewardMessageFormat = "%s completed quest '%s' and earned %.2f currency and %d skill XP.";
    private YamlConfiguration completionConfig;
    private File completionFile;

    public QuestCompletionListener() {
        PluginManager pluginManager = Bukkit.getPluginManager();
        pluginManager.registerEvents(this, com.example.questplugin.QuestPlugin.getInstance());

        // Initialize the YAML configuration
        completionFile = new File(QuestPlugin.getInstance().getDataFolder(), "quest_completions.yml");
        if (!completionFile.exists()) {
            try {
                completionFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        completionConfig = YamlConfiguration.loadConfiguration(completionFile);
    }

    @EventHandler
    public void onQuestCompleted(QuestCompleteEvent event) {
        Player player = event.getPlayer();
        Quest quest = event.getQuest();

        if (player.hasPermission(notificationPermission)) {
            String message = String.format(rewardMessageFormat, player.getName(), quest.getDescription(), quest.getCurrencyReward(), quest.getSkillXp());
            player.sendMessage(message);
        }

        // Record the completion in the YAML file
        UUID playerId = player.getUniqueId();
        String playerName = player.getName();

        if (!completionConfig.isSet(quest.getId())) {
            completionConfig.set(quest.getId() + ".completions", 0);
        }
        int currentCompletions = completionConfig.getInt(quest.getId() + ".completions");
        completionConfig.set(quest.getId() + ".completions", currentCompletions + 1);

        if (!completionConfig.isSet(quest.getId() + ".players")) {
            completionConfig.createSection(quest.getId() + ".players");
        }
        completionConfig.set(quest.getId() + ".players." + playerId.toString(), playerName);

        // Save the configuration
        try {
            completionConfig.save(completionFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
