package com.example.questplugin.managers;

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.model.Quest;
import com.example.questplugin.model.QuestTemplate;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import java.io.File;
import java.io.IOException;
import java.util.*;

public class QuestStorageManager {

    private final QuestPlugin plugin;
    private final Map<UUID, PlayerQuestData> playerQuestData = new HashMap<>();
    private final Map<UUID, List<Quest>> savedDaily = new HashMap<>();
    private final Map<UUID, List<Quest>> savedWeekly = new HashMap<>();
    private final File file;
    private FileConfiguration config;

    public QuestStorageManager(QuestPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "player_quests.yml");
        if (!file.exists()) {
            plugin.saveResource("player_quests.yml", false);
            plugin.debug("[Storage] Created new player_quests.yml");
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public Set<UUID> getAllStoredPlayers() {
        return playerQuestData.keySet();
    }

    public List<Quest> loadPlayerDailyQuests(UUID uuid) {
        return playerQuestData.containsKey(uuid) ? playerQuestData.get(uuid).getDailyQuests() : new ArrayList<>();
    }

    public List<Quest> loadPlayerWeeklyQuests(UUID uuid) {
        return playerQuestData.containsKey(uuid) ? playerQuestData.get(uuid).getWeeklyQuests() : new ArrayList<>();
    }

    public void loadIntoManager(QuestManager questManager) {
        plugin.debug("[Storage] Loading saved quests into QuestManager...");
        for (UUID uuid : getAllStoredPlayers()) {
            List<Quest> daily = loadPlayerDailyQuests(uuid);
            List<Quest> weekly = loadPlayerWeeklyQuests(uuid);
            questManager.assignNewDailyQuests(uuid, daily);
            questManager.assignNewWeeklyQuests(uuid, weekly);
            plugin.debug("[Storage] Loaded " + daily.size() + " daily and " + weekly.size() + " weekly quests for " + uuid);
        }
    }

    public void saveFromManager(QuestManager questManager) {
        plugin.debug("[Storage] Saving player quest data from manager...");
        for (UUID uuid : questManager.getAllPlayers()) {
            List<Quest> daily = questManager.getPlayerDailyQuests(uuid);
            List<Quest> weekly = questManager.getPlayerWeeklyQuests(uuid);
            savePlayerQuests(uuid, daily, weekly);
            plugin.debug("[Storage] Saved " + daily.size() + " daily and " + weekly.size() + " weekly quests for " + uuid);
        }
        save();
    }

    public void load() {
        plugin.debug("[Storage] Loading quests from player_quests.yml...");
        for (String uuidStr : config.getKeys(false)) {
            UUID uuid = UUID.fromString(uuidStr);
            List<Quest> daily = new ArrayList<>();
            List<Quest> weekly = new ArrayList<>();

            if (config.contains(uuidStr + ".daily")) {
                for (String key : config.getConfigurationSection(uuidStr + ".daily").getKeys(false)) {
                    QuestTemplate template = plugin.getQuestLoader().getAllTemplates().stream()
                            .filter(q -> q.getId().equals(key)).findFirst().orElse(null);
                    if (template == null) {
                        plugin.debug("[Storage] Skipped unknown daily quest ID: " + key);
                        continue;
                    }
                    Quest quest = new Quest(template, uuid);
                    for (QuestTemplate.Objective obj : template.getObjectives()) {
                        String path = uuidStr + ".daily." + key + ".objectives." + obj.getObjectiveTargetKey();
                        quest.getQuestObjectives().stream()
                                .filter(qObj -> qObj.getObjectiveTargetKey().equals(obj.getObjectiveTargetKey()))
                                .findFirst()
                                .ifPresent(qObj -> {
                                    qObj.setProgress(config.getInt(path + ".progress"));
                                    if (config.getBoolean(path + ".claimed")) quest.setRewardClaimed();
                                });
                    }
                    daily.add(quest);
                }
            }

            if (config.contains(uuidStr + ".weekly")) {
                for (String key : config.getConfigurationSection(uuidStr + ".weekly").getKeys(false)) {
                    QuestTemplate template = plugin.getQuestLoader().getAllTemplates().stream()
                            .filter(q -> q.getId().equals(key)).findFirst().orElse(null);
                    if (template == null) {
                        plugin.debug("[Storage] Skipped unknown weekly quest ID: " + key);
                        continue;
                    }
                    Quest quest = new Quest(template, uuid);
                    for (QuestTemplate.Objective obj : template.getObjectives()) {
                        String path = uuidStr + ".weekly." + key + ".objectives." + obj.getObjectiveTargetKey();
                        quest.getQuestObjectives().stream()
                                .filter(qObj -> qObj.getObjectiveTargetKey().equals(obj.getObjectiveTargetKey()))
                                .findFirst()
                                .ifPresent(qObj -> {
                                    qObj.setProgress(config.getInt(path + ".progress"));
                                    if (config.getBoolean(path + ".claimed")) quest.setRewardClaimed();
                                });
                    }
                    weekly.add(quest);
                }
            }

            savedDaily.put(uuid, daily);
            savedWeekly.put(uuid, weekly);
            playerQuestData.put(uuid, new PlayerQuestData(daily, weekly));
            plugin.debug("[Storage] Loaded " + daily.size() + " daily and " + weekly.size() + " weekly quests for " + uuid);
        }
    }

    public void save() {
        plugin.debug("[Storage] Saving player_quests.yml...");
        for (UUID uuid : savedDaily.keySet()) {
            for (Quest q : savedDaily.get(uuid)) {
                String path = uuid.toString() + ".daily." + q.getId();
                config.set(path + ".progress", q.getCurrentProgress());
                config.set(path + ".claimed", q.isRewardClaimed());
            }
        }
        for (UUID uuid : savedWeekly.keySet()) {
            for (Quest q : savedWeekly.get(uuid)) {
                String path = uuid.toString() + ".weekly." + q.getId();
                config.set(path + ".progress", q.getCurrentProgress());
                config.set(path + ".claimed", q.isRewardClaimed());
            }
        }
        try {
            config.save(file);
            plugin.debug("[Storage] Quest data successfully saved to file.");
        } catch (IOException e) {
            plugin.log("[Storage] Failed to save player_quests.yml: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void savePlayerQuests(UUID uuid, List<Quest> daily, List<Quest> weekly) {
        savedDaily.put(uuid, daily);
        savedWeekly.put(uuid, weekly);
        playerQuestData.put(uuid, new PlayerQuestData(daily, weekly));
        plugin.debug("[Storage] Queued quest data for " + uuid);
    }

    public List<Quest> getSavedDaily(UUID uuid) {
        return savedDaily.getOrDefault(uuid, new ArrayList<>());
    }

    public List<Quest> getSavedWeekly(UUID uuid) {
        return savedWeekly.getOrDefault(uuid, new ArrayList<>());
    }

    public class PlayerQuestData {
        private final List<Quest> daily;
        private final List<Quest> weekly;

        public PlayerQuestData(List<Quest> daily, List<Quest> weekly) {
            this.daily = daily;
            this.weekly = weekly;
        }

        public List<Quest> getDailyQuests() {
            return daily;
        }

        public List<Quest> getWeeklyQuests() {
            return weekly;
        }
    }
}