package com.example.questplugin.ui;

import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import com.example.questplugin.model.Quest;
import com.example.questplugin.model.QuestTemplate;

import java.util.ArrayList;
import java.util.List;

public class QuestItemFactory {
    public static ItemStack createQuestItem(Quest quest) {
        Material mat = switch (quest.getRarity()) {
            case COMMON -> Material.PAPER;
            case RARE -> Material.MAP;
            case EPIC -> Material.ENCHANTED_BOOK;
            case LEGENDARY -> Material.NETHER_STAR;
            default -> throw new IllegalArgumentException("Unknown rarity: " + quest.getRarity());
        };
    
        ItemStack item = new ItemStack(mat);
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.setDisplayName(quest.getRarity().getColor() + quest.getDescription());
        List<String> lore = new ArrayList<>();
        lore.add("");
        for (QuestTemplate.Objective obj : quest.getQuestObjectives()) {
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

    public static ItemStack glowing(ItemStack item) {
        item.addUnsafeEnchantment(Enchantment.LURE, 1);
        ItemMeta meta = item.getItemMeta();
        if (meta != null) {
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
            item.setItemMeta(meta);
        }
        return item;
    }
}
