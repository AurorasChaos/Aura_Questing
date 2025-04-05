package com.example.questplugin.commands;

import com.example.questplugin.model.Quest;
import com.example.questplugin.QuestPlugin;
import com.example.questplugin.model.QuestTemplate;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

@SuppressWarnings("unused")
public class DevCommands implements CommandExecutor {

    private final QuestPlugin plugin;

    /**
     * Constructor to initialize the command executor.
     *
     * @param plugin The main plugin instance.
     */
    public DevCommands(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the execution of commands by players.
     *
     * @param sender  The source of the command, either a player or console.
     * @param command The command which was executed.
     * @param label   The alias of the command which was used.
     * @param args    All arguments that were passed to the command.
     * @return true if the command was handled successfully, false otherwise.
     */
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage("Player only.");
            return true;
        }

        if (!player.hasPermission("questplugin.dev")) {
            player.sendMessage(ChatColor.RED + "No permission.");
            return true;
        }

        if (args.length == 0) {
            player.sendMessage(ChatColor.YELLOW + "/questdev reload - Reload quests");
            player.sendMessage(ChatColor.YELLOW + "/questdev give <quest_id> - Give quest");
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                plugin.getQuestLoader().loadTemplates();
                player.sendMessage(ChatColor.GREEN + "Reloaded quest templates.");
            }
            case "give" -> {
                if (args.length < 2) {
                    player.sendMessage(ChatColor.RED + "Usage: /questdev give <quest_id>");
                    return true;
                }
                String id = args[1];
                QuestTemplate template = plugin.getQuestLoader().getAllTemplates().stream()
                    .filter(q -> q.toQuest().getId().equalsIgnoreCase(id))
                    .findFirst().orElse(null);
                if (template == null) {
                    player.sendMessage(ChatColor.RED + "Quest not found.");
                    return true;
                }
                Quest quest = template.toQuest();
                plugin.getQuestManager().getPlayerDailyQuests(player.getUniqueId()).add(quest);
                player.sendMessage(ChatColor.GREEN + "Given quest: " + quest.getDescription());
            }
        }

        return true;
    }
}
