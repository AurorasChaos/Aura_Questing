// This file will serve as the main patch hub for the fixed QuestPlugin system.
// We'll start with logging and config enhancements, then correct data syncing, resets, and missing event handling.

package com.example.questplugin;

import com.example.questplugin.Listeners.*;
import com.example.questplugin.commands.DevCommands;
import com.example.questplugin.commands.QuestCommand;
import com.example.questplugin.managers.*;
import com.example.questplugin.model.QuestLeaderboardSection;
import com.example.questplugin.ui.QuestGUI;
import com.example.questplugin.util.QuestNotifier;
import com.example.questplugin.util.RarityRoller;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import net.milkbowl.vault.economy.Economy;
import dev.aurelium.auraskills.api.AuraSkillsApi;

import org.bukkit.Bukkit;
import org.bukkit.plugin.RegisteredServiceProvider;
import net.kyori.adventure.platform.bukkit.BukkitAudiences;
import com.auradev.universalscoreboard.UniversalScoreboard;
import com.auradev.universalscoreboard.SidebarManager;

public class QuestPlugin extends JavaPlugin {

    private QuestManager questManager;
    private QuestLoader questLoader;
    private QuestStorageManager questStorage;
    private LeaderboardManager leaderboardManager;
    private RarityRoller rarityRoller;
    private Economy economy;
    private boolean debugMode;
    private BukkitAudiences adventure;
    private QuestAssigner questAssigner;
    private static QuestPlugin instance;
    private QuestNotifier questNotifier;
    private RewardHandler rewardHandler;


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

    public void loadConfig(){
        log("[Init] Loading configuration...");
        saveDefaultConfig();
        FileConfiguration config = getConfig();
        this.debugMode = config.getBoolean("Debug", false);
    }

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
        instance = this;

        SidebarManager manager = UniversalScoreboard.get().getSidebarManager();
        manager.registerSection(new QuestLeaderboardSection());
    }

    public void registerListeners() {
        log("[Init] Registering event listeners...");
        getServer().getPluginManager().registerEvents(new QuestGUI(this), this);
        getServer().getPluginManager().registerEvents(new MobKillListener(this), this);
        getServer().getPluginManager().registerEvents(new BlockEventsListener(this), this);
        getServer().getPluginManager().registerEvents(new LifeEventsListener(this), this);
        getServer().getPluginManager().registerEvents(new AuraSkillsListener(this), this);
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
    }

    public void registerCommands(){
        log("[Init] Registering commands...");
        getCommand("questdev").setExecutor(new DevCommands(this));
        getCommand("quest").setExecutor(new QuestCommand(this));
    }

    public void loadQuestData(){
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

    public void log(String message) {
        getLogger().info(message);
    }

    public void debug(String message) {
        if (debugMode) log("[DEBUG] " + message);
    }

    private boolean setupEconomy() {
        if (getServer().getPluginManager().getPlugin("Vault") == null) {
            return false;
        }
        RegisteredServiceProvider<Economy> rsp = getServer().getServicesManager().getRegistration(Economy.class);
        if (rsp == null) return false;
        economy = rsp.getProvider();
        return economy != null;
    }

    public BukkitAudiences adventure() {
        return adventure;
    }

    public QuestManager getQuestManager() { return questManager; }
    public QuestLoader getQuestLoader() { return questLoader; }
    public QuestStorageManager getQuestStorage() { return questStorage; }
    public LeaderboardManager getLeaderboardManager() { return leaderboardManager; }
    public RarityRoller getRarityRoller() { return rarityRoller; }
    public Economy getEconomy() { return economy; }
    public AuraSkillsApi getAuraSkillsApi() { return AuraSkillsApi.get(); }
    public boolean isDebugMode() { return debugMode; }
    public QuestAssigner getQuestAssigner() { return questAssigner;}
    public QuestNotifier getQuestNotifier() { return questNotifier;}
    public RewardHandler getRewardHandler() { return rewardHandler; }

    public static QuestPlugin getInstance() {
        return instance;
    }
}