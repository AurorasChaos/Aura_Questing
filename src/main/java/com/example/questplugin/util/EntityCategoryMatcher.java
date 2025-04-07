/**
 * This utility class provides a method to match entity types based on category names.
 * It categorizes entities as hostile (monsters), passive (animals), bosses,
 * or undead, allowing for flexible entity targeting in quests or other plugin functionalities.
 */
package com.example.questplugin.util;

import org.bukkit.entity.EntityType;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Animals;

/**
 * The {@code EntityCategoryMatcher} class contains a single method, {@link #matches(String, String)},
 * which takes a target category (e.g., "HOSTILE", "PASSIVE", "BOSS", or "UNDEAD") and an entity type,
 * then returns a boolean indicating whether the given entity type matches the target category.
 */
public class EntityCategoryMatcher {

    /**
     * Matches the input entity type with the target category.
     *
     * @param targetKey The category to match against, e.g., "HOSTILE", "PASSIVE", "BOSS", or "UNDEAD".
     * @param inputEntityType The entity type to check for matching categories.
     * @return {@code true} if the given entity type matches the target category; {@code false} otherwise.
     */
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

    /**
     * Checks if the given {@link EntityType} is considered a boss in this context.
     *
     * @param type The {@link EntityType} to check for boss classification.
     * @return {@code true} if the given entity type is considered a boss; {@code false} otherwise.
     */
    private static boolean isBoss(EntityType type) {
        return type == EntityType.ENDER_DRAGON || type == EntityType.WITHER || type == EntityType.ELDER_GUARDIAN;
    }

    /**
     * Checks if the given {@link EntityType} is considered undead in this context.
     *
     * @param type The {@link EntityType} to check for undead classification.
     * @return {@code true} if the given entity type is considered undead; {@code false} otherwise.
     */
    private static boolean isUndead(EntityType type) {
        return switch (type) {
            case ZOMBIE, ZOMBIE_VILLAGER, HUSK,
                 SKELETON, STRAY, WITHER_SKELETON,
                 DROWNED, WITHER -> true;
            default -> false;
        };
    }
}