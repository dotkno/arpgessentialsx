package com.ahren.arpgessentialsx.attributes;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.classes.RPGClass;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.Registry;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.Player;
import org.bukkit.inventory.EquipmentSlotGroup;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public final class ClassAttributeApplier {

    private final ARPGEssentialsX plugin;

    /**
     * Maps classes.yml attribute keys → Bukkit Attribute objects.
     *
     * FIX: Keys were corrupted ("com/ahren/..." path strings) due to a refactor artifact.
     *      "damage" and "armor" now correctly map to their Minecraft attribute IDs.
     */
    private static final Map<String, Attribute> ATTRIBUTE_MAP = new HashMap<>();

    static {
        ATTRIBUTE_MAP.put("health",               Registry.ATTRIBUTE.get(NamespacedKey.minecraft("max_health")));
        ATTRIBUTE_MAP.put("damage",               Registry.ATTRIBUTE.get(NamespacedKey.minecraft("attack_damage")));
        ATTRIBUTE_MAP.put("speed",                Registry.ATTRIBUTE.get(NamespacedKey.minecraft("movement_speed")));
        ATTRIBUTE_MAP.put("armor",                Registry.ATTRIBUTE.get(NamespacedKey.minecraft("armor")));
        ATTRIBUTE_MAP.put("armor_toughness",      Registry.ATTRIBUTE.get(NamespacedKey.minecraft("armor_toughness")));
        ATTRIBUTE_MAP.put("knockback_resistance", Registry.ATTRIBUTE.get(NamespacedKey.minecraft("knockback_resistance")));
    }

    /** The namespace string used for all our NamespacedKeys (e.g. "arpgessentialsx") */
    private final String pluginNamespace;

    public ClassAttributeApplier(ARPGEssentialsX plugin) {
        this.plugin = plugin;
        // Compute this once: NamespacedKey lowercases the plugin name for us.
        // Storing it avoids creating a throwaway key just to read its namespace.
        this.pluginNamespace = new NamespacedKey(plugin, "probe").getNamespace();
    }

    /**
     * Removes all attribute modifiers that belong to this plugin from the player.
     *
     * FIX: Previously compared against plugin.getName() (e.g. "ARPGEssentialsX"),
     *      but NamespacedKey stores the namespace in lowercase ("arpgessentialsx").
     *      The comparison always failed, so old modifiers were never removed.
     */
    public void clearAttributes(Player player) {
        try {
            for (Attribute attribute : ATTRIBUTE_MAP.values()) {
                if (attribute == null) continue;

                AttributeInstance instance = player.getAttribute(attribute);
                if (instance == null) continue;

                List<AttributeModifier> toRemove = new ArrayList<>();
                for (AttributeModifier mod : instance.getModifiers()) {
                    if (mod != null && mod.getKey() != null
                            && mod.getKey().getNamespace().equals(pluginNamespace)) {
                        toRemove.add(mod);
                    }
                }

                for (AttributeModifier mod : toRemove) {
                    instance.removeModifier(mod);
                }
            }
        } catch (Exception e) {
            plugin.getLogger().severe("Error clearing attributes for " + player.getName());
            e.printStackTrace();
        }

        // Sync health to the new max after stripping modifiers
        syncHealth(player);

        // Force movement speed resync on the client side.
        // Scheduled 2 ticks out so the client is ready to receive the update.
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (player.isOnline()) {
                Bukkit.dispatchCommand(
                        Bukkit.getConsoleSender(),
                        "execute as " + player.getName()
                                + " run attribute " + player.getName()
                                + " minecraft:movement_speed base set 0.1"
                );
            }
        }, 2L);
    }

    /**
     * Applies a class's attributes to a player, replacing any previously applied ones.
     */
    public void applyAttributes(Player player, RPGClass rpgClass) {
        if (rpgClass == null) return;

        clearAttributes(player);

        for (Map.Entry<String, Double> entry : rpgClass.getAttributes().entrySet()) {
            String configKey = entry.getKey();
            double value = entry.getValue();

            if (value == 0.0) continue;

            Attribute bukkitAttribute = ATTRIBUTE_MAP.get(configKey);
            if (bukkitAttribute == null) {
                plugin.getLogger().warning(
                        "classes.yml: Unknown attribute key '" + configKey + "' on class '"
                                + rpgClass.getId() + "'. Skipping."
                );
                continue;
            }

            applySingleAttribute(player, bukkitAttribute, configKey, value);
        }

        // If the new max health is higher than current health, set health to the new max.
        // This prevents the "you have 10 HP but your bar shows 20 HP" desync.
        Attribute healthAttr = ATTRIBUTE_MAP.get("health");
        if (healthAttr != null) {
            AttributeInstance inst = player.getAttribute(healthAttr);
            if (inst != null && player.getHealth() < inst.getValue()) {
                player.setHealth(Math.min(inst.getValue(), inst.getValue()));
            }
        }
    }

    private void applySingleAttribute(Player player, Attribute attribute, String configKey, double value) {
        AttributeInstance instance = player.getAttribute(attribute);
        if (instance == null) return;

        NamespacedKey key = new NamespacedKey(plugin, "class_" + configKey);

        // Guard against double-application (e.g. if applyAttributes is called twice quickly)
        for (AttributeModifier mod : instance.getModifiers()) {
            if (mod.getKey().equals(key)) {
                instance.removeModifier(mod);
                break;
            }
        }

        AttributeModifier modifier = new AttributeModifier(
                key,
                value,
                AttributeModifier.Operation.ADD_NUMBER,
                EquipmentSlotGroup.ANY
        );

        instance.addModifier(modifier);
    }

    /**
     * Applies penalty stats to classless "Civilian" players.
     * These are intentionally weak to encourage class selection.
     */
    public void applyCivilianStats(Player player) {
        clearAttributes(player);

        // -10 HP → 5 hearts (vanilla = 20 HP / 10 hearts)
        applySingleAttribute(player, ATTRIBUTE_MAP.get("health"), "civilian_health", -10.0);

        // -0.5 attack damage → 0.5 (vanilla fist = 1.0)
        applySingleAttribute(player, ATTRIBUTE_MAP.get("damage"), "civilian_damage", -0.5);

        // -0.03 movement speed → slightly slower than vanilla (vanilla internal = 0.1)
        applySingleAttribute(player, ATTRIBUTE_MAP.get("speed"), "civilian_speed", -0.03);

        syncHealth(player);
    }

    /** Clamps the player's current health to their new max health. */
    private void syncHealth(Player player) {
        Attribute healthAttr = ATTRIBUTE_MAP.get("health");
        if (healthAttr == null) return;
        AttributeInstance inst = player.getAttribute(healthAttr);
        if (inst == null) return;
        double max = inst.getValue();
        if (player.getHealth() > max) {
            player.setHealth(max);
        }
    }
}