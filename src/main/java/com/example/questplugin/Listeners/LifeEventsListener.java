package com.example.questplugin.Listeners;

import java.util.HashMap;
import java.util.UUID;

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.model.Quest;
import com.example.questplugin.model.QuestTier;
import com.example.questplugin.model.QuestType;
import com.example.questplugin.util.QuestNotifier;
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

    public LifeEventsListener(QuestPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onAnimalBreed(EntityBreedEvent event) {
        if (!(event.getBreeder() instanceof Player player)) return;

        String type = event.getEntityType().name();
        plugin.debug("[Breed] " + player.getName() + " bred a " + type);
        handleQuestTypeAndTarget(QuestType.BREED_ANIMAL, type, player);
    }

    @EventHandler
    public void onEntityTame(EntityTameEvent event) {
        if (!(event.getOwner() instanceof Player player)) return;

        String type = event.getEntityType().name();
        plugin.debug("[Tame] " + player.getName() + " tamed a " + type);
        handleQuestTypeAndTarget(QuestType.TAME_ENTITY, type, player);
    }

    @EventHandler
    public void onCraft(org.bukkit.event.inventory.CraftItemEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        String item = event.getRecipe().getResult().getType().name();
        plugin.debug("[Craft] " + player.getName() + " crafted " + item);
        handleQuestTypeAndTarget(QuestType.CRAFT_ITEM, item, player);
    }

    @EventHandler
    public void onBiomeEnter(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        Location from = event.getFrom();
        Location to = event.getTo();

        if (to == null || from.getBlockX() == to.getBlockX()
                && from.getBlockY() == to.getBlockY()
                && from.getBlockZ() == to.getBlockZ()) return;

        Biome previousBiome = from.getBlock().getBiome();
        Biome newBiome = to.getBlock().getBiome();

        if (previousBiome == newBiome) return;

        UUID uuid = player.getUniqueId();
        String biomeName = newBiome.name();

        plugin.debug("[BiomeVisit] " + player.getName() + " entered biome: " + biomeName);
        handleQuestTypeAndTarget(QuestType.EXPLORE_BIOME, biomeName, player);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        if (event.getInventory().getType() == InventoryType.MERCHANT) {
            plugin.debug(player.getName() + " interacted with a Villager trade.");

            String target = "VILLAGER";
            // Optional: You can track the trade slot
            int slot = event.getRawSlot();
            if (slot == 2 && event.getCurrentItem() != null) { // Result slot of the trade
                plugin.debug(player.getName() + " completed a trade!");
                handleQuestTypeAndTarget(QuestType.TRADE, target, player);
            }
        }
    }

    @EventHandler
    public void onBrew(BrewEvent event) {
        if (!(event.getBlock().getState() instanceof BrewingStand)) return;

        BrewingStand stand = (BrewingStand) event.getBlock().getState();
        //Need to fix how this quest works, currently broken
    }
    @EventHandler
    public void onEnchant(EnchantItemEvent event) {
        Player player = event.getEnchanter();
        handleQuestTypeAndTarget(QuestType.ENCHANT_ITEM, "", player);
        //Need to verify if this even works
    }

    @EventHandler
    public void onConsume(PlayerItemConsumeEvent event) {
        var player = event.getPlayer();
        String itemName = event.getItem().getType().name();

        plugin.debug("[ConsumeItem] " + player.getName() + " consumed " + itemName);
        handleQuestTypeAndTarget(QuestType.CONSUME_ITEM, itemName, player);
    }

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
        int stepsToApply = (int) total; // 1 block = 1 progress unit
        if (stepsToApply < 1) return;

        walkProgress.put(uuid, total % 1.0);

        // Handle Player Quests
        for (Quest quest : questManager.getPlayerQuests(player.getUniqueId())){
            if (!quest.isCompleted()){
                quest.getQuestObjectives().forEach(obj -> {
                    if (obj.getObjectiveType() == QuestType.WALK_DISTANCE){
                        incrementProgressAndNotify(player, obj, quest);
                        plugin.debug("[WALK_DISTANCE] Updated progress for " + quest.getTier() +  " quest " + quest.getId() + ": " + obj.getProgress());
                    }
                });
            }
        }

        // Handle global quests
        for (Quest quest : questManager.getGlobalQuests()){
            if (!quest.isCompleted()){
                quest.getQuestObjectives().forEach(obj -> {
                    if (obj.getObjectiveType() == QuestType.WALK_DISTANCE){
                        incrementProgressAndNotify(player, obj, quest);
                        plugin.debug("WALK_DISTANCE] Updated progress for global quest " + quest.getId() + ": " + obj.getProgress());
                    }
                });
            }
        }
    }
    protected String getEventType(){
        return "List Events";
    }

}