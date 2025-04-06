package com.example.questplugin.model;

import com.auradev.universalscoreboard.SidebarFrame;
import com.auradev.universalscoreboard.SidebarSection;
import com.example.questplugin.QuestPlugin;
import com.example.questplugin.managers.LeaderboardManager;
import org.bukkit.entity.Player;

import java.util.*;

public class QuestLeaderboardSection implements SidebarSection {

    @Override
    public String getId() {
        return "quest_leaderboard";
    }

    @Override
    public int getPriority() {
        return 10;
    }

    @Override
    public boolean shouldShow(Player player) {
        return true;
    }

    @Override
    public SidebarFrame getFrame(Player player) {
        return (Player p) -> {
            QuestPlugin.getInstance().debug("[Leaderboard] Loading frame section");
            List<String> lines = new ArrayList<>();
            lines.add("§6§lTop Questers");

            LeaderboardManager leaderboard = QuestPlugin.getInstance().getLeaderboardManager();
            Map<String, Integer> top = leaderboard.getTopPlayers(5);
            int rank = 1;

            for (Map.Entry<String, Integer> entry : top.entrySet()) {
                String name = entry.getKey();
                int score = entry.getValue();
                lines.add("§e" + rank++ + ". §a" + name + " §7- §b" + score + " pts");
            }

            UUID playerId = p.getUniqueId();
            int personalRank = leaderboard.getRank(playerId);
            int personalScore = leaderboard.getScore(playerId);

            lines.add("§8────────────");
            lines.add("§7Your Rank: §b" + (personalRank == -1 ? "Get Questing" : "#" + personalRank));
            lines.add("§7Your Score: §a" + personalScore);

            return lines;
        };
    }
}
