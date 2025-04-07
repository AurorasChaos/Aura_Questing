/**
 * The {@code RarityRoller} class provides functionality for rolling quest rarities and calculating bonus multipliers based on configuration.
 */
package com.example.questplugin.util;

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.model.QuestRarity;
import org.bukkit.configuration.file.FileConfiguration;

import java.util.Random;

/**
 * The {@code RarityRoller} class provides functionality for rolling quest rarities and calculating bonus multipliers based on configuration.
 */
public class RarityRoller {
    private final QuestPlugin plugin;
    private final Random random = new Random();

    /**
     * Constructs a {@code RarityRoller} instance with the given {@link QuestPlugin}.
     *
     * @param plugin The {@link QuestPlugin} instance to use for configuration access.
     */
    public RarityRoller(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Rolls an upgraded quest rarity based on the input base rarity and config-defined upgrade chances. Upgrade chances are defined as follows:
     * - {@link QuestRarity#COMMON}: {@code rarity_upgrade_chances.COMMON_TO_RARE}
     * - {@link QuestRarity#RARE}: {@code rarity_upgrade_chances.RARE_TO_EPIC}
     * - {@link QuestRarity#EPIC}: {@code rarity_upgrade_chances.EPIC_TO_LEGENDARY}
     *
     * @param base The starting quest rarity.
     * @return The rolled upgraded quest rarity.
     */
    public QuestRarity rollUpgrade(QuestRarity base) {
        FileConfiguration config = plugin.getConfig();
        return switch (base) {
            case COMMON -> tryUpgrade(base, QuestRarity.RARE, config.getDouble("rarity_upgrade_chances.COMMON_TO_RARE"));
            case RARE -> tryUpgrade(base, QuestRarity.EPIC, config.getDouble("rarity_upgrade_chances.RARE_TO_EPIC"));
            case EPIC -> tryUpgrade(base, QuestRarity.LEGENDARY, config.getDouble("rarity_upgrade_chances.EPIC_TO_LEGENDARY"));
            default -> base;
        };
    }

    /**
     * Attempts to upgrade the given quest rarity based on a random double value and the specified chance.
     *
     * @param current The current quest rarity.
     * @param next    The next desired quest rarity upon successful upgrade.
     * @param chance  The upgrade chance as a decimal between 0.0 (never) and 1.0 (always).
     * @return The resulting quest rarity after the roll attempt.
     */
    private QuestRarity tryUpgrade(QuestRarity current, QuestRarity next, double chance) {
        return random.nextDouble() < chance ? next : current;
    }

    /**
     * Retrieves the bonus multiplier for the given quest rarity based on configuration. If no multiplier is found, defaults to 1.0.
     *
     * @param rarity The {@link QuestRarity} to retrieve the bonus multiplier for.
     * @return The bonus multiplier as a double.
     */
    public double getBonusMultiplier(QuestRarity rarity) {
        return plugin.getConfig().getDouble("bonus_rewards." + rarity.name(), 1.0);
    }
}

