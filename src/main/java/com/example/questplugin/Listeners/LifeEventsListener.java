/**
 * Handles various life-related events that trigger quest progress updates, including breeding,
 * taming, crafting, exploring biomes, trading, enchanting, brewing, consuming items, and walking distance.
 */
package com.example.questplugin.Listeners;

import java.util.HashMap;
import java.util.UUID;

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.model.Quest;
import com.example.questplugin.model.QuestType;

import org.bukkit.Location;
import org.bukkit.block.Biome;
import org.bukkit.block.BrewingStand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.enchantment.EnchantItemEvent;
import org.bukkit.event.entity.EntityBreedEvent;
import org.bukkit.event.entity.EntityTameEvent;
import org.bukkit.event.inventory.BrewEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryType;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.event.player.PlayerMoveEvent;

public class LifeEventsListener extends BaseListener implements Listener {

    private final HashMap<UUID, Double> walkProgress = new HashMap<>();

    /**
     * Constructs the listener and binds it to the plugin.
     *
     * @param plugin The main plugin instance.
     */
    public LifeEventsListener(QuestPlugin plugin) {
        super(plugin);
    }

    /**
     * Triggered when a player breeds an animal.
     *
     * @param event The breeding event.
     */
    @EventHandler
    public void onAnimalBreed(EntityBreedEvent event) {
        if (!(event.getBreeder() instanceof Player player)) return;

        String type = event.getEntityType().name();
        plugin.debug("[Breed] " + player.getName() + " bred a " + type);
        handleQuestTypeAndTarget(QuestType.BREED_ANIMAL, type, player);
    }

    /**
     * Triggered when a player tames an entity.
     *
     * @param event The tame event.
     */
    @EventHandler
    public void onEntityTame(EntityTameEvent event) {
        if (!(event.getOwner() instanceof Player player)) return;

        String type = event.getEntityType().name();
        plugin.debug("[Tame] " + player.getName() + " tamed a " + type);
        handleQuestTypeAndTarget(QuestType.TAME_ENTITY, type, player);
    }

    /**
     * Triggered when a player crafts an item.
     *
     * @param event The crafting event.
     */
    @EventHandler
    public void onCraft(org.bukkit.event.inventory.CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String item = event.getRecipe().getResult().getType().name();
        plugin.debug("[Craft] " + player.getName() + " crafted " + item);
        handleQuestTypeAndTarget(QuestType.CRAFT_ITEM, item, player);
    }

    /**
     * Triggered when a player enters a new biome.
     *
     * @param event The movement event.
     */
    @EventHandler
    public void onBiomeEnter(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null || (from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ())) return;

        Biome previousBiome = from.getBlock().getBiome();
        Biome newBiome = to.getBlock().getBiome();

        if (previousBiome == newBiome) return;

        String biomeName = newBiome.name();
        plugin.debug("[BiomeVisit] " + player.getName() + " entered biome: " + biomeName);
        handleQuestTypeAndTarget(QuestType.EXPLORE_BIOME, biomeName, player);
    }

    /**
     * Triggered when a player interacts with a merchant trade inventory.
     *
     * @param event The inventory click event.
     */
    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (event.getInventory().getType() == InventoryType.MERCHANT) {
            plugin.debug(player.getName() + " interacted with a Villager trade.");

            String target = "VILLAGER";
            int slot = event.getRawSlot();
            if (slot == 2 && event.getCurrentItem() != null) {
                plugin.debug(player.getName() + " completed a trade!");
                handleQuestTypeAndTarget(QuestType.TRADE, target, player);
            }
        }
    }

    /**
     * Triggered when brewing finishes. Currently unused and needs fixing.
     *
     * @param event The brewing event.
     */
    @EventHandler
    public void onBrew(BrewEvent event) {
        if (!(event.getBlock().getState() instanceof BrewingStand)) return;
        // TODO: Fix brewing quest implementation
    }

    /**
     * Triggered when a player enchants an item.
     *
     * @param event The enchantment event.
     */
    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        handleQuestTypeAndTarget(QuestType.ENCHANT_ITEM, "", player);
        // TODO: Verify enchantment logic works correctly
    }

    /**
     * Triggered when a player consumes an item.
     *
     * @param event The item consumption event.
     */
    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        var player = event.getPlayer();
        String itemName = event.getItem().getType().name();

        plugin.debug("[ConsumeItem] " + player.getName() + " consumed " + itemName);
        handleQuestTypeAndTarget(QuestType.CONSUME_ITEM, itemName, player);
    }

    /**
     * Triggered on player movement. Tracks distance walked and updates relevant quests.
     *
     * @param event The move event.
     */
    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (event.getFrom().getBlockX() == event.getTo().getBlockX()
                && event.getFrom().getBlockY() == event.getTo().getBlockY()
                && event.getFrom().getBlockZ() == event.getTo().getBlockZ()) {
            return;
        }

        double distance = event.getFrom().distance(event.getTo());
        if (distance < 0.01) return;

        UUID uuid = player.getUniqueId();
        walkProgress.put(uuid, walkProgress.getOrDefault(uuid, 0.0) + distance);

        double total = walkProgress.get(uuid);
        int stepsToApply = (int) total;
        if (stepsToApply < 1) return;

        walkProgress.put(uuid, total % 1.0);

        // Update player quests
        for (Quest quest : questManager.getPlayerQuests(player.getUniqueId())) {
            if (!quest.isCompleted()) {
                quest.getQuestObjectives().forEach(obj -> {
                    if (obj.getType() == QuestType.WALK_DISTANCE) {
                        incrementProgressAndNotify(player, obj, quest);
                        plugin.debug("[WALK_DISTANCE] Updated progress for " + quest.getTier() + " quest " + quest.getId() + ": " + obj.getProgress());
                    }
                });
            }
        }

        // Update global quests
        for (Quest quest : questManager.getGlobalQuests()) {
            if (!quest.isCompleted()) {
                quest.getQuestObjectives().forEach(obj -> {
                    if (obj.getType() == QuestType.WALK_DISTANCE) {
                        incrementProgressAndNotify(player, obj, quest);
                        plugin.debug("[WALK_DISTANCE] Updated progress for global quest " + quest.getId() + ": " + obj.getProgress());
                    }
                });
            }
        }
    }

    /**
     * @return The listener category name for internal logging/debugging.
     */
    @Override
    protected String getEventType() {
        return "List Events";
    }
}
