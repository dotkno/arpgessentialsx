package com.ahren.arpgessentialsx.weapons;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.util.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

public final class WeaponItemFactory {

    public static final String NBT_WEAPON_ID       = "weapon_id";
    public static final String NBT_CATALYST_STARS  = "catalyst_stars";
    public static final String NBT_MAX_DURABILITY  = "max_durability";
    public static final String NBT_CURRENT_DURABILITY = "current_durability";

    private final ARPGEssentialsX plugin;
    private final LegacyComponentSerializer serializer;
    private final NamespacedKey weaponIdKey;
    private final NamespacedKey catalystStarsKey;
    private final NamespacedKey maxDurabilityKey;
    private final NamespacedKey currentDurabilityKey;

    public WeaponItemFactory(ARPGEssentialsX plugin) {
        this.plugin               = plugin;
        this.serializer           = LegacyComponentSerializer.legacyAmpersand();
        this.weaponIdKey          = new NamespacedKey(plugin, NBT_WEAPON_ID);
        this.catalystStarsKey     = new NamespacedKey(plugin, NBT_CATALYST_STARS);
        this.maxDurabilityKey     = new NamespacedKey(plugin, NBT_MAX_DURABILITY);
        this.currentDurabilityKey = new NamespacedKey(plugin, NBT_CURRENT_DURABILITY);
    }

    public ItemStack createWeapon(Weapon weapon) {
        if (weapon.getBaseItem() == null) return new ItemStack(org.bukkit.Material.AIR);

        ItemStack item = new ItemStack(weapon.getBaseItem());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        meta.displayName(serializer.deserialize(ColorUtil.translate(weapon.getDisplayName())));

        List<Component> lore = new ArrayList<>();
        lore.add(serializer.deserialize(
                CatalystItemFactory.buildStarLine(weapon.getStars())
                        + " &8" + weapon.getWeaponType().getDisplayName()));
        lore.add(serializer.deserialize("&r "));

        for (String line : weapon.getLore()) {
            lore.add(serializer.deserialize(ColorUtil.translate(line)));
        }

        if (weapon.hasSkill()) {
            lore.add(serializer.deserialize("&r "));
            lore.add(serializer.deserialize("&e&l✦ Skill: &r" + ColorUtil.translate(weapon.getSkillName())));
            lore.add(serializer.deserialize("&7Sneak + Right-Click to activate"));
            if (weapon.getSkillClassTag() > 0) {
                lore.add(serializer.deserialize("&8(" + classNameFor(weapon.getSkillClassTag()) + " only)"));
            }
        }

        meta.lore(lore);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        if (weapon.getCustomModelData() >= 0) {
            meta.setCustomModelData(weapon.getCustomModelData());
        }

        if (weapon.getDamageBonus() != 0) {
            meta.addAttributeModifier(
                    Attribute.GENERIC_ATTACK_DAMAGE,
                    new AttributeModifier(
                            new NamespacedKey(plugin, "weapon_damage_" + weapon.getId()),
                            weapon.getDamageBonus(),
                            AttributeModifier.Operation.ADD_NUMBER,
                            org.bukkit.inventory.EquipmentSlotGroup.MAINHAND));
        }
        // Set attack speed from weapons.yml as an absolute value
        // attack_speed_bonus is treated as the actual attack speed (attacks per second)
        // Use ADD_NUMBER with negative offset to override the base attack speed
        double targetSpeed = weapon.getAttackSpeedBonus();
        if (targetSpeed == 0.0) {
            targetSpeed = 4.0; // Default to standard sword speed if not specified
        }
        // Calculate the offset needed: targetSpeed - baseSpeed (assume base is 4.0 for most weapons)
        double offset = targetSpeed - 4.0;
        meta.addAttributeModifier(
                Attribute.GENERIC_ATTACK_SPEED,
                new AttributeModifier(
                        new NamespacedKey(plugin, "weapon_speed_" + weapon.getId()),
                        offset,
                        AttributeModifier.Operation.ADD_NUMBER,
                        org.bukkit.inventory.EquipmentSlotGroup.MAINHAND));

        meta.getPersistentDataContainer().set(weaponIdKey, PersistentDataType.STRING, weapon.getId());

        if (weapon.getWeaponType() == WeaponType.CATALYST) {
            meta.getPersistentDataContainer().set(catalystStarsKey, PersistentDataType.INTEGER, weapon.getStars());
        }

        // Set custom durability if specified
        if (weapon.hasCustomDurability()) {
            meta.getPersistentDataContainer().set(maxDurabilityKey, PersistentDataType.INTEGER, weapon.getMaxDurability());
            meta.getPersistentDataContainer().set(currentDurabilityKey, PersistentDataType.INTEGER, weapon.getMaxDurability()); // Start at max
            
            // Set vanilla durability to match custom durability
            if (meta instanceof org.bukkit.inventory.meta.Damageable damageable) {
                damageable.setMaxDamage(weapon.getMaxDurability());
                damageable.setDamage(0); // Start at full durability (0 damage)
            }
            
            item.setItemMeta(meta);
            return item;
        }

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Reads the catalyst multiplier from the item in the player's MAINHAND.
     * (Useful if a system ever needs to check weapon stats while actively holding it to swing)
     */
    public CatalystMultiplier getMainhandCatalystMultiplier(org.bukkit.entity.Player player) {
        ItemStack main = player.getInventory().getItemInMainHand();
        if (main == null || !main.hasItemMeta()) return CatalystMultiplier.NONE;
        ItemMeta meta = main.getItemMeta();
        if (meta == null) return CatalystMultiplier.NONE;
        Integer stars = meta.getPersistentDataContainer().get(catalystStarsKey, PersistentDataType.INTEGER);
        if (stars == null) return CatalystMultiplier.NONE;
        return new CatalystMultiplier(stars);
    }

    /**
     * ADDED: Reads the catalyst multiplier from the item in the player's OFFHAND.
     * Used by SpellCastManager when the player is holding a Spellbook in their mainhand.
     */
    public CatalystMultiplier getOffhandCatalystMultiplier(org.bukkit.entity.Player player) {
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand == null || !offhand.hasItemMeta()) return CatalystMultiplier.NONE;
        ItemMeta meta = offhand.getItemMeta();
        if (meta == null) return CatalystMultiplier.NONE;
        Integer stars = meta.getPersistentDataContainer().get(catalystStarsKey, PersistentDataType.INTEGER);
        if (stars == null) return CatalystMultiplier.NONE;
        return new CatalystMultiplier(stars);
    }

