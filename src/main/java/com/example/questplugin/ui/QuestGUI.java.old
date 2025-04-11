// QuestGUI.java with safe Map<Slot, Quest> click handling, pagination, and ALL tab support
package com.example.questplugin.ui;

import com.example.questplugin.model.QuestTemplate;
import com.example.questplugin.util.QuestNotifier;
import com.example.questplugin.QuestPlugin;
import com.example.questplugin.model.QuestTier;
import com.example.questplugin.managers.RewardHandler;
import com.example.questplugin.model.Quest;
import com.example.questplugin.util.QuestFilter;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class QuestGUI implements Listener {

    private static final int QUESTS_PER_PAGE = 14;
    private static final int[] QUEST_SLOTS = {
        10, 11, 12, 13, 14, 15, 16,
        19, 20, 21, 22, 23, 24, 25
    };

    private final QuestPlugin plugin;
    private final RewardHandler rewardHandler;
    private final QuestNotifier notifier;

    private final Map<UUID, Integer> pageMap = new HashMap<>();
    private final Map<UUID, QuestFilter> filterMap = new HashMap<>();
    private final Map<UUID, QuestTier> tierMap = new HashMap<>();
    private final Map<UUID, Map<Integer, Quest>> slotQuestMap = new HashMap<>();

    private final Map<UUID, Set<String>> claimedGlobalQuestMap = new HashMap<>();
    private final Map<UUID, Integer> shimmerTaskIds = new HashMap<>();
    private final File claimFile;
    private final FileConfiguration claimConfig;

    private QuestTier currentTier;
    private int currentPage;

    private final Material[] shimmerColors = {
        Material.RED_STAINED_GLASS_PANE,
        Material.ORANGE_STAINED_GLASS_PANE,
        Material.YELLOW_STAINED_GLASS_PANE,
        Material.LIME_STAINED_GLASS_PANE,
        Material.LIGHT_BLUE_STAINED_GLASS_PANE,
        Material.PURPLE_STAINED_GLASS_PANE
    };

    public QuestGUI(QuestPlugin plugin) {
        this.plugin = plugin;
        this.rewardHandler = new RewardHandler(plugin);
        this.notifier = new QuestNotifier(plugin);

        this.claimFile = new File(plugin.getDataFolder(), "global_claims.yml");
        if (!claimFile.exists()) {
            try {
                claimFile.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        this.claimConfig = YamlConfiguration.loadConfiguration(claimFile);
        loadClaims();
    }

    public void open(Player player, int page, QuestTier tier, QuestFilter filter) {
        UUID uuid = player.getUniqueId();
        List<Quest> quests = plugin.getQuestManager().getQuestsForTier(uuid, tier);
        List<Quest> filtered = filter.apply(quests);

        slotQuestMap.remove(uuid);

        pageMap.put(uuid, page);
        filterMap.put(uuid, filter);
        tierMap.put(uuid, tier);

        currentPage = page;
        currentTier = tier;

        plugin.debug("Here's the current info:" + page + " " +filter + " " + tier );

        int maxPages = (int) Math.ceil(filtered.size() / (double) QUESTS_PER_PAGE);
        if (maxPages <= 0) maxPages = 1;
        page = Math.max(0, Math.min(page, maxPages - 1));

        Inventory gui = Bukkit.createInventory(null, 36, getGuiTitle(tier, page));
        Map<Integer, Quest> slotMap = new HashMap<>();

        int start = page * QUESTS_PER_PAGE;
        int end = Math.min(start + QUESTS_PER_PAGE, filtered.size());
        List<Quest> pageQuests = filtered.subList(start, end);

        if (pageQuests.isEmpty()) {
            ItemStack noQuests = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
            ItemMeta meta = noQuests.getItemMeta();
            meta.setDisplayName("§7No quests to display.");
            noQuests.setItemMeta(meta);
            gui.setItem(13, noQuests);
        } else {
            for (int i = 0; i < pageQuests.size(); i++) {
                if (i < QUEST_SLOTS.length) {
                    int slot = QUEST_SLOTS[i];
                    gui.setItem(slot, createQuestItem(pageQuests.get(i)));
                    slotMap.put(slot, pageQuests.get(i));
                }
            }
        }

        slotQuestMap.put(uuid, slotMap);

        gui.setItem(27, page > 0 ? createNavItem(Material.ARROW, "Previous Page") : createNavItem(Material.RED_STAINED_GLASS_PANE, "No Previous Page"));
        gui.setItem(28, createNavItem(Material.BARRIER, "§cClose Menu"));
        gui.setItem(35, page < maxPages - 1 ? createNavItem(Material.ARROW, "Next Page") : createNavItem(Material.RED_STAINED_GLASS_PANE, "No Next Page"));
        gui.setItem(31, createNavItem(Material.HOPPER, "Filter: " + filter.name()));
        gui.setItem(29, tier == QuestTier.DAILY ? glowing(createNavItem(Material.EMERALD, "Daily Quests")) : createNavItem(Material.EMERALD, "Daily Quests"));
        gui.setItem(30, tier == QuestTier.WEEKLY ? glowing(createNavItem(Material.DIAMOND, "Weekly Quests")) : createNavItem(Material.DIAMOND, "Weekly Quests"));
        gui.setItem(32, tier == QuestTier.GLOBAL ? glowing(createNavItem(Material.NETHER_STAR, "Global Quests")) : createNavItem(Material.NETHER_STAR, "Global Quests"));
        gui.setItem(33, tier == QuestTier.ALL ? glowing(createNavItem(Material.BOOK, "All Quests")) : createNavItem(Material.BOOK, "All Quests"));

        slotQuestMap.put(uuid, slotMap);
        player.openInventory(gui);
        startShimmeringBorder(player, gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Map<Integer, Quest> questMap = slotQuestMap.getOrDefault(player.getUniqueId(), Collections.emptyMap());
        if (!event.getView().getTitle().contains("Quests")) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();

        if (slotQuestMap.get(slot) == null){
            open(player, currentPage, currentTier, QuestFilter.ALL);
        }

        questMap = slotQuestMap.get(player.getUniqueId());
        if (questMap.containsKey(slot)) {
            Quest quest = questMap.get(slot);
            plugin.debug("[GUI] Player clicked quest: " + quest.getId() + " | canClaim=" + quest.canClaim());

            if (quest.canClaim()) {
                rewardHandler.giveReward(player, quest, true);
                notifier.notifyCompletion(player, quest);
                plugin.getQuestStorage().savePlayerQuests(
                    player.getUniqueId(),
                    plugin.getQuestManager().getPlayerDailyQuests(player.getUniqueId()),
                    plugin.getQuestManager().getPlayerWeeklyQuests(player.getUniqueId())
                );
                open(player, pageMap.getOrDefault(player.getUniqueId(), 0), tierMap.get(player.getUniqueId()), filterMap.get(player.getUniqueId()));
            } else {
                if (quest.isCompleted()){
                    player.sendMessage(ChatColor.RED + "❌ You've already claimed this.");
                } else {
                    player.sendMessage(ChatColor.RED + "❌ You can't claim this yet.");
                }
            }
        } else {
            switch (slot) {
                case 27 -> open(player, pageMap.getOrDefault(player.getUniqueId(), 0) - 1, tierMap.get(player.getUniqueId()), filterMap.get(player.getUniqueId()));
                case 28 -> {
                    player.closeInventory();
                    player.playSound(player.getLocation(), Sound.UI_BUTTON_CLICK, 1f, 1f);
                }
                case 35 -> open(player, pageMap.getOrDefault(player.getUniqueId(), 0) + 1, tierMap.get(player.getUniqueId()), filterMap.get(player.getUniqueId()));
                case 31 -> open(player, 0, tierMap.get(player.getUniqueId()), filterMap.get(player.getUniqueId()).next());
                case 29 -> open(player, 0, QuestTier.DAILY, QuestFilter.ALL);
                case 30 -> open(player, 0, QuestTier.WEEKLY, QuestFilter.ALL);
                case 32 -> open(player, 0, QuestTier.GLOBAL, QuestFilter.ALL);
                case 33 -> open(player, 0, QuestTier.ALL, QuestFilter.ALL);
            }
        }
    }

    private String getGuiTitle(QuestTier tier, int page) {
        return switch (tier) {
            case DAILY -> "§a§lDaily Quests §7(Page " + (page + 1) + ")";
            case WEEKLY -> "§9§lWeekly Quests §7(Page " + (page + 1) + ")";
            case GLOBAL -> "§d§lGlobal Quests §7(Page " + (page + 1) + ")";
            case ALL -> "§e§lAll Quests §7(Page " + (page + 1) + ")";
            case null, default ->  "§a§lDaily Quests §7(Page " + (page + 1) + ")";
        };
    }

    private ItemStack createNavItem(Material mat, String name) {
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.setDisplayName("§e" + name);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack glowing(ItemStack item) {
        item.addUnsafeEnchantment(Enchantment.LURE, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }

    private ItemStack createQuestItem(Quest quest) {
        Material mat = switch (quest.getRarity()) {
            case COMMON -> Material.PAPER;
            case RARE -> Material.MAP;
            case EPIC -> Material.ENCHANTED_BOOK;
            case LEGENDARY -> Material.NETHER_STAR;
        };

        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(quest.getRarity().getColor() + quest.getDescription());
        List<String> lore = new ArrayList<>();
        lore.add("");
        for (QuestTemplate.Objective obj : quest.getQuestObjectives()){
            lore.add(obj.getDescription());
            lore.add(obj.getProgress() + " /" + obj.getTargetAmount());
        }
        lore.add("");
        lore.add("Overall : " + quest.getCurrentProgress() + " / " + quest.getTargetAmount());
        lore.add("");
        lore.add("§b+ " + quest.getCurrencyReward() + " coins");
        lore.add("§d+ " + quest.getSkillType().toUpperCase() + ": " + quest.getSkillXp() + "xp");

        lore.add("§8Rarity: " + quest.getRarity().getDisplayName());

        if (quest.isRewardClaimed()) {
            lore.add("§a✔ Reward claimed!");
        } else if (quest.isCompleted()) {
            lore.add("§eClick to claim reward!");
            meta.addEnchant(Enchantment.LUCK, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        } else {
            lore.add("§cNot completed yet.");
        }

        meta.setLore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private void startShimmeringBorder(Player player, Inventory gui) {
        UUID uuid = player.getUniqueId();
        stopShimmering(uuid);
        int[] shimmerSlots = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26 };
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            int frame = 0;
            public void run() {
                Material mat = shimmerColors[frame % shimmerColors.length];
                ItemStack pane = new ItemStack(mat);
                ItemMeta meta = pane.getItemMeta();
                meta.setDisplayName("§r");
                pane.setItemMeta(meta);
                for (int slot : shimmerSlots) {
                    if (slot < gui.getSize()) gui.setItem(slot, pane);
                }
                frame++;
            }
        }, 0L, 30L);
        shimmerTaskIds.put(uuid, taskId);
    }

    private void stopShimmering(UUID uuid) {
        if (shimmerTaskIds.containsKey(uuid)) {
            Bukkit.getScheduler().cancelTask(shimmerTaskIds.remove(uuid));
        }
    }

    private void loadClaims() {
        for (String key : claimConfig.getKeys(false)) {
            UUID uuid = UUID.fromString(key);
            List<String> ids = claimConfig.getStringList(key);
            claimedGlobalQuestMap.put(uuid, new HashSet<>(ids));
        }
    }

    private void saveClaims() {
        for (Map.Entry<UUID, Set<String>> entry : claimedGlobalQuestMap.entrySet()) {
            claimConfig.set(entry.getKey().toString(), new ArrayList<>(entry.getValue()));
        }
        try {
            claimConfig.save(claimFile);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        stopShimmering(event.getPlayer().getUniqueId());
    }
}



