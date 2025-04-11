package com.example.questplugin.ui;

import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.model.Quest;
import com.example.questplugin.model.QuestTier;
import com.example.questplugin.util.QuestFilter;

import java.util.*;

public class QuestGUI implements Listener {
    private static final int[] QUEST_SLOTS = { 10, 11, 12, 14, 15, 16, 30, 31, 32 };
    private final QuestPlugin plugin;
    private final Map<UUID, Integer> pageMap = new HashMap<>();
    private final Map<UUID, QuestTier> tierMap = new HashMap<>();
    private final Map<UUID, QuestFilter> filterMap = new HashMap<>();
    private final Map<UUID, Map<Integer, Quest>> slotQuestMap = new HashMap<>();

    public QuestGUI(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    public void open(Player player, int page, QuestTier tier, QuestFilter filter) {
        Inventory gui = plugin.getServer().createInventory(null, 36, getGuiTitle(tier, page));
        List<Quest> pageQuests = getPageQuests(player.getUniqueId(), page, tier, filter);
        Map<Integer, Quest> slotMap = new HashMap<>();

        if (pageQuests.isEmpty()) {
            ItemStack noQuests = new ItemStack(Material.BARRIER);
            ItemMeta meta = noQuests.getItemMeta();
            if (meta != null) {
                meta.setDisplayName("§7No quests to display.");
                noQuests.setItemMeta(meta);
            }
            gui.setItem(13, noQuests);
        } else {
            for (int i = 0; i < pageQuests.size(); i++) {
                if (i < QUEST_SLOTS.length) {
                    int slot = QUEST_SLOTS[i];
                    gui.setItem(slot, QuestItemFactory.createQuestItem(pageQuests.get(i)));
                    slotMap.put(slot, pageQuests.get(i));
                }
            }
        }

        slotQuestMap.put(player.getUniqueId(), slotMap);

        gui.setItem(27, page > 0 ? NavItemFactory.createNavItem(Material.ARROW, "Previous Page") : NavItemFactory.createNavItem(Material.RED_STAINED_GLASS_PANE, "No Previous Page"));
        gui.setItem(28, NavItemFactory.createNavItem(Material.BARRIER, "§cClose Menu"));
        gui.setItem(35, page < getMaxPages(player.getUniqueId(), tier, filter) - 1 ? NavItemFactory.createNavItem(Material.ARROW, "Next Page") : NavItemFactory.createNavItem(Material.RED_STAINED_GLASS_PANE, "No Next Page"));
        gui.setItem(31, NavItemFactory.createNavItem(Material.HOPPER, "Filter: " + filter.name()));
        gui.setItem(29, tier == QuestTier.DAILY ? QuestItemFactory.glowing(NavItemFactory.createNavItem(Material.EMERALD, "Daily Quests")) : NavItemFactory.createNavItem(Material.EMERALD, "Daily Quests"));
        gui.setItem(30, tier == QuestTier.WEEKLY ? QuestItemFactory.glowing(NavItemFactory.createNavItem(Material.DIAMOND, "Weekly Quests")) : NavItemFactory.createNavItem(Material.DIAMOND, "Weekly Quests"));
        gui.setItem(32, tier == QuestTier.GLOBAL ? QuestItemFactory.glowing(NavItemFactory.createNavItem(Material.NETHER_STAR, "Global Quests")) : NavItemFactory.createNavItem(Material.NETHER_STAR, "Global Quests"));
        gui.setItem(33, tier == QuestTier.ALL ? QuestItemFactory.glowing(NavItemFactory.createNavItem(Material.BOOK, "All Quests")) : NavItemFactory.createNavItem(Material.BOOK, "All Quests"));

        slotQuestMap.put(player.getUniqueId(), slotMap);
        player.openInventory(gui);
        ShimmeringBorderHandler.startShimmeringBorder(plugin, player, gui);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;
        Map<Integer, Quest> questMap = slotQuestMap.getOrDefault(player.getUniqueId(), Collections.emptyMap());
        if (!event.getView().getTitle().contains("Quests")) return;

        event.setCancelled(true);

        int slot = event.getRawSlot();

        if (slotQuestMap.get(event.getWhoClicked().getUniqueId()) == null) {
            open(player, pageMap.getOrDefault(player.getUniqueId(), 0), tierMap.get(player.getUniqueId()), QuestFilter.ALL);
        }

        questMap = slotQuestMap.get(player.getUniqueId());
        if (questMap.containsKey(slot)) {
            Quest quest = questMap.get(slot);
            plugin.debug("[GUI] Player clicked quest: " + quest.getId() + " | canClaim=" + quest.canClaim());

            if (quest.canClaim()) {
                try {
                    plugin.getRewardHandler().giveReward(player, quest, true);
                    plugin.getQuestNotifier().notifyCompletion(player, quest);
                    plugin.getQuestStorage().savePlayerQuests(
                            player.getUniqueId(),
                            plugin.getQuestManager().getPlayerDailyQuests(player.getUniqueId()),
                            plugin.getQuestManager().getPlayerWeeklyQuests(player.getUniqueId())
                    );
                } catch (Exception e) {
                    e.printStackTrace();
                    player.sendMessage("§cAn error occurred while claiming the quest.");
                }
                open(player, pageMap.getOrDefault(player.getUniqueId(), 0), tierMap.get(player.getUniqueId()), filterMap.get(player.getUniqueId()));
            } else {
                if (quest.isCompleted()) {
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

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        ShimmeringBorderHandler.stopShimmering(event.getPlayer().getUniqueId());
    }

    private String getGuiTitle(QuestTier tier, int page) {
        return switch (tier) {
            case DAILY -> "§a§lDaily Quests §7(Page " + (page + 1) + ")";
            case WEEKLY -> "§9§lWeekly Quests §7(Page " + (page + 1) + ")";
            case GLOBAL -> "§d§lGlobal Quests §7(Page " + (page + 1) + ")";
            case ALL -> "§e§lAll Quests §7(Page " + (page + 1) + ")";
            default -> throw new IllegalArgumentException("Unknown tier: " + tier);
        };
    }

    private List<Quest> getPageQuests(UUID uuid, int page, QuestTier tier, QuestFilter filter) {
        List<Quest> allQuests = plugin.getQuestManager().getQuestsForTier(uuid, tier);
        List<Quest> filteredQuests = filter.apply(allQuests);
        
        int questsPerPage = QUEST_SLOTS.length;
        int startIndex = page * questsPerPage;
        int endIndex = Math.min(startIndex + questsPerPage, filteredQuests.size());
        
        if (startIndex >= filteredQuests.size()) {
            return Collections.emptyList();
        }
        
        return filteredQuests.subList(startIndex, endIndex);
    }

    

    private int getMaxPages(UUID uuid, QuestTier tier, QuestFilter filter) {
        List<Quest> allQuests = plugin.getQuestManager().getQuestsForTier(uuid, tier);
        
        if (allQuests.isEmpty()) {
            return 1;
        }
        
        int filteredCount = (int) filter.apply(allQuests).stream().count();
        
        return (int) Math.ceil((double) filteredCount / QUEST_SLOTS.length);
    }
}
