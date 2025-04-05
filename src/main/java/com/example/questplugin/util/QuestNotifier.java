package com.example.questplugin.util;

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.model.Quest;
import com.example.questplugin.model.QuestTemplate;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.format.NamedTextColor;
import org.bukkit.Sound;
import org.bukkit.entity.Player;

public class QuestNotifier {

    @SuppressWarnings("unused")
    private final QuestPlugin plugin;

    public QuestNotifier(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Notifies the player about quest progress using the action bar.
     * This triggers every 10% milestone (excluding 0% and 100%).
     */
    public void notifyProgress(Player player, QuestTemplate.Objective obj, Quest quest) {
        double percent = (double) obj.getProgress() / obj.getObjectiveTargetAmount();
        int percentage = (int) (percent * 100);

        if (percentage % 10 == 0 && percentage != 0 && !quest.isCompleted()) {
            Component actionBar = Component.text()
                    .append(Component.text("Objective: ", NamedTextColor.YELLOW))
                    .append(Component.text(quest.getDescription(), NamedTextColor.GOLD))
                    .append(Component.text(" [" + percentage + "%]", NamedTextColor.YELLOW))
                    .build();

            plugin.adventure().player(player).sendActionBar(actionBar);
            player.playSound(player.getLocation(), Sound.ENTITY_EXPERIENCE_ORB_PICKUP, 1f, 1.2f);
        }
    }

    /**
     * Shows a title to the player when a quest is completed.
     */
    public void notifyCompletion(Player player, Quest quest) {
        player.sendTitle("✔ Quest Complete!", null, 5, 30, 5);
        player.playSound(player.getLocation(), Sound.UI_TOAST_CHALLENGE_COMPLETE, 1f, 1f);
    }
}