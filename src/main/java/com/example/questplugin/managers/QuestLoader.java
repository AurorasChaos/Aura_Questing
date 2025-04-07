/**
 * Manages the loading of quest templates from a YAML configuration file.
 */
package com.example.questplugin.managers;

import com.example.questplugin.*;
import com.example.questplugin.model.QuestTemplate;
import com.example.questplugin.model.QuestTier;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class QuestLoader {
    private final QuestPlugin plugin;
    private final List<QuestTemplate> templates = new ArrayList<>();
    private FileConfiguration config;

    /**
     * Constructs a new QuestLoader with the specified plugin instance.
     *
     * @param plugin The main plugin instance of the quest system.
     */
    public QuestLoader(QuestPlugin plugin) {
        this.plugin = plugin;
        loadTemplates();
    }

    /**
     * Loads all quest templates from the YAML configuration file.
     */
    public void loadTemplates() {
        // Load quest templates from a YAML file
        File templateFile = new File(plugin.getDataFolder(), "quests.yml");
        if (!templateFile.exists()) {
            plugin.saveResource("quests.yml", false);
            plugin.debug("[TemplateLoader] Created new quests.yml");
        }
        config = YamlConfiguration.loadConfiguration(templateFile);

        for (String key : config.getKeys(false)) {
            plugin.debug(key);
            QuestTemplate template = new QuestTemplate(config.getConfigurationSection(key));
            templates.add(template);
            plugin.debug("[TemplateLoader] Loaded quest template: " + template.getId());
        }
    }

    /**
     * Retrieves a list of all loaded quest templates.
     *
     * @return A list of QuestTemplate objects.
     */
    public List<QuestTemplate> getAllTemplates() {
        return templates;
    }

    /**
     * Retrieves a list of quest templates filtered by the specified tier.
     *
     * @param tier The tier to filter quests by.
     * @return A list of QuestTemplate objects that match the specified tier.
     */
    public List<QuestTemplate> getTemplatesByTier(QuestTier tier) {
        return templates.stream()
                .filter(q -> q.getTier() == tier)
                .toList();
    }
}

