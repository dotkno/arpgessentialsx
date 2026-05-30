package com.ahren.arpgessentialsx.customitems;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class CustomItemManager {
    private final ARPGEssentialsX plugin;
    private final Map<String, CustomItem> items;

    public CustomItemManager(ARPGEssentialsX plugin) {
        this.plugin = plugin;
        this.items = new LinkedHashMap<>();
        loadItems();
    }

    public void loadItems() {
        plugin.saveResource("custom_items.yml", false);
        File file = new File(plugin.getDataFolder(), "custom_items.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        items.clear();

        ConfigurationSection section = config.getConfigurationSection("items");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                items.put(id.toLowerCase(), new CustomItem(id, section.getConfigurationSection(id)));
            }
        }
        plugin.getLogger().info("Loaded " + items.size() + " custom item(s).");
        registerRecipes();
    }

    private void registerRecipes() {
        for (CustomItem item : items.values()) {
            if (item.getRecipeShape().isEmpty() || item.getRecipeIngredients().isEmpty()) continue;
            try {
                ItemStack result = createItem(item);
                NamespacedKey key = new NamespacedKey(plugin, "item_" + item.getId());
                ShapedRecipe recipe = new ShapedRecipe(key, result);
                recipe.shape(item.getRecipeShape().toArray(new String[0]));
                for (Map.Entry<Character, Material> entry : item.getRecipeIngredients().entrySet()) {
                    recipe.setIngredient(entry.getKey(), entry.getValue());
                }
                plugin.getServer().addRecipe(recipe);
                plugin.getLogger().info("Registered recipe for item: " + item.getDisplayName());
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to register recipe for item '" + item.getId() + "'");
            }
        }
    }

    public ItemStack createItem(CustomItem item) {
        if (item.getMaterial() == null) return new ItemStack(Material.AIR);
        ItemStack stack = new ItemStack(item.getMaterial());
        var meta = stack.getItemMeta();
        meta.displayName(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand().deserialize(com.ahren.arpgessentialsx.util.ColorUtil.translate(item.getDisplayName())));
        meta.lore(item.getLore().stream().map(com.ahren.arpgessentialsx.util.ColorUtil::translate).map(net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer.legacyAmpersand()::deserialize).toList());
        if (item.getCustomModelData() != -1) meta.setCustomModelData(item.getCustomModelData());
        stack.setItemMeta(meta);
        return stack;
    }

    public CustomItem getItem(String id) { return id == null ? null : items.get(id.toLowerCase()); }
    public Collection<CustomItem> getAllItems() { return Collections.unmodifiableCollection(items.values()); }
}