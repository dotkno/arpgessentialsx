package com.ahren.arpgessentialsx.relics;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.util.ColorUtil;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.Sound;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Builds ItemStacks from Relic definitions.
 *
 * Handles:
 * - Display name, lore, enchant glint
 * - Custom model data (resource pack support)
 * - Uses tracking via NBT (shown in lore, item destroyed at 0)
 * - Tool break sound when uses run out
 */
public final class RelicItemFactory {

    public static final String NBT_RELIC_ID  = "relic_id";
    public static final String NBT_USES_LEFT = "relic_uses_left";

    private final ARPGEssentialsX plugin;
    private final LegacyComponentSerializer serializer;
    private final NamespacedKey relicIdKey;
    private final NamespacedKey usesLeftKey;

    public RelicItemFactory(ARPGEssentialsX plugin) {
        this.plugin = plugin;
        this.serializer = LegacyComponentSerializer.legacyAmpersand();
        this.relicIdKey  = new NamespacedKey(plugin, NBT_RELIC_ID);
        this.usesLeftKey = new NamespacedKey(plugin, NBT_USES_LEFT);
    }

    /**
     * Helper method to parse lore while preventing duplicate "Uses" rows
     */
    private List<net.kyori.adventure.text.Component> buildBaseLore(Relic relic) {
        List<net.kyori.adventure.text.Component> components = new ArrayList<>();
        for (String line : relic.getLore()) {
            // If the configuration line already contains a static uses indicator, skip it
            if (line.contains("Uses:")) continue;
            components.add(serializer.deserialize(ColorUtil.translate(line)));
        }
        return components;
    }

    /**
     * Creates a fully configured relic ItemStack.
     */
    public ItemStack createRelic(Relic relic) {
        ItemStack item = new ItemStack(relic.getBaseItem());
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return item;

        // Display name
        meta.displayName(serializer.deserialize(
                ColorUtil.translate(relic.getDisplayName())));

        // Lore — dynamically build base lore and safely append dynamic uses row
        List<net.kyori.adventure.text.Component> lore = buildBaseLore(relic);
        lore.add(serializer.deserialize(
                ColorUtil.translate("&f▸ &7Uses: &f" + relic.getMaxUses() + "/" + relic.getMaxUses())));
        meta.lore(lore);

        // Enchant glint
        meta.setEnchantmentGlintOverride(true);
        meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);

        // Custom model data
        if (relic.getCustomModelData() >= 0) {
            meta.setCustomModelData(relic.getCustomModelData());
        }

        // NBT stamps
        meta.getPersistentDataContainer().set(relicIdKey,  PersistentDataType.STRING,  relic.getId());
        meta.getPersistentDataContainer().set(usesLeftKey, PersistentDataType.INTEGER, relic.getMaxUses());

        item.setItemMeta(meta);
        return item;
    }

    /**
     * Consumes one use from a relic ItemStack.
     * Updates the lore, destroys the item at 0 uses with a tool break sound.
     *
     * @param item  The relic ItemStack in the player's hand
     * @param relic The relic definition
     * @param player The player holding it (for sound)
     * @return true if the item was destroyed, false if uses remain
     */
    public boolean consumeUse(ItemStack item, Relic relic, org.bukkit.entity.Player player) {
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return false;

        Integer usesLeft = meta.getPersistentDataContainer()
                .get(usesLeftKey, PersistentDataType.INTEGER);
        if (usesLeft == null || usesLeft <= 0) return false;

        int newUses = usesLeft - 1;

        if (newUses <= 0) {
            // Play tool break sound and destroy the item
            player.playSound(player.getLocation(), Sound.ENTITY_ITEM_BREAK, 1.0f, 1.0f);
            item.setAmount(0);
            return true;
        }

        // Update uses in NBT
        meta.getPersistentDataContainer().set(usesLeftKey, PersistentDataType.INTEGER, newUses);

        // Update uses line in lore safely without duplication
        List<net.kyori.adventure.text.Component> newLore = buildBaseLore(relic);
        newLore.add(serializer.deserialize(
                ColorUtil.translate("&f▸ &7Uses: &f" + newUses + "/" + relic.getMaxUses())));
        meta.lore(newLore);

        item.setItemMeta(meta);
        return false;
    }

    // ── NBT Readers ───────────────────────────────────────────────────────────

    public String getRelicId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(relicIdKey, PersistentDataType.STRING);
    }

    public int getUsesLeft(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return 0;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return 0;
        Integer uses = meta.getPersistentDataContainer().get(usesLeftKey, PersistentDataType.INTEGER);
        return uses != null ? uses : 0;
    }

    public NamespacedKey getRelicIdKey()  { return relicIdKey; }
    public NamespacedKey getUsesLeftKey() { return usesLeftKey; }
}