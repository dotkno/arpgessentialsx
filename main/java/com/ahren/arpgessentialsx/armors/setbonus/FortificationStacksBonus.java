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
 * Set bonus that grants defense stacks when hit, but reduces movement speed.
 *
 * yml params:
 *   max_stacks: 4
 *   defense_per_stack: 0.06   (6% defense per stack)
 *   movement_speed_penalty_per_stack: 0.05   (5% slow per stack)
 *   stack_decay_time: 8.0   (seconds without damage before decay)
 *
 * Trigger: 4-piece set completion, grants fortification on hit
 */
public final class FortificationStacksBonus implements ArmorSetBonus {

    private static final Map<UUID, Integer> playerStacks = new HashMap<>();
    private static final Map<UUID, Long> lastHitTime = new HashMap<>();

    @Override
    public void apply(Player player, ConfigurationSection config, int pieces, String setName) {
        playerStacks.put(player.getUniqueId(), 0);
    }

    @Override
    public void remove(Player player, ConfigurationSection config, int pieces, String setName) {
        UUID uuid = player.getUniqueId();
        playerStacks.remove(uuid);
        lastHitTime.remove(uuid);
        
        // Use composite key: setName_pieces (e.g., "Dragon_2pc")
        NamespacedKey defKey = new NamespacedKey("arpgessentialsx", setName + "_" + pieces + "pc_fort_def");
        NamespacedKey msKey = new NamespacedKey("arpgessentialsx", setName + "_" + pieces + "pc_fort_ms");
        
        player.getAttribute(Attribute.GENERIC_ARMOR).getModifiers()
            .removeIf(modifier -> modifier.getKey().equals(defKey));
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getModifiers()
            .removeIf(modifier -> modifier.getKey().equals(msKey));
    }

    @Override
    public String getType() {
        return "fortification_stacks";
    }

    public static void onDamageTaken(Player player, ConfigurationSection config, String setName) {
        UUID uuid = player.getUniqueId();
        if (!playerStacks.containsKey(uuid)) return;

        int maxStacks = config.getInt("max_stacks", 4);
        int currentStacks = playerStacks.get(uuid);
        
        if (currentStacks < maxStacks) {
            playerStacks.put(uuid, currentStacks + 1);
            lastHitTime.put(uuid, System.currentTimeMillis());
            applyModifiers(player, config, playerStacks.get(uuid), setName);
        }
    }

    public static void decayStacks(Player player, ConfigurationSection config, String setName) {
        UUID uuid = player.getUniqueId();
        if (!playerStacks.containsKey(uuid)) return;

        double decayTime = config.getDouble("stack_decay_time", 8.0) * 1000;
        Long lastHit = lastHitTime.get(uuid);
        
        if (lastHit != null && System.currentTimeMillis() - lastHit > decayTime) {
            int currentStacks = playerStacks.get(uuid);
            if (currentStacks > 0) {
                playerStacks.put(uuid, currentStacks - 1);
                lastHitTime.put(uuid, System.currentTimeMillis());
                applyModifiers(player, config, playerStacks.get(uuid), setName);
            }
        }
    }

    private static void applyModifiers(Player player, ConfigurationSection config, int stacks, String setName) {
        int pieces = 4;
        double defPerStack = config.getDouble("defense_per_stack", 0.06);
        double msPenaltyPerStack = config.getDouble("movement_speed_penalty_per_stack", 0.05);

        // Use composite key: setName_pieces (e.g., "Dragon_2pc")
        NamespacedKey defKey = new NamespacedKey("arpgessentialsx", setName + "_" + pieces + "pc_fort_def");
        NamespacedKey msKey = new NamespacedKey("arpgessentialsx", setName + "_" + pieces + "pc_fort_ms");

        // Remove old modifiers
        player.getAttribute(Attribute.GENERIC_ARMOR).getModifiers()
            .removeIf(modifier -> modifier.getKey().equals(defKey));
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getModifiers()
            .removeIf(modifier -> modifier.getKey().equals(msKey));

        // Apply new modifiers
        if (stacks > 0) {
            double baseDef = player.getAttribute(Attribute.GENERIC_ARMOR).getBaseValue();
            double baseMS = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
            
            player.getAttribute(Attribute.GENERIC_ARMOR).addModifier(new AttributeModifier(
                defKey, baseDef * defPerStack * stacks, AttributeModifier.Operation.ADD_NUMBER, org.bukkit.inventory.EquipmentSlotGroup.ANY));
            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(new AttributeModifier(
                msKey, -baseMS * msPenaltyPerStack * stacks, AttributeModifier.Operation.ADD_NUMBER, org.bukkit.inventory.EquipmentSlotGroup.ANY));
        }
    }

    public static void clearPlayer(UUID uuid) {
        playerStacks.remove(uuid);
        lastHitTime.remove(uuid);
    }
}
