package com.ahren.arpgessentialsx.customitems;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;
import org.bukkit.inventory.ShapelessRecipe;
import org.bukkit.inventory.RecipeChoice;
import org.bukkit.inventory.Recipe;
import org.bukkit.inventory.meta.ItemMeta;
import java.util.HashSet;
import java.util.Set;
import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Locale;

public final class CustomItemManager {
    private final ARPGEssentialsX plugin;
    private final Map<String, CustomItem> items;
    private final CustomItemFactory factory;
    private final Set<NamespacedKey> registeredRecipeKeys = new HashSet<>();

    public CustomItemManager(ARPGEssentialsX plugin) {
        this.plugin = plugin;
        this.items = new LinkedHashMap<>();
        this.factory = new CustomItemFactory(plugin);
        loadItems();
    }

    public void loadItems() {
        // Unregister previous recipes before re-loading to avoid duplicates
        unregisterRecipes();

        plugin.saveResource("custom_items.yml", false);
        File file = new File(plugin.getDataFolder(), "custom_items.yml");
        FileConfiguration config = YamlConfiguration.loadConfiguration(file);
        items.clear();

        ConfigurationSection section = config.getConfigurationSection("items");
        if (section != null) {
            for (String id : section.getKeys(false)) {
                ConfigurationSection itemSection = section.getConfigurationSection(id);
                if (itemSection == null) continue;
                items.put(id.toLowerCase(), new CustomItem(id, itemSection));
            }
        }
        plugin.getLogger().info("Loaded " + items.size() + " custom item(s).");
        // Detailed debug output for console
        for (CustomItem it : items.values()) {
            plugin.getLogger().info(" - " + it.getId() + " [mat=" + (it.getMaterial() == null ? "null" : it.getMaterial().name())
                    + ", model=" + it.getCustomModelData()
                    + ", consumable=" + it.isConsumable()
                    + ", throwable=" + it.isThrowable()
                    + ", stackable=" + it.isStackable() + "]");
        }
        registerRecipes();
    }

    private void unregisterRecipes() {
        for (NamespacedKey key : registeredRecipeKeys) {
            try {
                plugin.getServer().removeRecipe(key);
            } catch (Exception ignored) {
            }
        }
        registeredRecipeKeys.clear();
    }

    private void registerRecipes() {
        for (CustomItem item : items.values()) {
            // if no recipe information, skip
            if ((item.getRecipeShape() == null || item.getRecipeShape().isEmpty()) && item.getRecipeIngredients().isEmpty()) continue;

            try {
                ItemStack result = createItem(item);

                // sanitize id for resource key (lowercase a-z0-9_-. allowed)
                String raw = item.getId() == null ? "unknown" : item.getId();
                String sanitized = raw.toLowerCase(Locale.ROOT).replaceAll("[^a-z0-9_.-]", "_");
                NamespacedKey key = new NamespacedKey(plugin, "item_" + sanitized);

                // Decide shaped vs shapeless: shaped when shape provided, else shapeless
                if (item.getRecipeShape() != null && !item.getRecipeShape().isEmpty()) {
                    // validate shape: max 3 rows and each row 1-3 chars and same length
                    var shape = item.getRecipeShape();
                    if (shape.size() > 3) {
                        plugin.getLogger().warning("Invalid recipe shape for " + item.getId() + ": more than 3 rows. Skipping recipe.");
                        continue;
                    }
                    int cols = shape.get(0).length();
                    if (cols < 1 || cols > 3) {
                        plugin.getLogger().warning("Invalid recipe shape columns for " + item.getId() + ". Skipping recipe.");
                        continue;
                    }
                    boolean rowsMatch = shape.stream().allMatch(s -> s.length() == cols);
                    if (!rowsMatch) {
                        plugin.getLogger().warning("Recipe shape rows mismatch for " + item.getId() + ". Skipping recipe.");
                        continue;
                    }

                    ShapedRecipe recipe = new ShapedRecipe(key, result);
                    recipe.shape(shape.toArray(new String[0]));

                    for (Map.Entry<Character, String> entry : item.getRecipeIngredients().entrySet()) {
                        char c = entry.getKey();
                        String val = entry.getValue();
                        if (val == null) continue;
                        val = val.trim();
                        if (val.startsWith("item:") || val.startsWith("custom:")) {
                            String ref = val.substring(val.indexOf(':') + 1);
                            CustomItem refItem = getItem(ref);
                            if (refItem != null) {
                                ItemStack ingr = factory.createItem(refItem);
                                recipe.setIngredient(c, new RecipeChoice.ExactChoice(ingr));
                            }
                        } else {
                            // try material
                            Material mat = Material.matchMaterial(val);
                            if (mat != null) recipe.setIngredient(c, mat);
                        }
                    }

                    plugin.getServer().addRecipe(recipe);
                    registeredRecipeKeys.add(key);
                    plugin.getLogger().info("Registered shaped recipe for item: " + item.getDisplayName());
                } else {
                    // shapeless
                    ShapelessRecipe recipe = new ShapelessRecipe(key, result);
                    for (Map.Entry<Character, String> entry : item.getRecipeIngredients().entrySet()) {
                        String val = entry.getValue();
                        if (val == null) continue;
                        val = val.trim();
                        if (val.startsWith("item:") || val.startsWith("custom:")) {
                            String ref = val.substring(val.indexOf(':') + 1);
                            CustomItem refItem = getItem(ref);
                            if (refItem != null) {
                                ItemStack ingr = factory.createItem(refItem);
                                recipe.addIngredient(new RecipeChoice.ExactChoice(ingr));
                            }
                        } else {
                            Material mat = Material.matchMaterial(val);
                            if (mat != null) recipe.addIngredient(mat);
                        }
                    }
                    plugin.getServer().addRecipe(recipe);
                    registeredRecipeKeys.add(key);
                    plugin.getLogger().info("Registered shapeless recipe for item: " + item.getDisplayName());
                }
            } catch (Exception e) {
                plugin.getLogger().warning("Failed to register recipe for item '" + item.getId() + "': " + e.getMessage());
            }
        }
    }

    public ItemStack createItem(CustomItem item) {
        return factory.createItem(item);
    }

    public CustomItemFactory getItemFactory() { return factory; }

    public CustomItem getItem(String id) { return id == null ? null : items.get(id.toLowerCase()); }
    public Collection<CustomItem> getAllItems() { return Collections.unmodifiableCollection(items.values()); }
}