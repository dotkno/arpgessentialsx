package com.ahren.arpgessentialsx.armors;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;

/**
 * Manages set bonus tracking and application for players.
 * Uses a clear-then-apply strategy to ensure airtight modifier management.
 */
public class SetBonusManager {

    private final ARPGEssentialsX plugin;
    private final ArmorManager armorManager;

    public SetBonusManager(ARPGEssentialsX plugin, ArmorManager armorManager) {
        this.plugin = plugin;
        this.armorManager = armorManager;
    }

    /**
     * Updates set bonuses based on currently equipped armor.
     * Uses clear-then-apply strategy: clear ALL arpgessentialsx modifiers, then re-apply based on current gear.
     */
    public void updateSetBonuses(Player player, Map<ArmorType, Armor> equippedArmor) {
        plugin.getLogger().info("[SetBonusManager] Updating set bonuses for player " + player.getName());

        // Step 1: Clear ALL existing 'arpgessentialsx' attributes from the player
        stripAllModifiers(player);

        // Step 2: Count pieces per set from current gear
        Map<String, Integer> setCounts = new HashMap<>();
        Map<String, Armor> representativeArmor = new HashMap<>();

        for (Armor armor : equippedArmor.values()) {
            if (armor.hasSetName()) {
                String setName = armor.getSetName();
                setCounts.merge(setName, 1, Integer::sum);
                representativeArmor.putIfAbsent(setName, armor);
            }
        }

        // Step 3: Apply new valid 2pc or 4pc modifiers based ONLY on the fresh count
        for (Map.Entry<String, Integer> entry : setCounts.entrySet()) {
            String setID = entry.getKey();
            int count = entry.getValue();
            Armor armor = representativeArmor.get(setID);

            if (count >= 2 && armor.hasTwoPieceBonus()) {
                apply2PieceBonus(player, setID, armor.getTwoPieceBonusConfig());
            }

            if (count >= 4 && armor.hasFourPieceBonus()) {
                apply4PieceBonus(player, setID, armor.getFourPieceBonusConfig());
            }
        }

        // Step 4: Force client-side attribute packet refresh for armor-related attributes
        forceClientAttributeRefresh(player);
    }

    /**
     * Strips ALL 'arpgessentialsx' attribute modifiers from the player.
     * This ensures a clean slate before applying new modifiers.
     * Skips class and civilian modifiers to avoid wiping class stats.
     * Only removes explicit set bonus tier modifiers (containing "_2pc" or "_4pc").
     */
    public void stripAllModifiers(Player player) {
        plugin.getLogger().info("[SetBonusManager] Stripping set bonus modifiers from player " + player.getName());

        for (Attribute attribute : Attribute.values()) {
            AttributeInstance instance = player.getAttribute(attribute);
            if (instance != null) {
                instance.getModifiers().stream()
                    .filter(mod -> mod.getKey().getNamespace().equals("arpgessentialsx"))
                    .filter(mod -> {
                        String path = mod.getKey().getKey();
                        // DO NOT remove if it belongs to the class or civilian system
                        if (path.startsWith("class_") || path.startsWith("civilian_")) {
                            return false;
                        }
                        // ONLY remove if it's an explicit set bonus tier modifier
                        return path.contains("_2pc") || path.contains("_4pc");
                    })
                    .forEach(instance::removeModifier);
            }
        }

        // Also clear any stack-based bonus state
        clearStackBasedBonuses(player);
    }

    /**
     * Clears stack-based bonus state for the player.
     * Called when stripping modifiers to ensure clean state.
     */
    private void clearStackBasedBonuses(Player player) {
        // Clear momentum stacks
        com.ahren.arpgessentialsx.armors.setbonus.MomentumStacksBonus.clearPlayer(player.getUniqueId());
        
        // Clear shadow stacks
        com.ahren.arpgessentialsx.armors.setbonus.ShadowStacksBonus.clearPlayer(player.getUniqueId());
        
        // Clear fortification stacks
        com.ahren.arpgessentialsx.armors.setbonus.FortificationStacksBonus.clearPlayer(player.getUniqueId());
        
        // Clear conditional attack speed task
        com.ahren.arpgessentialsx.armors.setbonus.ConditionalAttackSpeedBonus.clearPlayer(player.getUniqueId());
    }

    /**
     * Forces client-side attribute packet refresh for armor-related attributes.
     * This ensures the client receives updated armor, armor toughness, and knockback resistance values immediately.
     */
    private void forceClientAttributeRefresh(Player player) {
        org.bukkit.attribute.AttributeInstance armorInst = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_ARMOR);
        if (armorInst != null) {
            armorInst.setBaseValue(armorInst.getBaseValue());
        }

        org.bukkit.attribute.AttributeInstance toughnessInst = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_ARMOR_TOUGHNESS);
        if (toughnessInst != null) {
            toughnessInst.setBaseValue(toughnessInst.getBaseValue());
        }

        org.bukkit.attribute.AttributeInstance kbInst = player.getAttribute(org.bukkit.attribute.Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        if (kbInst != null) {
            kbInst.setBaseValue(kbInst.getBaseValue());
        }
    }

    private void apply2PieceBonus(Player player, String setID, ConfigurationSection config) {
        if (config == null) return;
        plugin.getLogger().info("[SetBonusManager] Applying 2-piece bonus for set " + setID + " to player " + player.getName());
        armorManager.getSetBonusRegistry().applyBonus(player, config, 2, setID);
    }

    private void apply4PieceBonus(Player player, String setID, ConfigurationSection config) {
        if (config == null) return;
        plugin.getLogger().info("[SetBonusManager] Applying 4-piece bonus for set " + setID + " to player " + player.getName());
        armorManager.getSetBonusRegistry().applyBonus(player, config, 4, setID);
    }

    /**
     * Re-applies set bonuses for a player based on their currently equipped armor.
     * Called on login to restore set bonuses without requiring manual re-equip.
     */
    public void restorePlayerSetBonuses(Player player) {
        plugin.getLogger().info("[SetBonusManager] Restoring set bonuses for " + player.getName());
        
        // Force re-calculation based on currently equipped armor
        plugin.getArmorEquipListener().scanAndEquipArmor(player);
    }
}
