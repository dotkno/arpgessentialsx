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
 * Set bonus that grants damage and defense stacks when hitting from behind or debuffed enemies.
 *
 * yml params:
 *   max_stacks: 2
 *   damage_per_stack: 0.09   (9% damage per stack)
 *   defense_per_stack: 0.09   (9% defense per stack)
 *   stack_duration: 8.0   (seconds)
 *
 * Trigger: 4-piece set completion, grants shadow stacks
 */
public final class ShadowStacksBonus implements ArmorSetBonus {

    private static final Map<UUID, Integer> playerStacks = new HashMap<>();
    private static final Map<UUID, Long> lastStackTime = new HashMap<>();

    @Override
    public void apply(Player player, ConfigurationSection config, int pieces, String setName) {
        playerStacks.put(player.getUniqueId(), 0);
    }

    @Override
    public void remove(Player player, ConfigurationSection config, int pieces, String setName) {
        UUID uuid = player.getUniqueId();
        playerStacks.remove(uuid);
        lastStackTime.remove(uuid);
        
        // Use composite key: setName_pieces (e.g., "Dragon_2pc")
        NamespacedKey dmgKey = new NamespacedKey("arpgessentialsx", setName + "_" + pieces + "pc_shadow_dmg");
        NamespacedKey defKey = new NamespacedKey("arpgessentialsx", setName + "_" + pieces + "pc_shadow_def");
        
        player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getModifiers()
            .removeIf(modifier -> modifier.getKey().equals(dmgKey));
        player.getAttribute(Attribute.GENERIC_ARMOR).getModifiers()
            .removeIf(modifier -> modifier.getKey().equals(defKey));
    }

    @Override
    public String getType() {
        return "shadow_stacks";
    }

    public static void onConditionMet(Player player, ConfigurationSection config, String setName, boolean fromBehind, boolean debuffed) {
        UUID uuid = player.getUniqueId();
        if (!playerStacks.containsKey(uuid)) return;

        if (fromBehind || debuffed) {
            int maxStacks = config.getInt("max_stacks", 2);
            int currentStacks = playerStacks.get(uuid);
            
            if (currentStacks < maxStacks) {
                playerStacks.put(uuid, currentStacks + 1);
                lastStackTime.put(uuid, System.currentTimeMillis());
                applyModifiers(player, config, playerStacks.get(uuid), setName);
            }
        }
    }

    public static void decayStacks(Player player, ConfigurationSection config, String setName) {
        UUID uuid = player.getUniqueId();
        if (!playerStacks.containsKey(uuid)) return;

        double duration = config.getDouble("stack_duration", 8.0) * 1000;
        Long lastTime = lastStackTime.get(uuid);
        
        if (lastTime != null && System.currentTimeMillis() - lastTime > duration) {
            int currentStacks = playerStacks.get(uuid);
            if (currentStacks > 0) {
                playerStacks.put(uuid, currentStacks - 1);
                lastStackTime.put(uuid, System.currentTimeMillis());
                applyModifiers(player, config, playerStacks.get(uuid), setName);
            }
        }
    }

    private static void applyModifiers(Player player, ConfigurationSection config, int stacks, String setName) {
        int pieces = 4;
        double dmgPerStack = config.getDouble("damage_per_stack", 0.09);
        double defPerStack = config.getDouble("defense_per_stack", 0.09);

        // Use composite key: setName_pieces (e.g., "Dragon_2pc")
        NamespacedKey dmgKey = new NamespacedKey("arpgessentialsx", setName + "_" + pieces + "pc_shadow_dmg");
        NamespacedKey defKey = new NamespacedKey("arpgessentialsx", setName + "_" + pieces + "pc_shadow_def");

        // Remove old modifiers
        player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getModifiers()
            .removeIf(modifier -> modifier.getKey().equals(dmgKey));
        player.getAttribute(Attribute.GENERIC_ARMOR).getModifiers()
            .removeIf(modifier -> modifier.getKey().equals(defKey));

        // Apply new modifiers
        if (stacks > 0) {
            double baseDmg = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).getBaseValue();
            double baseDef = player.getAttribute(Attribute.GENERIC_ARMOR).getBaseValue();
            
            player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE).addModifier(new AttributeModifier(
                dmgKey, baseDmg * dmgPerStack * stacks, AttributeModifier.Operation.ADD_NUMBER, org.bukkit.inventory.EquipmentSlotGroup.ANY));
            player.getAttribute(Attribute.GENERIC_ARMOR).addModifier(new AttributeModifier(
                defKey, baseDef * defPerStack * stacks, AttributeModifier.Operation.ADD_NUMBER, org.bukkit.inventory.EquipmentSlotGroup.ANY));
        }
    }

    public static void clearPlayer(UUID uuid) {
        playerStacks.remove(uuid);
        lastStackTime.remove(uuid);
    }
}
