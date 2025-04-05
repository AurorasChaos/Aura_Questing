package com.example.questplugin.util;

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.model.QuestRarity;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Random;

public class RarityRoller {
    private final QuestPlugin plugin;
    private final Random random = new Random();

    public RarityRoller(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    public QuestRarity rollUpgrade(QuestRarity base) {
        FileConfiguration config = plugin.getConfig();
        return switch (base) {
            case COMMON -> tryUpgrade(base, QuestRarity.RARE, config.getDouble("rarity_upgrade_chances.COMMON_TO_RARE"));
            case RARE -> tryUpgrade(base, QuestRarity.EPIC, config.getDouble("rarity_upgrade_chances.RARE_TO_EPIC"));
            case EPIC -> tryUpgrade(base, QuestRarity.LEGENDARY, config.getDouble("rarity_upgrade_chances.EPIC_TO_LEGENDARY"));
            default -> base;
        };
    }

    private QuestRarity tryUpgrade(QuestRarity current, QuestRarity next, double chance) {
        return random.nextDouble() < chance ? next : current;
    }

    public double getBonusMultiplier(QuestRarity rarity) {
        return plugin.getConfig().getDouble("bonus_rewards." + rarity.name(), 1.0);
    }
}
