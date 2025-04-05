package com.example.questplugin.util;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Animals;

public class EntityCategoryMatcher {

    public static boolean matches(String targetKey, String inputEntityType) {
        if (targetKey == null || inputEntityType == null) return false;

        String target = targetKey.trim().toUpperCase();
        String input = inputEntityType.trim().toUpperCase();

        if (target.equals(input)) return true;

        EntityType entityType;
        try {
            entityType = EntityType.valueOf(input);
        } catch (IllegalArgumentException e) {
            return false;
        }

        Class<?> clazz = entityType.getEntityClass();
        if (clazz == null) return false;

        return switch (target) {
            case "HOSTILE" -> Monster.class.isAssignableFrom(clazz);
            case "PASSIVE" -> Animals.class.isAssignableFrom(clazz);
            case "BOSS" -> isBoss(entityType);
            case "UNDEAD" -> isUndead(entityType);
            default -> false;
        };
    }

    private static boolean isBoss(EntityType type) {
        return type == EntityType.ENDER_DRAGON || type == EntityType.WITHER || type == EntityType.ELDER_GUARDIAN;
    }

    private static boolean isUndead(EntityType type) {
        return switch (type) {
            case ZOMBIE, ZOMBIE_VILLAGER, HUSK,
                 SKELETON, STRAY, WITHER_SKELETON,
                 DROWNED, WITHER -> true;
            default -> false;
        };
    }
}