package com.example.questplugin.commands;

import com.example.questplugin.QuestPlugin;
import com.example.questplugin.model.QuestTemplate;

import net.kyori.adventure.audience.Audience;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;

import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
public class QuestStatsCommand implements CommandExecutor, TabCompleter {
    private final QuestPlugin plugin;
    private final MiniMessage miniMessage = MiniMessage.miniMessage();

    public QuestStatsCommand(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, org.bukkit.command.Command command, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage("This command can only be used by players.");
            return false;
        }

        Audience player = (Audience) sender;

        if (args.length < 1) {
            Component parsed = miniMessage.deserialize(" Usage: /queststats <quest_name>");
            player.sendMessage(parsed);
            return false;
        }

        String questIdToCheck = args[0];

        // Validate the quest ID exists in templates
        List<QuestTemplate> templates = plugin.getQuestLoader().getAllTemplates();
        Set<String> validQuestIds = templates.stream()
                .map(QuestTemplate::getId)
                .collect(Collectors.toSet());

        if (validQuestIds.isEmpty()) {
            player.sendMessage(miniMessage.deserialize("No quests found in the system!"));
            return false;
        }

        String lowerCaseQuestId = questIdToCheck.toLowerCase();
        if (!validQuestIds.contains(lowerCaseQuestId)) {
            player.sendMessage(miniMessage.deserialize("Quest ID not found or invalid!"));
            return false;
        }

        // Check if the quest has any completion data
        if (!plugin.getCompletionConfig().isSet(questIdToCheck)) {
            player.sendMessage(miniMessage.deserialize("No completion data found for this quest!"));
            return false;
        }

        String questId = lowerCaseQuestId; // Use lowercase for consistency
        int completion = plugin.getCompletionConfig().getInt(questId + ".completions");
        long playersCompleted = plugin.getCompletionConfig()
                .getConfigurationSection(questId + ".players")
                .getValues(false)
                .size();
        Component message = miniMessage.deserialize(
            "Quest Stats:\n" +
            "Total Completions: " + completion + "\n" +
            "Players Completed: " + playersCompleted + "\n"
        );

        if (sender instanceof Player) {
            player.sendMessage(message);
        } else {
            sender.sendMessage(message.toString());
    }

        return true;
        }

    @Override
    public List<String> onTabComplete(CommandSender player, Command command, String alias, String[] args) {
        if (args.length != 1) {
            return Collections.emptyList();
        }

        // Get all quest IDs that have been completed and valid quests from templates
        Set<String> completedQuestIds = new HashSet<>();
        for (String key : plugin.getCompletionConfig().getKeys(false)) {
            if (key.matches("^[a-zA-Z_]+$")) {
                completedQuestIds.add(key.toLowerCase());
            }
        }

        // Get all available quests from templates
        List<String> allQuestIds = new ArrayList<>();
        try {
            List<QuestTemplate> templates = plugin.getQuestLoader().getAllTemplates();
            for (QuestTemplate template : templates) {
                allQuestIds.add(template.getId().toLowerCase());
    }
        } catch (Exception e) {
            // Handle any potential errors in getting quest templates
            if (player instanceof Player) {
                Audience p = (Audience) player;
                p.sendMessage(miniMessage.deserialize("Error loading quest templates"));
}
            return Collections.emptyList();
        }

        String input = args[0].toLowerCase();

        // Merge completed quests and available templates, then filter by input
        List<String> allPossibleIds = new ArrayList<>();
        allPossibleIds.addAll(allQuestIds);
        allPossibleIds.addAll(completedQuestIds);

        // Remove duplicates while preserving order (Java 11+)
        List<String> uniqueSuggestions = allPossibleIds.stream()
                .distinct()
                .sorted()
                .limit(10)  // Limit to top 10 matches for performance
                .collect(Collectors.toList());

        return uniqueSuggestions.stream()
                .filter(q -> q.contains(input))
                .sorted()
                .limit(10)
                .collect(Collectors.toList());
    }
}
