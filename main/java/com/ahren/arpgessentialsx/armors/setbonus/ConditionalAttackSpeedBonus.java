package com.ahren.arpgessentialsx.armors.setbonus;

import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Set bonus that grants attack speed when below a health threshold.
 *
 * yml params:
 *   health_threshold: 0.70   (70% health threshold)
 *   attack_speed_bonus: 0.15   (15% attack speed boost)
 *
 * Trigger: 4-piece set completion, checks health every second
 */
public final class ConditionalAttackSpeedBonus implements ArmorSetBonus {

    private static final Map<UUID, BukkitRunnable> activeTasks = new HashMap<>();

    @Override
    public void apply(Player player, ConfigurationSection config, int pieces, String setName) {
        double healthThreshold = config.getDouble("health_threshold", 0.70);
        double attackSpeedBonus = config.getDouble("attack_speed_bonus", 0.15);

        UUID uuid = player.getUniqueId();
        
        // Cancel existing task if any
        if (activeTasks.containsKey(uuid)) {
            activeTasks.get(uuid).cancel();
        }

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) {
                    cancel();
                    activeTasks.remove(uuid);
                    return;
                }

                double maxHealth = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
                double currentHealth = player.getHealth();
                double healthPercent = currentHealth / maxHealth;

                // Use composite key: setName_pieces (e.g., "Dragon_2pc")
                NamespacedKey key = new NamespacedKey("arpgessentialsx", setName + "_" + pieces + "pc_conditional_as");
                
                // Remove existing modifier
                player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getModifiers().removeIf(
                        modifier -> modifier.getKey().equals(key));

                // Apply bonus if below threshold
                if (healthPercent < healthThreshold) {
                    double baseSpeed = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getBaseValue();
                    double bonusAmount = baseSpeed * attackSpeedBonus;
                    player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).addModifier(new AttributeModifier(
                            key,
                            bonusAmount,
                            AttributeModifier.Operation.ADD_NUMBER));
                }
            }
        };

        task.runTaskTimer(com.ahren.arpgessentialsx.ARPGEssentialsX.getPlugin(com.ahren.arpgessentialsx.ARPGEssentialsX.class), 0L, 20L);
        activeTasks.put(uuid, task);
    }

    @Override
    public void remove(Player player, ConfigurationSection config, int pieces, String setName) {
        UUID uuid = player.getUniqueId();
        
        // Cancel task
        if (activeTasks.containsKey(uuid)) {
            activeTasks.get(uuid).cancel();
            activeTasks.remove(uuid);
        }

        // Use composite key: setName_pieces (e.g., "Dragon_2pc")
        NamespacedKey key = new NamespacedKey("arpgessentialsx", setName + "_" + pieces + "pc_conditional_as");
        player.getAttribute(Attribute.GENERIC_ATTACK_SPEED).getModifiers().removeIf(
                modifier -> modifier.getKey().equals(key));
    }

    @Override
    public String getType() {
        return "conditional_attack_speed";
    }

    public static void clearPlayer(UUID uuid) {
        if (activeTasks.containsKey(uuid)) {
            activeTasks.get(uuid).cancel();
            activeTasks.remove(uuid);
        }
    }
}
