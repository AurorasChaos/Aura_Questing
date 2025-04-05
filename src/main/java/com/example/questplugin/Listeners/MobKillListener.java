package com.example.questplugin.Listeners;

import java.util.List; // Add this import statement

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.model.QuestType;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;

public class MobKillListener extends BaseListener implements Listener {

    public MobKillListener(QuestPlugin plugin) {
        super(plugin);
    }

    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (!(event.getEntity().getKiller() instanceof Player killer)) return;

        String type = event.getEntityType().name();
        plugin.debug("[Kill] " + killer.getName() + " killed " + type);

        handleQuestTypeAndTarget(QuestType.KILL_MOB, type, killer);
    }

    @Override
    protected String getEventType() {
        return "Mob Kill";
    }
}