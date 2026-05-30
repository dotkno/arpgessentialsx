package com.ahren.arpgessentialsx.armors.setbonus;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Set bonus that reduces damage taken by a percentage.
 *
 * yml params:
 *   amount: 0.15   (15% damage reduction)
 *   damage_type: "all" or specific type (future expansion)
 *
 * Trigger: Set completion (2 or 4 pieces)
 */
public final class DamageReductionBonus implements ArmorSetBonus {

    @Override
    public void apply(Player player, ConfigurationSection config, int pieces, String setName) {
        double amount = config.getDouble("amount", 0.15);
        
        // Use composite key: setName_pieces (e.g., "Dragon_2pc")
        String modifierName = setName + "_" + pieces + "pc_damage_reduction";
        
        // Apply as armor bonus (simplified - actual damage reduction would need event handling)
        NamespacedKey key = new NamespacedKey("arpgessentialsx", modifierName);
        
        // Remove existing modifier with same key before adding new one
        player.getAttribute(Attribute.GENERIC_ARMOR).getModifiers().removeIf(
                modifier -> modifier.getKey().equals(key));
        
        player.getAttribute(Attribute.GENERIC_ARMOR).addModifier(new AttributeModifier(
                key,
                amount * 10, // Scale to armor points
                AttributeModifier.Operation.ADD_NUMBER));
    }

    @Override
    public void remove(Player player, ConfigurationSection config, int pieces, String setName) {
        // Use composite key: setName_pieces (e.g., "Dragon_2pc")
        String modifierName = setName + "_" + pieces + "pc_damage_reduction";
        
        player.getAttribute(Attribute.GENERIC_ARMOR).getModifiers().removeIf(
                modifier -> modifier.getKey().equals(new NamespacedKey("arpgessentialsx", modifierName)));
    }

    @Override
    public String getType() {
        return "damage_reduction";
    }
}
