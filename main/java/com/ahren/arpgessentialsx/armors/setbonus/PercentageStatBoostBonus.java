package com.ahren.arpgessentialsx.armors.setbonus;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Set bonus that boosts a player stat by a percentage when the set is completed.
 *
 * yml params:
 *   stat: "max_health" or "armor" or "armor_toughness" or "movement_speed" or "attack_damage" or "attack_speed" or "crit_damage"
 *   percentage: 0.10   (10% boost)
 *
 * Trigger: Set completion (2 or 4 pieces)
 */
public final class PercentageStatBoostBonus implements ArmorSetBonus {

    @Override
    public void apply(Player player, ConfigurationSection config, int pieces, String setName) {
        String stat = config.getString("stat", "max_health");
        double percentage = config.getDouble("percentage", 0.10);

        // Handle crit damage specially since there's no GENERIC_CRIT_DAMAGE attribute
        if (stat.equalsIgnoreCase("crit_damage") || stat.equalsIgnoreCase("crit_multiplier")) {
            // Store crit damage bonus in a custom attribute (using attack_damage as a carrier)
            // The key will contain "crit" so it can be identified by the HUD
            Attribute attribute = Attribute.GENERIC_ATTACK_DAMAGE;
            NamespacedKey key = new NamespacedKey("arpgessentialsx", setName + "_" + pieces + "pc_crit_damage_pct");
            
            // Remove existing modifier with same key before adding new one
            player.getAttribute(attribute).getModifiers().removeIf(
                    modifier -> modifier.getKey().equals(key));
            
            // Use ADD_NUMBER to add the percentage as a flat bonus to attack damage
            // This will be detected by the HUD as a crit modifier
            player.getAttribute(attribute).addModifier(new AttributeModifier(
                    key,
                    percentage,
                    AttributeModifier.Operation.ADD_NUMBER,
                    org.bukkit.inventory.EquipmentSlotGroup.ANY));
            return;
        }

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
            double baseValue = player.getAttribute(attribute).getBaseValue();
            double bonusAmount = baseValue * percentage;
            // Use composite key: setName_pieces (e.g., "Dragon_2pc")
            NamespacedKey key = new NamespacedKey("arpgessentialsx", setName + "_" + pieces + "pc_" + stat + "_pct");
            
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
            
            // Use MULTIPLY_SCALAR_1 for percentage-based boosts (except max_health)
            // MULTIPLY_SCALAR_1 adds (1 + percentage) to the base value
            AttributeModifier.Operation operation;
            if (attribute == Attribute.GENERIC_MAX_HEALTH) {
                operation = AttributeModifier.Operation.ADD_NUMBER;
            } else {
                operation = AttributeModifier.Operation.MULTIPLY_SCALAR_1;
                bonusAmount = percentage; // For MULTIPLY_SCALAR_1, we use the percentage directly
            }
            
            player.getAttribute(attribute).addModifier(new AttributeModifier(
                    key,
                    bonusAmount,
                    operation,
                    org.bukkit.inventory.EquipmentSlotGroup.ANY));
            
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

        System.out.println("[PercentageStatBoostBonus] Removing bonus - set: " + setName + ", stat: " + stat + ", pieces: " + pieces);

        // Handle crit damage specially
        if (stat.equalsIgnoreCase("crit_damage") || stat.equalsIgnoreCase("crit_multiplier")) {
            Attribute attribute = Attribute.GENERIC_ATTACK_DAMAGE;
            NamespacedKey key = new NamespacedKey("arpgessentialsx", setName + "_" + pieces + "pc_crit_damage_pct");
            System.out.println("[PercentageStatBoostBonus] Looking for crit damage modifier with key: " + key.toString());
            
            boolean removed = player.getAttribute(attribute).getModifiers().removeIf(
                    modifier -> modifier.getKey().equals(key));
            
            System.out.println("[PercentageStatBoostBonus] Crit damage modifier removed: " + removed);
            return;
        }

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
            NamespacedKey key = new NamespacedKey("arpgessentialsx", setName + "_" + pieces + "pc_" + stat + "_pct");
            System.out.println("[PercentageStatBoostBonus] Looking for modifier with key: " + key.toString());
            
            boolean removed = player.getAttribute(attribute).getModifiers().removeIf(
                    modifier -> modifier.getKey().equals(key));
            
            System.out.println("[PercentageStatBoostBonus] Modifier removed: " + removed);
            
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
        return "percentage_stat_boost";
    }
}
