package com.ahren.arpgessentialsx.customitems;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.util.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.WeakHashMap;
import java.util.concurrent.ConcurrentHashMap;

public final class CustomItemFactory {

    public static final String NBT_CUSTOM_ITEM_ID = "custom_item_id";
    public static final String NBT_CONSUMABLE = "custom_consumable";
    public static final String NBT_THROWABLE = "custom_throwable";
    public static final String NBT_BLOCK = "custom_block";
    public static final String NBT_STACKABLE = "custom_stackable";

    private final ARPGEssentialsX plugin;
    private final LegacyComponentSerializer serializer;
    private final NamespacedKey customItemIdKey;
    private final NamespacedKey consumableKey;
    private final NamespacedKey throwableKey;
    private final NamespacedKey blockKey;
    private final NamespacedKey stackableKey;
    private final Map<String, ItemStack> prototypeCache = new ConcurrentHashMap<>();

    public CustomItemFactory(ARPGEssentialsX plugin) {
        this.plugin = plugin;
        this.serializer = LegacyComponentSerializer.legacyAmpersand();
        this.customItemIdKey = new NamespacedKey(plugin, NBT_CUSTOM_ITEM_ID);
        this.consumableKey = new NamespacedKey(plugin, NBT_CONSUMABLE);
        this.throwableKey = new NamespacedKey(plugin, NBT_THROWABLE);
        this.blockKey = new NamespacedKey(plugin, NBT_BLOCK);
        this.stackableKey = new NamespacedKey(plugin, NBT_STACKABLE);
    }

    public ItemStack createItem(CustomItem item) {
        // Return a clone of cached prototype if available
        String cacheKey = item.getId();
        if (prototypeCache.containsKey(cacheKey)) {
            return prototypeCache.get(cacheKey).clone();
        }

        if (item == null || item.getMaterial() == null) return new ItemStack(org.bukkit.Material.AIR);

        ItemStack stack = new ItemStack(item.getMaterial(), Math.max(1, item.getAmount()));
        ItemMeta meta = stack.getItemMeta();
        if (meta == null) return stack;

        // Display name
        meta.displayName(serializer.deserialize(ColorUtil.translate(item.getDisplayName())));

        // Lore
        List<Component> lore = new ArrayList<>();
        for (String line : item.getLore()) {
            lore.add(serializer.deserialize(ColorUtil.translate(line)));
        }
        meta.lore(lore);

        // flags
        meta.addItemFlags(ItemFlag.HIDE_ATTRIBUTES);

        if (item.getCustomModelData() >= 0) meta.setCustomModelData(item.getCustomModelData());
        if (item.isUnbreakable()) meta.setUnbreakable(true);

        // Enchantments
        for (Map.Entry<String, Integer> ench : item.getEnchantments().entrySet()) {
            Enchantment e = Enchantment.getByName(ench.getKey().toUpperCase());
            if (e != null) meta.addEnchant(e, Math.max(1, ench.getValue()), true);
        }

        // Persistent data tags for easy detection
        meta.getPersistentDataContainer().set(customItemIdKey, PersistentDataType.STRING, item.getId());
        meta.getPersistentDataContainer().set(consumableKey, PersistentDataType.INTEGER, item.isConsumable() ? 1 : 0);
        meta.getPersistentDataContainer().set(throwableKey, PersistentDataType.INTEGER, item.isThrowable() ? 1 : 0);
        meta.getPersistentDataContainer().set(blockKey, PersistentDataType.INTEGER, item.isBlock() ? 1 : 0);
        meta.getPersistentDataContainer().set(stackableKey, PersistentDataType.INTEGER, item.isStackable() ? 1 : 0);

        stack.setItemMeta(meta);
        // Cache the prototype for future clones
        prototypeCache.put(cacheKey, stack.clone());
        // Return a clone to the caller so they get a unique instance
        return stack;
    }

    public String getCustomItemId(ItemStack item) {
        if (item == null || !item.hasItemMeta()) return null;
        ItemMeta meta = item.getItemMeta();
        if (meta == null) return null;
        return meta.getPersistentDataContainer().get(customItemIdKey, PersistentDataType.STRING);
    }

    public NamespacedKey getCustomItemIdKey() { return customItemIdKey; }
}

