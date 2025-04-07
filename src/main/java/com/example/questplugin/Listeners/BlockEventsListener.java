package com.example.questplugin.Listeners; 

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.model.QuestType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

/**
 * A listener that handles block-related events such as {@link BlockBreakEvent} and {@link BlockPlaceEvent},
 * updating players' quest progress accordingly.
 */
public class BlockEventsListener extends BaseListener implements Listener {

  /**
   * Creates a new instance of BlockEventsListener with the given plugin instance.
   *
   * @param plugin the instance of QuestPlugin that this listener is part of
   */
    public BlockEventsListener(QuestPlugin plugin) {
        super(plugin);
    }

  /**
   * Handles the {@link BlockBreakEvent} by logging the event and updating players' quest progress for mining blocks.
   *
   * @param event the {@link BlockBreakEvent} to handle
   */
    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        String blockType = event.getBlock().getType().name();
        plugin.debug("[BlockBreak] " + player.getName() + " broke " + blockType);

        handleQuestTypeAndTarget(QuestType.MINE_BLOCK, blockType, player);
    }

  /**
   * Handles the {@link BlockPlaceEvent} by logging the event and updating players' quest progress for placing blocks.
   *
   * @param event the {@link BlockPlaceEvent} to handle
   */
    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        String blockType = event.getBlock().getType().name();
        plugin.debug("[BlockPlace] " + player.getName() + " placed " + blockType);

        handleQuestTypeAndTarget(QuestType.PLACE_BLOCK, blockType, player);
    }

  /**
   * Gets the type of events handled by this listener.
   *
   * @return the string "Block Events"
   */
    @Override
    protected String getEventType() {
        return "Block Events";
    }
}