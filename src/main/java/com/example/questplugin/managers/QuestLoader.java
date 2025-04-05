package com.example.questplugin.managers;

import com.example.questplugin.*;
import com.example.questplugin.model.QuestRarity;
import com.example.questplugin.model.QuestTemplate;
import com.example.questplugin.model.QuestTier;
import com.example.questplugin.model.QuestType;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.*;

public class QuestLoader {

    private final QuestPlugin plugin;
    private final List<QuestTemplate> templates = new ArrayList<>();
    private FileConfiguration config;

    public QuestLoader(QuestPlugin plugin) {
        this.plugin = plugin;
        loadTemplates();
    }

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

    public List<QuestTemplate> getAllTemplates() {
        return templates;
    }

    public List<QuestTemplate> getTemplatesByTier(QuestTier tier) {
        return templates.stream()
                .filter(q -> q.getQuestTier() == tier)
                .toList();
    }
}
