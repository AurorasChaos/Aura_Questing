/**
 * QuestManager handles quest assignments, resets, persistence, and loading for all quest tiers:
 * DAILY, WEEKLY, and GLOBAL. It supports player-specific and shared global quests, saving progress,
 * and ensuring quest limits are honored per configuration.
 */
package com.example.questplugin.managers;

import java.io.File;
import java.io.IOException;
import java.time.LocalDate;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.model.QuestTemplate;
import com.example.questplugin.model.QuestTier;
import com.example.questplugin.model.Quest;
import org.bukkit.Bukkit;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

public class QuestManager {

    private final QuestPlugin plugin;
    private final Map<UUID, List<Quest>> dailyQuests = new ConcurrentHashMap<>();
    private final Map<UUID, List<Quest>> weeklyQuests = new ConcurrentHashMap<>();
    private final List<Quest> globalQuests = new ArrayList<>();
    private final Map<UUID, List<Quest>> playerGlobalQuests = new HashMap<>();

    private final File globalFile;
    private final FileConfiguration globalConfig;
    private final File resetFile;
    private final YamlConfiguration config;
    private LocalDate lastResetDate;

    /**
     * Constructs a new QuestManager and loads reset and global quest data from disk.
     *
     * @param plugin The main plugin instance.
     */
    public QuestManager(QuestPlugin plugin) {
        this.plugin = plugin;
        this.globalFile = new File(plugin.getDataFolder(), "global_quests.yml");
        if (!globalFile.exists()) {
            try {
                globalFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.globalConfig = YamlConfiguration.loadConfiguration(globalFile);

        this.resetFile = new File(plugin.getDataFolder(), "reset_data.yml");
        if (!resetFile.exists()) {
            try {
                resetFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.config = YamlConfiguration.loadConfiguration(resetFile);
        this.lastResetDate = loadLastResetDate();
    }

    /**
     * Saves global quest progress and claim status to disk.
     */
    public void saveGlobalQuests() {
        for (Quest quest : globalQuests) {
            String id = quest.getId();
            globalConfig.set(id + ".progress", quest.getCurrentProgress());
            globalConfig.set(id + ".claimed", quest.isRewardClaimed());
        }
        try {
            globalConfig.save(globalFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
        plugin.debug("[Global] Saved global quests to file.");
    }

    /**
     * Gets a list of weekly quests for a given player UUID.
     *
     * @param uuid The player's UUID.
     * @return List of weekly quests.
     */
    public List<Quest> getPlayerWeeklyQuests(UUID uuid) {
        return weeklyQuests.getOrDefault(uuid, new ArrayList<>());
    }

    /**
     * Gets all quests for a specific tier for a player.
     *
     * @param uuid The player's UUID.
     * @param tier The quest tier.
     * @return List of quests for that tier.
     */
    public List<Quest> getQuestsForTier(UUID uuid, QuestTier tier) {
        return switch (tier) {
            case DAILY -> dailyQuests.getOrDefault(uuid, new ArrayList<>());
            case WEEKLY -> weeklyQuests.getOrDefault(uuid, new ArrayList<>());
            case GLOBAL -> playerGlobalQuests.getOrDefault(uuid, new ArrayList<>());
            case ALL -> {
                List<Quest> all = new ArrayList<>();
                all.addAll(dailyQuests.getOrDefault(uuid, Collections.emptyList()));
                all.addAll(weeklyQuests.getOrDefault(uuid, Collections.emptyList()));
                all.addAll(playerGlobalQuests.getOrDefault(uuid, Collections.emptyList()));
                yield all;
            }
            case null -> dailyQuests.getOrDefault(uuid, new ArrayList<>());
        };
    }

    /**
     * Returns all quests for a player across all types.
     *
     * @param uuid The player's UUID.
     * @return List of all player quests.
     */
    public List<Quest> getAllPlayerQuests(UUID uuid) {
        List<Quest> all = new ArrayList<>();
        all.addAll(getPlayerDailyQuests(uuid));
        all.addAll(getPlayerWeeklyQuests(uuid));
        all.addAll(getGlobalQuests());
        return all;
    }

    /**
     * Gets the combined daily and weekly quests for a player.
     *
     * @param uuid The player's UUID.
     * @return List of player quests.
     */
    public List<Quest> getPlayerQuests(UUID uuid) {
        List<Quest> combinedQuests = new ArrayList<>();
        combinedQuests.addAll(dailyQuests.get(uuid));
        combinedQuests.addAll(weeklyQuests.get(uuid));
        return combinedQuests;
    }

    /**
     * Returns the daily quests assigned to a player.
     *
     * @param uuid The player's UUID.
     * @return List of daily quests.
     */
    public List<Quest> getPlayerDailyQuests(UUID uuid) {
        return dailyQuests.getOrDefault(uuid, new ArrayList<>());
    }

    /**
     * Ensures players have initial daily and weekly quests assigned.
     */
    public void ensureInitialAssignments() {
        int dailyQuestCount = plugin.getConfig().getInt("QuestLimits.DAILY", 7);
        int weeklyQuestCount = plugin.getConfig().getInt("QuestLimits.WEEKLY", 7);
    
        for (UUID uuid : getAllPlayers()) {
            if (getPlayerDailyQuests(uuid).isEmpty()) {
                List<Quest> daily = plugin.getQuestAssigner().getRandomQuestsWeighted(uuid, QuestTier.DAILY, dailyQuestCount);
                assignNewDailyQuests(uuid, daily);
                plugin.debug("[Assign] Assigned new DAILY quests to " + uuid + ". Total = " + dailyQuestCount);
            }
            if (getPlayerWeeklyQuests(uuid).isEmpty()) {
                List<Quest> weekly = plugin.getQuestAssigner().getRandomQuestsWeighted(uuid, QuestTier.WEEKLY, weeklyQuestCount);
                assignNewWeeklyQuests(uuid, weekly);
                plugin.debug("[Assign] Assigned new WEEKLY quests to " + uuid + ". Total = " + weeklyQuestCount);
            }
        }

        assignInitialGlobalQuests();
    }

    public void assignNewDailyQuests(UUID uuid, List<Quest> quests) {
        dailyQuests.put(uuid, quests);
    }

    public void assignNewWeeklyQuests(UUID uuid, List<Quest> quests) {
        weeklyQuests.put(uuid, quests);
    }

    public void assignGlobalQuests(UUID uuid, List<Quest> quests) {
        playerGlobalQuests.put(uuid, quests);
    }

    /**
     * Gets all players that currently have quests.
     *
     * @return Set of player UUIDs.
     */
    public Set<UUID> getAllPlayers() {
        Set<UUID> set = new HashSet<>();
        set.addAll(dailyQuests.keySet());
        set.addAll(weeklyQuests.keySet());
        return set;
    }

    public void setGlobalQuests(List<Quest> quests) {
        this.globalQuests.clear();
        this.globalQuests.addAll(quests);
    }

    public List<Quest> getGlobalQuests() {
        return globalQuests;
    }

    /**
     * Loads the last reset date from disk.
     *
     * @return Last reset date or yesterday if not found.
     */
    private LocalDate loadLastResetDate() {
        String stored = config.getString("last_reset", null);
        if (stored != null) {
            try {
                return LocalDate.parse(stored);
            } catch (Exception ignored) {}
        }
        return LocalDate.now().minusDays(1);
    }

    /**
     * Saves the last reset date to file.
     *
     * @param date The date to save.
     */
    private void saveLastResetDate(LocalDate date) {
        config.set("last_reset", date.toString());
        try {
            config.save(resetFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Checks whether quests need to be reset on startup.
     * Performs daily and weekly resets as needed.
     */
    public void checkResetOnStartup() {
        LocalDate today = LocalDate.now();
        plugin.debug("[Reset] Last reset: " + lastResetDate + ", Today: " + today);

        if (!lastResetDate.equals(today)) {
            performDailyReset();
            if (today.getDayOfWeek().getValue() == 1) {
                performWeeklyReset();
            }
            lastResetDate = today;
            saveLastResetDate(today);
        } else {
            plugin.debug("[Reset] Skipped reset; already ran today.");
        }
    }

    /**
     * Resets daily quests for all players.
     */
    public void performDailyReset() {
        Bukkit.getLogger().info("[QuestPlugin] Performing daily quest reset.");
    
        for (UUID uuid : plugin.getQuestManager().getAllPlayers()) {
            List<Quest> daily = plugin.getQuestAssigner().assignDailyQuests(uuid);
            List<Quest> weekly = plugin.getQuestManager().getPlayerWeeklyQuests(uuid);
            plugin.getQuestManager().assignNewDailyQuests(uuid, daily);
            plugin.getQuestStorage().savePlayerQuests(uuid, daily, weekly);
        }
    
        plugin.getQuestStorage().save();
    }

    /**
     * Resets weekly quests for all players.
     */
    public void performWeeklyReset() {
        Bukkit.getLogger().info("[QuestPlugin] Performing weekly quest reset.");
    
        for (UUID uuid : plugin.getQuestManager().getAllPlayers()) {
            List<Quest> weekly = plugin.getQuestAssigner().assignWeeklyQuests(uuid);
            List<Quest> daily = plugin.getQuestManager().getPlayerDailyQuests(uuid);
            plugin.getQuestManager().assignNewWeeklyQuests(uuid, weekly);
            plugin.getQuestStorage().savePlayerQuests(uuid, daily, weekly);
        }
    
        plugin.getQuestStorage().save();
        Bukkit.getLogger().info("[QuestPlugin] Weekly reset complete.");
    }

    /**
     * Refreshes global quests by removing completed ones and adding new ones up to the limit.
     */
    public void refreshGlobalQuests() {
        List<Quest> currentGlobal = plugin.getQuestManager().getGlobalQuests();
        int globalLimit = plugin.getConfig().getInt("QuestLimits.GLOBAL", 10);
        List<Quest> newGlobals = new ArrayList<>(currentGlobal);
    
        List<QuestTemplate> allGlobalTemplates = plugin.getQuestLoader().getTemplatesByTier(QuestTier.GLOBAL);
        Set<String> usedIds = currentGlobal.stream()
            .filter(q -> !q.isCompleted())
            .map(Quest::getId)
            .collect(Collectors.toSet());
    
        newGlobals.removeIf(Quest::isCompleted);
    
        List<QuestTemplate> available = new ArrayList<>(
            allGlobalTemplates.stream()
                .filter(template -> !usedIds.contains(template.getId()))
                .toList()
        );
        Collections.shuffle(available);
        for (QuestTemplate template : available) {
            if (newGlobals.size() >= globalLimit) break;
            newGlobals.add(template.toQuest());
        }
    
        plugin.getQuestManager().setGlobalQuests(newGlobals);
    }

    /**
     * Assigns initial global quests if none exist.
     */
    public void assignInitialGlobalQuests() {
        if (!globalQuests.isEmpty()) return;
        refreshGlobalQuests();
    }

    /**
     * Adds a development/test quest directly to a player.
     *
     * @param uuid  The player's UUID.
     * @param quest The quest to add.
     */
    public void giveDevQuest(UUID uuid, Quest quest) {
        List<Quest> existing = plugin.getQuestManager().getPlayerDailyQuests(uuid);
        boolean alreadyHas = existing.stream().anyMatch(q -> q.getId().equalsIgnoreCase(quest.getId()));
        if (!alreadyHas) {
            existing.add(quest);
            plugin.getQuestStorage().savePlayerQuests(uuid, existing, plugin.getQuestManager().getPlayerWeeklyQuests(uuid));
            plugin.debug("[Dev] Added quest '" + quest.getId() + "' to player " + uuid);
        } else {
            plugin.debug("[Dev] Player already has quest '" + quest.getId() + "'");
        }
    }
}
