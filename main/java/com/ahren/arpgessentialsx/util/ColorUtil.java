package com.ahren.arpgessentialsx.util;

import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

public final class ColorUtil {

    private ColorUtil() {}

    public static String translate(String text) {
        if (text == null) return "";
        return ChatColor.translateAlternateColorCodes('&', text);
    }

    public static void sendActionBar(Player player, String message) {
        player.sendActionBar(
                LegacyComponentSerializer.legacyAmpersand()
                        .deserialize(translate(message)));
    }
}