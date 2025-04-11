package com.example.questplugin.ui;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.Plugin;

import java.util.UUID;

public class ShimmeringBorderHandler {
    private static final Material[] shimmerColors = { Material.RED_STAINED_GLASS_PANE, Material.BLUE_STAINED_GLASS_PANE, Material.GREEN_STAINED_GLASS_PANE };
    private static final int[] shimmerSlots = { 0, 1, 2, 3, 4, 5, 6, 7, 8, 9, 17, 18, 26 };
    
    public static void startShimmeringBorder(Plugin plugin, Player player, Inventory gui) {
        UUID uuid = player.getUniqueId();
        stopShimmering(uuid);
        int taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, new Runnable() {
            int frame = 0;
            @Override
            public void run() {
                Material mat = shimmerColors[frame % shimmerColors.length];
                ItemStack pane = new ItemStack(mat);
                ItemMeta meta = pane.getItemMeta();
                if (meta != null) {
                    meta.setDisplayName(ChatColor.RESET.toString());
                    pane.setItemMeta(meta);
                }
                for (int slot : shimmerSlots) {
                    if (slot < gui.getSize()) {
                        gui.setItem(slot, pane);
                    }
                }
                frame++;
            }
        }, 0L, 30L);
        TaskManager.addTask(uuid, taskId);
    }

    public static void stopShimmering(UUID uuid) {
        int taskId = TaskManager.getTaskId(uuid);
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
            TaskManager.removeTask(uuid);
        }
    }
}
