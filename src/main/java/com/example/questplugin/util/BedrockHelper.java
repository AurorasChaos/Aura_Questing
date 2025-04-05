package com.example.questplugin.util;

import org.bukkit.entity.Player;

public class BedrockHelper {

    public static boolean isBedrock(Player player) {
        return player.hasPermission("floodgate.is_bedrock");
    }
}
