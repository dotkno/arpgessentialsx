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
 * Set bonus that grants attack speed and movement speed stacks on hitting enemies.
 *
 * yml params:
 *   max_stacks: 3
 *   attack_speed_per_stack: 0.12   (12% per stack)
 *   movement_speed_per_stack: 0.08   (8% per stack)
 *   stack_duration: 5.0   (seconds before stack decays)
 *
 * Trigger: 4-piece set completion, grants stacks on hit
 */
public final class MomentumStacksBonus implements ArmorSetBonus {

    private static final Map<UUID, Integer> playerStacks = new HashMap<>();
    private static final Map<UUID, Long> lastHitTime = new HashMap<>();

    @Override
    public void apply(Player player, ConfigurationSection config, int pieces, String setName) {
        // Initialize stacks to 0
        playerStacks.put(player.getUniqueId(), 0);
        lastHitTime.put(player.getUniqueId(), System.currentTimeMillis());
    }

    @Override
    public void remove(Player player, ConfigurationSection config, int pieces, String setName) {
        UUID uuid = player.getUniqueId();
        
        // Use composite key: setName_pieces (e.g., "Dragon_2pc")
        NamespacedKey asKey = new NamespacedKey("arpgessentialsx", setName + "_" + pieces + "pc_momentum_as");
        NamespacedKey msKey = new NamespacedKey("arpgessentialsx", setName + "_" + pieces + "pc_momentum_ms");
        
        player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getModifiers()
            .removeIf(modifier -> modifier.getKey().equals(asKey));
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getModifiers()
            .removeIf(modifier -> modifier.getKey().equals(msKey));
        
        playerStacks.remove(uuid);
        lastHitTime.remove(uuid);
    }

    @Override
    public String getType() {
        return "momentum_stacks";
    }

    public static void onHit(Player player, ConfigurationSection config, String setName) {
        UUID uuid = player.getUniqueId();
        if (!playerStacks.containsKey(uuid)) return;

        int maxStacks = config.getInt("max_stacks", 3);
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

        double stackDuration = config.getDouble("stack_duration", 5.0) * 1000;
        long lastHit = lastHitTime.get(uuid);
        
        if (System.currentTimeMillis() - lastHit > stackDuration) {
            int currentStacks = playerStacks.get(uuid);
            if (currentStacks > 0) {
                playerStacks.put(uuid, currentStacks - 1);
                lastHitTime.put(uuid, System.currentTimeMillis());
                applyModifiers(player, config, playerStacks.get(uuid), setName);
            }
        }
    }

    private static void applyModifiers(Player player, ConfigurationSection config, int stacks, String setName) {
        int pieces = 4; // This is always a 4-piece bonus
        double asPerStack = config.getDouble("attack_speed_per_stack", 0.12);
        double msPerStack = config.getDouble("movement_speed_per_stack", 0.08);

        // Use composite key: setName_pieces (e.g., "Dragon_2pc")
        NamespacedKey asKey = new NamespacedKey("arpgessentialsx", setName + "_" + pieces + "pc_momentum_as");
        NamespacedKey msKey = new NamespacedKey("arpgessentialsx", setName + "_" + pieces + "pc_momentum_ms");

        // Remove old modifiers
        player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getModifiers()
            .removeIf(modifier -> modifier.getKey().equals(asKey));
        player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getModifiers()
            .removeIf(modifier -> modifier.getKey().equals(msKey));

        // Apply new modifiers based on current stacks
        if (stacks > 0) {
            double baseAS = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getBaseValue();
            double baseMS = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
            
            player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).addModifier(new AttributeModifier(
                asKey, baseAS * asPerStack * stacks, AttributeModifier.Operation.ADD_NUMBER));
            player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(new AttributeModifier(
                msKey, baseMS * msPerStack * stacks, AttributeModifier.Operation.ADD_NUMBER));
        }
    }

    public static void clearPlayer(UUID uuid) {
        playerStacks.remove(uuid);
        lastHitTime.remove(uuid);
    }
}
