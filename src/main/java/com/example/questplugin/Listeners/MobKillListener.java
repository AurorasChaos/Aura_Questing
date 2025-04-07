package com.example.questplugin.Listeners;

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.model.QuestType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * A listener that handles {@link EntityDeathEvent}s where a player is the killer,
 * updating players' quest progress accordingly for killing mobs.
 */

public class MobKillListener extends BaseListener implements Listener {

  /**
   * Creates a new instance of MobKillListener with the given plugin instance.
   *
   * @param plugin the instance of QuestPlugin that this listener is part of
   */
    public MobKillListener(QuestPlugin plugin) {
        super(plugin);
    }

  /**
   * Handles the {@link EntityDeathEvent} where a player is the killer by logging the event and updating players' quest progress for killing mobs.
   *
   * @param event the {@link EntityDeathEvent} to handle
   */
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player killer)) return;

        String type = event.getEntityType().name();
        plugin.debug("[Kill] " + killer.getName() + " killed " + type);
        handleQuestTypeAndTarget(QuestType.KILL_MOB, type, killer);
    }

  /**
   * Gets the type of events handled by this listener.
   *
   * @return the string "Mob Kill"
   */
    @Override
    protected String getEventType() {
        return "Mob Kill";
    }
}