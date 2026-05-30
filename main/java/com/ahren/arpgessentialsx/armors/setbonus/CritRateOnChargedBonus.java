package com.ahren.arpgessentialsx.armors.setbonus;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Set bonus that grants critical rate on charged/heavy ranged attacks.
 *
 * yml params:
 *   crit_rate_bonus: 0.30   (30% crit rate bonus)
 *   crit_damage_bonus: 0.40   (40% additional crit damage)
 *
 * Trigger: 4-piece set completion, enhances charged attacks
 */
public final class CritRateOnChargedBonus implements ArmorSetBonus {

    private static final Map<UUID, Boolean> activePlayers = new HashMap<>();

    @Override
    public void apply(Player player, ConfigurationSection config, int pieces, String setName) {
        activePlayers.put(player.getUniqueId(), true);
    }

    @Override
    public void remove(Player player, ConfigurationSection config, int pieces, String setName) {
        UUID uuid = player.getUniqueId();
        activePlayers.remove(uuid);
        
        // Use composite key: setName_pieces (e.g., "Dragon_2pc")
        NamespacedKey crKey = new NamespacedKey("arpgessentialsx", setName + "_" + pieces + "pc_charged_cr");
        NamespacedKey cdKey = new NamespacedKey("arpgessentialsx", setName + "_" + pieces + "pc_charged_cd");
        
        player.getAttribute(Attribute.GENERIC_LUCK).getModifiers()
            .removeIf(modifier -> modifier.getKey().equals(crKey));
        // Note: crit damage would need a custom attribute or implementation
    }

    @Override
    public String getType() {
        return "crit_rate_on_charged";
    }

    public static boolean isActive(UUID uuid) {
        return activePlayers.containsKey(uuid);
    }

    public static double getCritRateBonus(ConfigurationSection config) {
        return config.getDouble("crit_rate_bonus", 0.30);
    }

    public static double getCritDamageBonus(ConfigurationSection config) {
        return config.getDouble("crit_damage_bonus", 0.40);
    }

    public static void clearPlayer(UUID uuid) {
        activePlayers.remove(uuid);
    }
}
