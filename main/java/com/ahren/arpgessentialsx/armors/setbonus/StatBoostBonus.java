package com.ahren.arpgessentialsx.armors.setbonus;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Set bonus that boosts a player stat when the set is completed.
 *
 * yml params:
 *   stat: "max_health" or "armor" or "armor_toughness"
 *   amount: 2.0   (amount to boost the stat by)
 *
 * Trigger: Set completion (2 or 4 pieces)
 */
public final class StatBoostBonus implements ArmorSetBonus {

    @Override
    public void apply(Player player, ConfigurationSection config, int pieces, String setName) {
        String stat = config.getString("stat", "max_health");
        double amount = config.getDouble("amount", 2.0);

        Attribute attribute = switch (stat.toLowerCase()) {
            case "max_health" -> Attribute.GENERIC_MAX_HEALTH;
            case "armor" -> Attribute.GENERIC_ARMOR;
            case "armor_toughness" -> Attribute.GENERIC_ARMOR_TOUGHNESS;
            case "movement_speed" -> Attribute.GENERIC_MOVEMENT_SPEED;
            case "attack_damage" -> Attribute.GENERIC_ATTACK_DAMAGE;
            case "attack_speed" -> Attribute.GENERIC_ATTACK_SPEED;
            default -> null;
        };

        if (attribute != null) {
            // Use composite key: setName_pieces (e.g., "Dragon_2pc")
            NamespacedKey key = new NamespacedKey("arpgessentialsx", setName + "_" + pieces + "pc_" + stat);
            
            // For max_health, store current health percentage before applying
            double healthPercentage = 0.0;
            if (attribute == Attribute.GENERIC_MAX_HEALTH) {
                double currentMax = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                double currentHealth = player.getHealth();
                healthPercentage = currentHealth / currentMax;
            }
            
            // Remove existing modifier with same key before adding new one
            player.getAttribute(attribute).getModifiers().removeIf(
                    modifier -> modifier.getKey().equals(key));
            
            player.getAttribute(attribute).addModifier(new AttributeModifier(
                    key,
                    amount,
                    AttributeModifier.Operation.ADD_NUMBER));
            
            // For max_health, restore health percentage
            if (attribute == Attribute.GENERIC_MAX_HEALTH) {
                double newMax = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                double newHealth = newMax * healthPercentage;
                player.setHealth(Math.min(newHealth, newMax));
            }
        }
    }

    @Override
    public void remove(Player player, ConfigurationSection config, int pieces, String setName) {
        String stat = config.getString("stat", "max_health");

        System.out.println("[StatBoostBonus] Removing bonus - set: " + setName + ", stat: " + stat + ", pieces: " + pieces);

        Attribute attribute = switch (stat.toLowerCase()) {
            case "max_health" -> Attribute.GENERIC_MAX_HEALTH;
            case "armor" -> Attribute.GENERIC_ARMOR;
            case "armor_toughness" -> Attribute.GENERIC_ARMOR_TOUGHNESS;
            case "movement_speed" -> Attribute.GENERIC_MOVEMENT_SPEED;
            case "attack_damage" -> Attribute.GENERIC_ATTACK_DAMAGE;
            case "attack_speed" -> Attribute.GENERIC_ATTACK_SPEED;
            default -> null;
        };

        if (attribute != null) {
            // For max_health, store current health percentage before removing
            double healthPercentage = 0.0;
            if (attribute == Attribute.GENERIC_MAX_HEALTH) {
                double currentMax = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                double currentHealth = player.getHealth();
                healthPercentage = currentHealth / currentMax;
            }
            
            // Use composite key: setName_pieces (e.g., "Dragon_2pc")
            NamespacedKey key = new NamespacedKey("arpgessentialsx", setName + "_" + pieces + "pc_" + stat);
            System.out.println("[StatBoostBonus] Looking for modifier with key: " + key.toString());
            
            boolean removed = player.getAttribute(attribute).getModifiers().removeIf(
                    modifier -> modifier.getKey().equals(key));
            
            System.out.println("[StatBoostBonus] Modifier removed: " + removed);
            
            // For max_health, restore health percentage and cap at new max
            if (attribute == Attribute.GENERIC_MAX_HEALTH) {
                double newMax = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                double newHealth = newMax * healthPercentage;
                player.setHealth(Math.min(newHealth, newMax));
            }
        }
    }

    @Override
    public String getType() {
        return "stat_boost";
    }
}
