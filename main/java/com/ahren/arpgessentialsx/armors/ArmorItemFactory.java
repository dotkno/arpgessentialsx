package com.ahren.arpgessentialsx.armors;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.util.ColorUtil;
import com.ahren.arpgessentialsx.weapons.CatalystItemFactory;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public final class ArmorItemFactory {

    public static final String NBT_ARMOR_ID = "armor_id";
    public static final String NBT_MAX_DURABILITY = "max_durability";
    public static final String NBT_CURRENT_DURABILITY = "current_durability";
    public static final String NBT_SET_NAME = "set_name";

    private final ARPGEssentialsX plugin;
    private final LegacyComponentSerializer serializer;
    private final NamespacedKey armorIdKey;
    private final NamespacedKey maxDurabilityKey;
    private final NamespacedKey currentDurabilityKey;
    private final NamespacedKey setNameKey;

    public ArmorItemFactory(ARPGEssentialsX plugin) {
        this.plugin = plugin;
        this.serializer = LegacyComponentSerializer.legacyAmpersand();
        this.armorIdKey = new NamespacedKey(plugin, NBT_ARMOR_ID);
        this.maxDurabilityKey = new NamespacedKey(plugin, NBT_MAX_DURABILITY);
        this.currentDurabilityKey = new NamespacedKey(plugin, NBT_CURRENT_DURABILITY);
        this.setNameKey = new NamespacedKey(plugin, NBT_SET_NAME);
    }

    public ItemStack createArmor(Armor armor) {
        if (armor.getBaseItem() == null) return new ItemStack(org.bukkit.Material.AIR);

        ItemStack item = new ItemStack(armor.getBaseItem());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.displayName(serializer.deserialize(ColorUtil.translate(armor.getDisplayName())));

        List<Component> lore = new ArrayList<>();
        lore.add(serializer.deserialize(
                CatalystItemFactory.buildStarLine(armor.getStars())
                        + " &8" + armor.getArmorType().getDisplayName()));
        lore.add(serializer.deserialize("&r "));

        for (String line : armor.getLore()) {
            lore.add(serializer.deserialize(ColorUtil.translate(line)));
        }

        // Display set name if applicable
        if (armor.hasSetName()) {
            lore.add(serializer.deserialize("&r "));
            lore.add(serializer.deserialize("&6Set: &f" + armor.getSetName()));
            
            // Show set bonus availability
            if (armor.hasTwoPieceBonus()) {
                lore.add(serializer.deserialize("&7 2-Piece Bonus Available"));
            }
            if (armor.hasFourPieceBonus()) {
                lore.add(serializer.deserialize("&7 4-Piece Bonus Available"));
            }
        }

        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        if (armor.getCustomModelData() >= 0) {
            meta.setCustomModelData(armor.getCustomModelData());
        }

        // Apply armor stats
        EquipmentSlotGroup slot = getSlotForArmorType(armor.getArmorType());
        
        if (armor.getArmorPoints() != 0) {
            meta.addAttributeModifier(
                    Attribute.GENERIC_ARMOR,
                    new AttributeModifier(
                            new NamespacedKey(plugin, "armor_points_" + armor.getId()),
                            armor.getArmorPoints(),
                            AttributeModifier.Operation.ADD_NUMBER,
                            slot));
        }
        
        if (armor.getArmorToughness() != 0) {
            meta.addAttributeModifier(
                    Attribute.GENERIC_ARMOR_TOUGHNESS,
                    new AttributeModifier(
                            new NamespacedKey(plugin, "armor_toughness_" + armor.getId()),
                            armor.getArmorToughness(),
                            AttributeModifier.Operation.ADD_NUMBER,
                            slot));
        }

        meta.getPersistentDataContainer().set(armorIdKey, PersistentDataType.STRING, armor.getId());

        if (armor.hasSetName()) {
            meta.getPersistentDataContainer().set(setNameKey, PersistentDataType.STRING, armor.getSetName());
        }

        // Set custom durability if specified
        if (armor.hasCustomDurability()) {
            meta.getPersistentDataContainer().set(maxDurabilityKey, PersistentDataType.INTEGER, armor.getDurability());
            meta.getPersistentDataContainer().set(currentDurabilityKey, PersistentDataType.INTEGER, armor.getDurability());
            
            // Set vanilla durability to match custom durability
            if (meta instanceof org.bukkit.inventory.meta.Damageable damageable) {
                damageable.setMaxDamage(armor.getDurability());
                damageable.setDamage(0);
            }
            
            item.setItemMeta(meta);
            return item;
        }

        item.setItemMeta(meta);
        return item;
    }

    public String getArmorId(ItemStack item) {
        ItemMeta meta = getItemMetaOrNull(item);
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(armorIdKey, PersistentDataType.STRING);
    }

    public String getSetName(ItemStack item) {
        ItemMeta meta = getItemMetaOrNull(item);
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(setNameKey, PersistentDataType.STRING);
    }

    public Integer getMaxDurability(ItemStack item) {
        ItemMeta meta = getItemMetaOrNull(item);
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(maxDurabilityKey, PersistentDataType.INTEGER);
    }

    public Integer getCurrentDurability(ItemStack item) {
        ItemMeta meta = getItemMetaOrNull(item);
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(currentDurabilityKey, PersistentDataType.INTEGER);
    }

    public boolean setCurrentDurability(ItemStack item, int durability) {
        ItemMeta meta = getItemMetaOrNull(item);
        if (meta == null) return false;
        Integer max = getMaxDurability(item);
        if (max == null) return false;
        int clamped = Math.max(0, Math.min(max, durability));
        meta.getPersistentDataContainer().set(currentDurabilityKey, PersistentDataType.INTEGER, clamped);
        item.setItemMeta(meta);
        return true;
    }

    public boolean reduceDurability(ItemStack item, int amount) {
        Integer current = getCurrentDurability(item);
        if (current == null) return false;
        int newDurability = Math.max(0, current - amount);
        setCurrentDurability(item, newDurability);
        updateDurabilityLore(item);
        return newDurability <= 0;
    }

    private void updateDurabilityLore(ItemStack item) {
        ItemMeta meta = getItemMetaOrNull(item);
        if (meta == null) return;

        Integer max = getMaxDurability(item);
        Integer current = getCurrentDurability(item);
        if (max == null || current == null) return;

        List<Component> lore = meta.lore();
        if (lore == null) return;

        List<Component> newLore = new ArrayList<>();
        for (Component line : lore) {
            String lineStr = serializer.serialize(line);
            if (lineStr.contains("Durability:")) {
                String newLine = lineStr.replaceAll("Durability: \\d+/\\d+", "Durability: " + current + "/" + max);
                newLore.add(serializer.deserialize(ColorUtil.translate(newLine)));
            } else {
                newLore.add(line);
            }
        }

        meta.lore(newLore);
        item.setItemMeta(meta);
    }

    public boolean hasCustomDurability(ItemStack item) {
        return getMaxDurability(item) != null;
    }

    public NamespacedKey getArmorIdKey() { return armorIdKey; }
    public NamespacedKey getSetNameKey() { return setNameKey; }
    public NamespacedKey getMaxDurabilityKey() { return maxDurabilityKey; }
    public NamespacedKey getCurrentDurabilityKey() { return currentDurabilityKey; }

    private ItemMeta getItemMetaOrNull(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        return item.getItemMeta();
    }

    private EquipmentSlotGroup getSlotForArmorType(ArmorType type) {
        return switch (type) {
            case HEAD -> EquipmentSlotGroup.HEAD;
            case CHEST -> EquipmentSlotGroup.CHEST;
            case LEGS -> EquipmentSlotGroup.LEGS;
            case FEET -> EquipmentSlotGroup.FEET;
        };
    }
}
