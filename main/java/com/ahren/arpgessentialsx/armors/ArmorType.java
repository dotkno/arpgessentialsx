package com.ahren.arpgessentialsx.armors;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.List;

/**
 * Represents the type/slot of an armor piece.
 * Similar to WeaponType but for armor slots.
 */
public enum ArmorType {
    HEAD("head", "Helmet", List.of(
            Material.LEATHER_HELMET,
            Material.CHAINMAIL_HELMET,
            Material.IRON_HELMET,
            Material.GOLDEN_HELMET,
            Material.DIAMOND_HELMET,
            Material.NETHERITE_HELMET
    )),
    CHEST("chest", "Chestplate", List.of(
            Material.LEATHER_CHESTPLATE,
            Material.CHAINMAIL_CHESTPLATE,
            Material.IRON_CHESTPLATE,
            Material.GOLDEN_CHESTPLATE,
            Material.DIAMOND_CHESTPLATE,
            Material.NETHERITE_CHESTPLATE
    )),
    LEGS("legs", "Leggings", List.of(
            Material.LEATHER_LEGGINGS,
            Material.CHAINMAIL_LEGGINGS,
            Material.IRON_LEGGINGS,
            Material.GOLDEN_LEGGINGS,
            Material.DIAMOND_LEGGINGS,
            Material.NETHERITE_LEGGINGS
    )),
    FEET("feet", "Boots", List.of(
            Material.LEATHER_BOOTS,
            Material.CHAINMAIL_BOOTS,
            Material.IRON_BOOTS,
            Material.GOLDEN_BOOTS,
            Material.DIAMOND_BOOTS,
            Material.NETHERITE_BOOTS
    ));

    private final String configName;
    private final String displayName;
    private final List<Material> validMaterials;

    ArmorType(String configName, String displayName, List<Material> validMaterials) {
        this.configName = configName;
        this.displayName = displayName;
        this.validMaterials = validMaterials;
    }

    public String getConfigName() {
        return configName;
    }

    public String getDisplayName() {
        return displayName;
    }

    public List<Material> getValidMaterials() {
        return validMaterials;
    }

    /**
     * Checks if the given material is valid for this armor type.
     */
    public boolean isValidMaterial(Material material) {
        return validMaterials.contains(material);
    }

    /**
     * Parses armor type from config string.
     */
    public static ArmorType fromString(String str) {
        if (str == null) return HEAD;
        for (ArmorType type : values()) {
            if (type.configName.equalsIgnoreCase(str)) {
                return type;
            }
        }
        return HEAD;
    }

    /**
     * Gets the armor type from an ItemStack.
     */
    public static ArmorType fromItemStack(ItemStack item) {
        if (item == null) return null;
        Material material = item.getType();
        for (ArmorType type : values()) {
            if (type.isValidMaterial(material)) {
                return type;
            }
        }
        return null;
    }
}
