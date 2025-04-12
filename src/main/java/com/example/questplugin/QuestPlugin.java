package com.example.questplugin;

import com.example.questplugin.Listeners.*;
import com.example.questplugin.commands.DevCommands;
import com.example.questplugin.commands.QuestCommand;
import com.example.questplugin.managers.*;
import com.example.questplugin.ui.QuestGUI;
import com.example.questplugin.util.QuestNotifier;
import com.example.questplugin.util.RarityRoller;

import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.plugin.RegisteredServiceProvider;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;
import dev.aurelium.auraskills.api.AuraSkillsApi;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;

/**
 * The main class for QuestPlugin, handling plugin initialization,
 * event listeners, commands, config loading, and quest data management.
 */

public class QuestPlugin extends JavaPlugin {

    private QuestCompletionConfig questCompletionConfig;

    /**
     * Quest manager to handle quest assignment, progress, and completion.
     */
    private QuestManager questManager;

    /**
     * Quest loader to read and parse quest data from files.
     */
    private QuestLoader questLoader;

    /**
     * Quest storage manager to save and load player and global quest data.
     */
    private QuestStorageManager questStorage;

    /**
     * Leaderboard manager to handle player rankings based on quest completion.
     */
    private LeaderboardManager leaderboardManager;

    /**
     * Rarity roller for generating random rewards with weighted probabilities.
     */
    private RarityRoller rarityRoller;

    /**
     * Economy provider for handling coin rewards (using Vault).
     */
    private Economy economy;

    /**
     * Debug mode flag to enable/disable additional debug logging.
     */
    private boolean debugMode;

    /**
     * Adventure platform for advanced player messaging using Adventure framework.
     */
    private BukkitAudiences adventure;

    /**
     * Quest assigner to handle quest assignments and reassignment upon event triggers.
     */
    private QuestAssigner questAssigner;

    /**
     * Static instance of QuestPlugin for easy access within the plugin.
     */
    private static QuestPlugin instance;

    /**
     * Quest notifier to send messages to players about their quest progress.
     */
    private QuestNotifier questNotifier;

    /**
     * Reward handler to manage reward-related functionalities like generating rewards and applying them.
     */
    private RewardHandler rewardHandler;

    private QuestCompletionListener questCompletionListener;

    @Override
    public void onEnable() {
        loadConfig();
        setupAssignments();
        registerListeners();
        registerCommands();

        if (!setupEconomy()) {
            log("[Vault] Economy provider not found. Coin rewards will be disabled.");
        }

        loadQuestData();

        log("QuestPlugin enabled.");
    }

    /**
     * Loads and saves the default config file, initializing debug mode.
     */
    public void loadConfig() {
        log("[Init] Loading configuration...");
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        this.debugMode = config.getBoolean("Debug", false);

        try {
            questCompletionConfig = new QuestCompletionConfig(this);
            debug("Quest completion loaded succesfully");
        } catch (Exception e) {
            getLogger().severe("Falied to load quest completion file");
        }

    }

    /**
     * Initializes various managers for quest data handling.
     */
    public void setupAssignments() {
        log("[Init] Loading managers...");
        this.questLoader = new QuestLoader(this);
        this.questStorage = new QuestStorageManager(this);
        this.questManager = new QuestManager(this);
        this.leaderboardManager = new LeaderboardManager(this);
        this.rarityRoller = new RarityRoller(this);
        this.adventure = BukkitAudiences.create(this);
        this.questAssigner = new QuestAssigner(this);
        this.questNotifier = new QuestNotifier(this);
        this.rewardHandler = new RewardHandler(this);
        this.questCompletionListener = new QuestCompletionListener();
        instance = this;
    }

    /**
     * Registers event listeners for various in-game events.
     */
    public void registerListeners() {
        log("[Init] Registering event listeners...");
        getServer().getPluginManager().registerEvents(new QuestGUI(this), this);
        getServer().getPluginManager().registerEvents(new MobKillListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockEventsListener(this), this);
        getServer().getPluginManager().registerEvents(new LifeEventsListener(this), this);
        getServer().getPluginManager().registerEvents(new AuraSkillsListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new QuestCompletionListener(), this);
    }