    public String getWeaponId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(weaponIdKey, PersistentDataType.STRING);
    }

    /**
     * Gets the max durability for a custom weapon item.
     * Returns null if the item doesn't have custom durability.
     */
    public Integer getMaxDurability(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(maxDurabilityKey, PersistentDataType.INTEGER);
    }

    /**
     * Gets the current durability for a custom weapon item.
     * Returns null if the item doesn't have custom durability.
     */
    public Integer getCurrentDurability(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(currentDurabilityKey, PersistentDataType.INTEGER);
    }

    /**
     * Sets the current durability for a custom weapon item.
     * Returns true if successful, false if the item doesn't have custom durability.
     */
    public boolean setCurrentDurability(ItemStack item, int durability) {
        if (item == null || !item.hasItemMeta()) return false;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;
        Integer max = getMaxDurability(item);
        if (max == null) return false;
        int clamped = Math.max(0, Math.min(max, durability));
        meta.getPersistentDataContainer().set(currentDurabilityKey, PersistentDataType.INTEGER, clamped);
        item.setItemMeta(meta);
        return true;
    }

    /**
     * Reduces durability by a specified amount.
     * Returns true if the item broke (durability reached 0), false otherwise.
     */
    public boolean reduceDurability(ItemStack item, int amount) {
        Integer current = getCurrentDurability(item);
        if (current == null) return false;
        int newDurability = Math.max(0, current - amount);
        setCurrentDurability(item, newDurability);
        updateDurabilityLore(item);
        return newDurability <= 0;
    }

    /**
     * Updates the durability display in the item's lore.
     * Looks for "&4Durability: X/Y" pattern and updates it with current values.
     */
    private void updateDurabilityLore(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return;
        ItemMeta meta = item.getItemMeta();
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
                // Update the durability line
                String newLine = lineStr.replaceAll("Durability: \\d+/\\d+", "Durability: " + current + "/" + max);
                newLore.add(serializer.deserialize(ColorUtil.translate(newLine)));
            } else {
                newLore.add(line);
            }
        }

        meta.lore(newLore);
        item.setItemMeta(meta);
    }

    /**
     * Checks if an item has custom durability.
     */
    public boolean hasCustomDurability(ItemStack item) {
        return getMaxDurability(item) != null;
    }

    public NamespacedKey getWeaponIdKey()          { return weaponIdKey; }
    public NamespacedKey getCatalystStarsKey()     { return catalystStarsKey; }
    public NamespacedKey getMaxDurabilityKey()     { return maxDurabilityKey; }
    public NamespacedKey getCurrentDurabilityKey() { return currentDurabilityKey; }

    private String classNameFor(int tag) {
        return switch (tag) {
            case 1 -> "Fighter";
            case 2 -> "Mage";
            case 3 -> "Marksman";
            case 4 -> "Assassin";
            case 5 -> "Tank";
            default -> "Unknown";
        };
    }
}