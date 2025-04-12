package com.example.questplugin.events;

import org.bukkit.entity.Player;
import org.bukkit.event.Event;
import org.bukkit.event.HandlerList;

import com.example.questplugin.model.Quest;
public class QuestCompleteEvent extends Event {

    private static final HandlerList handlers = new HandlerList();
    private final Player player;
    private final Quest quest;

    public QuestCompleteEvent(Player player, Quest quest) {
        this.player = player;
        this.quest = quest;
    }

    public Player getPlayer() {
        return player;
    }

    public Quest getQuest() {
        return quest;
    }

    @Override
    public HandlerList getHandlers() {
        return handlers;
    }

    public static HandlerList getHandlerList() {
        return handlers;
    }
}
