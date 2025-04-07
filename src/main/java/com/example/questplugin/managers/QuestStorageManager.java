package com.example.questplugin.managers;

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.model.Quest;
import com.example.questplugin.model.QuestTemplate;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.*;

/**
 * Manages storage of quest data for players in a plugin.
 */
public class QuestStorageManager {

    /**
     * The main plugin instance.
     */
    private final QuestPlugin plugin;

    /**
     * Map storing player UUIDs to their daily quest data.
     */
    private final Map<UUID, PlayerQuestData> playerQuestData = new HashMap<>();

    /**
     * Map storing player UUIDs to their saved daily quests.
     */
    private final Map<UUID, List<Quest>> savedDaily = new HashMap<>();

    /**
     * Map storing player UUIDs to their saved weekly quests.
     */
    private final Map<UUID, List<Quest>> savedWeekly = new HashMap<>();

    /**
     * The file where player quest data is stored.
     */
    private final File file;

    /**
     * Configuration for the player_quests.yml file.
     */
    private FileConfiguration config;

    /**
     * Constructs a new QuestStorageManager instance and loads existing data if available.
     *
     * @param plugin The main plugin instance.
     */
    public QuestStorageManager(QuestPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "player_quests.yml");

        // Check if the file exists, and create it if not
        if (!file.exists()) {
            plugin.saveResource("player_quests.yml", false);
            plugin.debug("[Storage] Created new player_quests.yml");
        }

        this.config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    /**
     * Returns a set containing all UUIDs of players with stored quest data.
     *
     * @return Set of player UUIDs.
     */
    public Set<UUID> getAllStoredPlayers() {
        return playerQuestData.keySet();
    }

    /**
     * Loads the daily quests for a specific player from storage.
     *
     * @param uuid The player's UUID.
     * @return List of daily quests, or an empty list if none exist.
     */
    public List<Quest> loadPlayerDailyQuests(UUID uuid) {
        return playerQuestData.containsKey(uuid) ? playerQuestData.get(uuid).getDailyQuests() : new ArrayList<>();
    }

    /**
     * Loads the weekly quests for a specific player from storage.
     *
     * @param uuid The player's UUID.
     * @return List of weekly quests, or an empty list if none exist.
     */
    public List<Quest> loadPlayerWeeklyQuests(UUID uuid) {
        return playerQuestData.containsKey(uuid) ? playerQuestData.get(uuid).getWeeklyQuests() : new ArrayList<>();
    }

    /**
     * Loads player quest data from storage into the QuestManager instance.
     *
     * @param questManager The QuestManager instance to load data into.
     */
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

    /**
     * Saves player quest data from the QuestManager instance back to storage.
     *
     * @param questManager The QuestManager instance to save data from.
     */
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

    /**
     * Loads player quest data from the player_quests.yml file into storage.
     */
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
                        String path = uuidStr + ".daily." + key + ".objectives." + obj.getTargetKey();
                        quest.getQuestObjectives().stream()
                                .filter(qObj -> qObj.getTargetKey().equals(obj.getTargetKey()))
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
                        String path = uuidStr + ".weekly." + key + ".objectives." + obj.getTargetKey();
                        quest.getQuestObjectives().stream()
                                .filter(qObj -> qObj.getTargetKey().equals(obj.getTargetKey()))
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

    /**
     * Saves player quest data from storage to the player_quests.yml file.
     */
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

    /**
     * Saves provided daily and weekly quest lists for a specific player UUID.
     *
     * @param uuid   The player's UUID.
     * @param daily  The list of daily quests to save.
     * @param weekly The list of weekly quests to save.
     */
    public void savePlayerQuests(UUID uuid, List<Quest> daily, List<Quest> weekly) {
        savedDaily.put(uuid, daily);
        savedWeekly.put(uuid, weekly);
        playerQuestData.put(uuid, new PlayerQuestData(daily, weekly));
        plugin.debug("[Storage] Queued quest data for " + uuid);
    }

    /**
     * Returns the list of saved daily quests for a specific player UUID.
     *
     * @param uuid The player's UUID.
     * @return List of saved daily quests, or an empty list if none exist.
     */
    public List<Quest> getSavedDaily(UUID uuid) {
        return savedDaily.getOrDefault(uuid, new ArrayList<>());
    }

    /**
     * Returns the list of saved weekly quests for a specific player UUID.
     *
     * @param uuid The player's UUID.
     * @return List of saved weekly quests, or an empty list if none exist.
     */
    public List<Quest> getSavedWeekly(UUID uuid) {
        return savedWeekly.getOrDefault(uuid, new ArrayList<>());
    }

    /**
     * Represents a player's daily and weekly quest data.
     */
    public class PlayerQuestData {

        /**
         * The list of daily quests for this player.
         */
        private final List<Quest> daily;

        /**
         * The list of weekly quests for this player.
         */
        private final List<Quest> weekly;

        /**
         * Constructs a new PlayerQuestData instance with provided daily and weekly quest lists.
         *
         * @param daily   The list of daily quests.
         * @param weekly The list of weekly quests.
         */
        public PlayerQuestData(List<Quest> daily, List<Quest> weekly) {
            this.daily = daily;
            this.weekly = weekly;
        }

        /**
         * Returns the list of daily quests for this player.
         *
         * @return List of daily quests.
         */
        public List<Quest> getDailyQuests() {
            return daily;
        }

        /**
         * Returns the list of weekly quests for this player.
         *
         * @return List of weekly quests.
         */
        public List<Quest> getWeeklyQuests() {
            return weekly;
        }
    }
}
