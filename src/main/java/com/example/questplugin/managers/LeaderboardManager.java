package com.example.questplugin.managers;

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.model.Quest;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import eu.decentsoftware.holograms.api.DHAPI;
import eu.decentsoftware.holograms.api.holograms.Hologram;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

public class LeaderboardManager {

    private final QuestPlugin plugin;
    private final Map<UUID, Integer> scores = new HashMap<>();
    private final File file;
    private final FileConfiguration config;

    public LeaderboardManager(QuestPlugin plugin) {
        this.plugin = plugin;
        this.file = new File(plugin.getDataFolder(), "leaderboard.yml");
        if (!file.exists()) {
            plugin.saveResource("leaderboard.yml", false);
        }
        this.config = YamlConfiguration.loadConfiguration(file);
        load();
    }

    public void addScore(UUID uuid, int amount) {
        int newScore = scores.getOrDefault(uuid, 0) + amount;
        scores.put(uuid, newScore);
        plugin.debug("[Leaderboard] Added " + amount + " points to " + uuid + " (new total: " + newScore + ")");
        save();
        updateHologram();
    }

    public int getScore(UUID uuid) {
        return scores.getOrDefault(uuid, 0);
    }

    public List<Map.Entry<UUID, Integer>> getTop(int limit) {
        return scores.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .limit(limit)
                .collect(Collectors.toList());
    }

    public void load() {
        scores.clear();
        for (String key : config.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                int score = config.getInt(key);
                scores.put(uuid, score);
                plugin.debug("[Leaderboard] Loaded score " + score + " for " + uuid);
            } catch (IllegalArgumentException ignored) {
                plugin.log("[Leaderboard] Skipped invalid UUID: " + key);
            }
        }
    }

    public void save() {
        for (Map.Entry<UUID, Integer> entry : scores.entrySet()) {
            config.set(entry.getKey().toString(), entry.getValue());
        }
        try {
            config.save(file);
            plugin.debug("[Leaderboard] Saved leaderboard to file.");
        } catch (IOException e) {
            plugin.log("[Leaderboard] Failed to save leaderboard: " + e.getMessage());
            e.printStackTrace();
        }
    }

    public void updateHologram() {
        if (!plugin.getConfig().getBoolean("Leaderboard.EnableHologram", false)) return;
        if (!Bukkit.getPluginManager().isPluginEnabled("DecentHolograms")) return;

        String world = plugin.getConfig().getString("Leaderboard.Location.World", "world");
        double x = plugin.getConfig().getDouble("Leaderboard.Location.X", 0);
        double y = plugin.getConfig().getDouble("Leaderboard.Location.Y", 100);
        double z = plugin.getConfig().getDouble("Leaderboard.Location.Z", 0);
        Location location = new Location(Bukkit.getWorld(world), x, y, z);

        String holoId = "questplugin_leaderboard";
        Hologram hologram = DHAPI.getHologram(holoId);
        if (hologram == null) {
            hologram = DHAPI.createHologram(holoId, location, true);
            plugin.debug("[Leaderboard] Created new hologram.");
        }

        List<String> templateLines = plugin.getConfig().getStringList("Leaderboard.Lines");
        List<Map.Entry<UUID, Integer>> top = getTop(templateLines.size() - 1);
        List<String> newLines = new ArrayList<>();

        for (int i = 0; i < templateLines.size(); i++) {
            String line = templateLines.get(i);
            if (i == 0) {
                newLines.add(line); // header
            } else {
                int rank = i;
                if (rank <= top.size()) {
                    UUID id = top.get(rank - 1).getKey();
                    int score = top.get(rank - 1).getValue();
                    String name = Bukkit.getOfflinePlayer(id).getName();
                    if (name == null) name = "Unknown";
                    newLines.add(line.replace("%player" + rank + "%", name).replace("%value" + rank + "%", String.valueOf(score)));
                } else {
                    newLines.add(line.replace("%player" + rank + "%", "None").replace("%value" + rank + "%", "0"));
                }
            }
        }

        DHAPI.setHologramLines(hologram, newLines);
        plugin.debug("[Leaderboard] Updated hologram lines.");
    }

    public void recordCompletion(UUID playerId, Quest quest) {
        int points;
        switch (quest.getRarity()) {
            case LEGENDARY -> points = 10;
            case EPIC -> points = 5;
            case RARE -> points = 3;
            case COMMON -> points = 1;
            default -> points = 1;
        }
    
        addScore(playerId, points);
        plugin.debug("[Leaderboard] Recorded " + points + " pts for " + playerId + " for completing " + quest.getRarity().name() + " quest: " + quest.getId());
    }

    public Map<String, Integer> getTopPlayers(int amount) {
        return getTop(amount).stream()
            .map(entry -> {
                String name = Optional.ofNullable(Bukkit.getOfflinePlayer(entry.getKey()).getName())
                                      .orElse("Unknown");
                return Map.entry(name, entry.getValue());
            })
            .limit(amount)
            .collect(
                LinkedHashMap::new,
                (map, entry) -> map.put(entry.getKey(), entry.getValue()),
                Map::putAll
            );
    }

    public int getRank(UUID playerId) {
        List<Map.Entry<UUID, Integer>> sorted = scores.entrySet().stream()
                .sorted(Map.Entry.<UUID, Integer>comparingByValue().reversed())
                .toList();
    
        for (int i = 0; i < sorted.size(); i++) {
            if (sorted.get(i).getKey().equals(playerId)) {
                return i + 1; // ranks are 1-based
            }
        }
    
        return -1; // not found
    }
    
}