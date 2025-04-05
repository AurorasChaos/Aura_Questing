package com.example.questplugin.commands;

import com.example.questplugin.util.QuestFilter;
import com.example.questplugin.ui.QuestGUI;
import com.example.questplugin.QuestPlugin;
import com.example.questplugin.model.QuestTier;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Command executor for the "quest" command.
 */
public class QuestCommand implements CommandExecutor {

    private final QuestPlugin plugin;

    /**
     * Constructor to initialize the command executor.
     *
     * @param plugin The main plugin instance.
     */
    public QuestCommand(QuestPlugin plugin) {
        this.plugin = plugin;
    }

    /**
     * Handles the execution of the "quest" command by players.
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
            sender.sendMessage("Only players can use this command.");
            return true;
        }

        player.sendMessage("Opening quests..."); // Debug message
        
        // Open the QuestGUI
        QuestGUI gui = new QuestGUI(plugin);
        gui.open(player, 1, QuestTier.DAILY, QuestFilter.ALL); // or switch tab logic
        return true;
    }
}