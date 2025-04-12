package com.example.questplugin.managers;

import java.util.ArrayList;
import java.util.List;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.Plugin;

public class QuestCompletionConfig {
    private static QuestCompletionConfig instance;
    private FileConfiguration config;

    public static QuestCompletionConfig getInstance(Plugin plugin) {
        if (instance == null) {
            instance = new QuestCompletionConfig(plugin);
        }
        return instance;
    }

    public QuestCompletionConfig(Plugin plugin) {
        // Get the configuration file
        FileConfiguration cfg = plugin.getConfig();
        
        // Save default config if not exists
        if (!plugin.getDataFolder().exists()) {
            plugin.getDataFolder().mkdir();
        }

        // Reload config when changes are detected (optional)
        plugin.getConfig().addDefaults(cfg);
    }

    public FileConfiguration getConfig() {
        return config;
    }

    // Add methods to retrieve data
    public boolean isSet(String path) {
        return config.contains(path);
    }

    public int getInt(String path) {
        if (!isSet(path)) {
            throw new IllegalArgumentException("Path not found in configuration: " + path);
        }
        return config.getInt(path);
    }

    public ConfigurationSection getConfigurationSection(String path) {
        if (!isSet(path)) {
            throw new IllegalArgumentException("Path not found in configuration: " + path);
        }
        return config.getConfigurationSection(path);
    }

public String[] getKeys(boolean recursive) {
    List<String> keys = new ArrayList<>();

    if (recursive) {
        // Collect all keys, including those in nested sections
        collectAllKeys(config, "", keys);
    } else {
        // Collect only top-level keys
        keys.addAll(config.getKeys(false));
    }

    return keys.toArray(new String[0]);
}

private void collectAllKeys(FileConfiguration config, String currentPath, List<String> keys) {
    for (String key : config.getKeys(true)) {
        if (!currentPath.isEmpty()) {
            key = currentPath + "." + key;
        }
        keys.add(key);
        
        // Recursively process nested sections
        ConfigurationSection section = config.getConfigurationSection(key);
        if (section != null && !section.getKeys(false).isEmpty()) {
            collectAllKeys((FileConfiguration) section, key, keys);
        }
    }
}

    // Add any other helper methods you need
}