    /**
     * Registers commands for player interaction with QuestPlugin.
     */
    public void registerCommands() {
        log("[Init] Registering commands...");
        getCommand("questdev").setExecutor(new DevCommands(this));
        getCommand("quest").setExecutor(new QuestCommand(this));
    }

    /**
     * Loads saved quest data and ensures initial quest assignments upon startup.
     */
    public void loadQuestData() {
        log("[Init] Loading saved quest data...");
        questStorage.loadIntoManager(questManager);

        questManager.checkResetOnStartup();

        questManager.ensureInitialAssignments();
    }

    @Override
    public void onDisable() {
        log("[Shutdown] Saving player and global quest data...");
        if (questManager != null) {
            questStorage.saveFromManager(questManager);
            questManager.saveGlobalQuests();
        }
        if (this.adventure != null) {
            this.adventure.close();
        }
        log("QuestPlugin disabled.");
    }

    /**
     * Logs messages with the QuestPlugin prefix for better readability.
     *
     * @param message The message to log.
     */
    public void log(String message) {
        getLogger().info(message);
    }

    /**
     * Logs debug messages if debugMode is enabled.
     *
     * @param message The debug message to potentially log.
     */
    public void debug(String message) {
        if (debugMode) log("[DEBUG] " + message);
    }

    /**
     * Sets up the economy provider using Vault, returning false if not found or null.
     *
     * @return True if setup is successful; otherwise, false.
     */
    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    /**
     * Returns the Adventure platform instance for advanced player messaging.
     *
     * @return The BukkitAudiences instance.
     */
    public BukkitAudiences adventure() {
        return adventure;
    }

    // Getters and setters for various managers and attributes

    /**
 * Gets the {@link QuestManager} responsible for managing all quests and their logic.
 *
 * @return the quest manager instance
 */
public QuestManager getQuestManager() {
    return questManager;
}

/**
 * Gets the {@link QuestLoader} responsible for loading quests from configuration files.
 *
 * @return the quest loader instance
 */
public QuestLoader getQuestLoader() {
    return questLoader;
}

/**
 * Gets the {@link QuestStorageManager} responsible for handling quest data storage and retrieval.
 *
 * @return the quest storage manager instance
 */
public QuestStorageManager getQuestStorage() {
    return questStorage;
}

/**
 * Gets the {@link LeaderboardManager} responsible for tracking and displaying player rankings.
 *
 * @return the leaderboard manager instance
 */
public LeaderboardManager getLeaderboardManager() {
    return leaderboardManager;
}

/**
 * Gets the {@link RarityRoller} used for rolling random rarities for generated quests.
 *
 * @return the rarity roller instance
 */
public RarityRoller getRarityRoller() {
    return rarityRoller;
}

/**
 * Gets the {@link Economy} instance used for handling in-game currency transactions.
 *
 * @return the economy provider instance
 */
public Economy getEconomy() {
    return economy;
}

/**
 * Gets the {@link AuraSkillsApi} instance used to interact with the AuraSkills plugin.
 *
 * @return the AuraSkills API instance
 */
public AuraSkillsApi getAuraSkillsApi() {
    return AuraSkillsApi.get();
}

/**
 * Checks whether debug mode is enabled for the plugin.
 *
 * @return true if debug mode is active, false otherwise
 */
public boolean isDebugMode() {
    return debugMode;
}

/**
 * Gets the {@link QuestAssigner} responsible for assigning quests to players.
 *
 * @return the quest assigner instance
 */
public QuestAssigner getQuestAssigner() {
    return questAssigner;
}

/**
 * Gets the {@link QuestNotifier} used to send notifications and updates about quests to players.
 *
 * @return the quest notifier instance
 */
public QuestNotifier getQuestNotifier() {
    return questNotifier;
}

/**
 * Gets the {@link RewardHandler} responsible for distributing rewards when quests are completed.
 *
 * @return the reward handler instance
 */
public RewardHandler getRewardHandler() {
    return rewardHandler;
}


    /**
     * Returns the static instance of QuestPlugin for easy access.
     *
     * @return The QuestPlugin instance.
     */
    public static QuestPlugin getInstance() {
        return instance;
    }

    public QuestCompletionListener getQuestCompletionListener(){
        return questCompletionListener;
    }

    public QuestCompletionConfig getCompletionConfig() {
        return questCompletionConfig;
    }
}