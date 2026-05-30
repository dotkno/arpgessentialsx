package com.ahren.arpgessentialsx.weapons;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.util.ColorUtil;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds catalyst ItemStacks from Catalyst definitions.
 *
 * NBT keys:
 *   catalyst_id   → String  (e.g., "tag2catalyst1")
 *   catalyst_stars → int    (1–5)
 */
public final class CatalystItemFactory {

    public static final String NBT_CATALYST_ID    = "catalyst_id";
    public static final String NBT_CATALYST_STARS = "catalyst_stars";

    private final ARPGEssentialsX plugin;
    private final LegacyComponentSerializer serializer;
    private final NamespacedKey catalystIdKey;
    private final NamespacedKey catalystStarsKey;

    public CatalystItemFactory(ARPGEssentialsX plugin) {
        this.plugin           = plugin;
        this.serializer       = LegacyComponentSerializer.legacyAmpersand();
        this.catalystIdKey    = new NamespacedKey(plugin, NBT_CATALYST_ID);
        this.catalystStarsKey = new NamespacedKey(plugin, NBT_CATALYST_STARS);
    }

    public ItemStack createCatalyst(Catalyst catalyst) {
        ItemStack item = new ItemStack(catalyst.getBaseItem());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // Display name
        meta.displayName(serializer.deserialize(
                ColorUtil.translate(catalyst.getDisplayName())));

        // Build lore — inject star rating and multiplier info
        List<net.kyori.adventure.text.Component> lore = new ArrayList<>();

        // Star display line
        lore.add(serializer.deserialize(buildStarLine(catalyst.getStars())));
        lore.add(serializer.deserialize("&8Catalyst · Mage Offhand"));
        lore.add(serializer.deserialize("&r "));

        // Custom lore from yml
        for (String line : catalyst.getLore()) {
            lore.add(serializer.deserialize(ColorUtil.translate(line)));
        }

        lore.add(serializer.deserialize("&r "));

        // Multiplier summary
        CatalystMultiplier m = catalyst.getMultiplier();
        lore.add(serializer.deserialize("&7Spell Amplification:"));
        lore.add(serializer.deserialize(
                "&f  ✦ &aDamage &7×&f" + String.format("%.2f", m.getDamageMultiplier())));
        lore.add(serializer.deserialize(
                "&f  ✦ &bRadius &7×&f" + String.format("%.2f", m.getRadiusMultiplier())));
        lore.add(serializer.deserialize(
                "&f  ✦ &dDuration &7×&f" + String.format("%.2f", m.getDurationMultiplier())));
        lore.add(serializer.deserialize(
                "&f  ✦ &eCharge &7×&f" + String.format("%.2f", m.getChargeTimeMultiplier())));
        lore.add(serializer.deserialize(
                "&f  ✦ &6Cooldown &7×&f" + String.format("%.2f", m.getCooldownMultiplier())));
        lore.add(serializer.deserialize("&r "));
        lore.add(serializer.deserialize("&9&lMage Offhand Only"));

        meta.lore(lore);
        meta.setEnchantmentGlintOverride(true);
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        if (catalyst.getCustomModelData() >= 0) {
            meta.setCustomModelData(catalyst.getCustomModelData());
        }

        // NBT stamps
        meta.getPersistentDataContainer().set(catalystIdKey,    PersistentDataType.STRING,  catalyst.getId());
        meta.getPersistentDataContainer().set(catalystStarsKey, PersistentDataType.INTEGER, catalyst.getStars());

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Reads the catalyst from a player's offhand and returns its multiplier.
     * Returns CatalystMultiplier.NONE if no catalyst is equipped.
     */
    public CatalystMultiplier getOffhandMultiplier(org.bukkit.entity.Player player) {
        ItemStack offhand = player.getInventory().getItemInOffHand();
        if (offhand == null || !offhand.hasItemMeta()) return CatalystMultiplier.NONE;
        ItemMeta meta = offhand.getItemMeta();
        if (meta == null) return CatalystMultiplier.NONE;
        Integer stars = meta.getPersistentDataContainer()
                .get(catalystStarsKey, PersistentDataType.INTEGER);
        if (stars == null) return CatalystMultiplier.NONE;
        return new CatalystMultiplier(stars);
    }

    public String getCatalystId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(catalystIdKey, PersistentDataType.STRING);
    }

    public NamespacedKey getCatalystIdKey()    { return catalystIdKey; }
    public NamespacedKey getCatalystStarsKey() { return catalystStarsKey; }

    // ── Helpers ───────────────────────────────────────────────────────────────

    /**
     * Builds the colored star display line for a given star count.
     * 1★ = gray, 2★ = green, 3★ = blue, 4★ = purple, 5★ = gold
     */
    public static String buildStarLine(int stars) {
        String color = switch (stars) {
            case 1 -> "&7";   // gray
            case 2 -> "&a";   // green
            case 3 -> "&b";   // blue (aqua)
            case 4 -> "&5";   // purple
            default -> "&6";  // gold
        };
        return color + "✦".repeat(stars) + " &8" + "✦".repeat(5 - stars);
    }
}