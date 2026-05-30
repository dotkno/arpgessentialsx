package com.ahren.arpgessentialsx.armors.passives.utility;

import com.ahren.arpgessentialsx.armors.passives.ArmorPassive;
import com.ahren.arpgessentialsx.armors.passives.ArmorPassiveContext;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

/**
 * Applies a permanent movement speed boost when the armor is equipped.
 *
 * yml params:
 *   amount: 0.1   (speed multiplier, default 0.1 = 10% boost)
 *
 * Trigger: ON_EQUIP, ON_UNEQUIP (state-tracking)
 */
public final class SpeedBoostPassive implements ArmorPassive {

    @Override
    public Trigger getTrigger() {
        return Trigger.ON_EQUIP;
    }

    @Override
    public void apply(ArmorPassiveContext ctx) {
        // Event-driven logic (if needed)
    }

    @Override
    public void onEquip(Player player, ConfigurationSection config) {
        double amount = config.getDouble("amount", 0.1);
        String armorId = config.getString("armor_id", "unknown");
        NamespacedKey key = new NamespacedKey("arpgessentialsx", "armor_speed_boost_" + armorId);
        
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
                .addModifier(new AttributeModifier(
                        key,
                        amount,
                        AttributeModifier.Operation.ADD_SCALAR));
    }

    @Override
    public void onUnequip(Player player, ConfigurationSection config) {
        String armorId = config.getString("armor_id", "unknown");
        NamespacedKey key = new NamespacedKey("arpgessentialsx", "armor_speed_boost_" + armorId);
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED)
                .getModifiers().removeIf(modifier -> modifier.getKey().equals(key));
    }
}
