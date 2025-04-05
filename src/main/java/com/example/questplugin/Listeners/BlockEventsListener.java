package com.example.questplugin.Listeners;

import java.util.List; // Add this import statement

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.model.QuestType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.event.block.BlockPlaceEvent;

public class BlockEventsListener extends BaseListener implements Listener {

    public BlockEventsListener(QuestPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        String blockType = event.getBlock().getType().name();
        plugin.debug("[BlockBreak] " + player.getName() + " broke " + blockType);

        handleQuestTypeAndTarget(QuestType.MINE_BLOCK, blockType, player);
    }

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;

        String blockType = event.getBlock().getType().name();
        plugin.debug("[BlockPlace] " + player.getName() + " placed " + blockType);

        handleQuestTypeAndTarget(QuestType.PLACE_BLOCK, blockType, player);
    }

    @Override
    protected String getEventType() {
        return "Block Events";
    }
}