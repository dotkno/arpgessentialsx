package com.ahren.arpgessentialsx.weapons;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.ShapedRecipe;

import java.io.File;
import java.util.*;

/**
 * Loads catalysts from the "catalysts:" section of weapons.yml.
 * Merged into one file so server owners only have one place to edit.
 */
public final class CatalystManager {

    private final ARPGEssentialsX plugin;
    private final File weaponsFile;
    private final Map<String, Catalyst> catalysts = new LinkedHashMap<>();
    private final CatalystItemFactory itemFactory;

    public CatalystManager(ARPGEssentialsX plugin) {
        this.plugin        = plugin;
        this.weaponsFile   = new File(plugin.getDataFolder(), "weapons.yml");
        this.itemFactory   = new CatalystItemFactory(plugin);
        loadCatalysts();
    }

    public void loadCatalysts() {
        // weapons.yml is saved by WeaponManager — we just read from it
        if (!weaponsFile.exists()) {
            plugin.saveResource("weapons.yml", false);
        }

        FileConfiguration config = YamlConfiguration.loadConfiguration(weaponsFile);
        catalysts.clear();

        ConfigurationSection section = config.getConfigurationSection("catalysts");
        if (section == null) {
            plugin.getLogger().warning(
                    "[CatalystManager] No 'catalysts:' section in weapons.yml.");
            return;
        }

        for (String id : section.getKeys(false)) {
            ConfigurationSection cs = section.getConfigurationSection(id);
            if (cs == null) continue;
            try {
                Catalyst catalyst = new Catalyst(id, cs);
                catalysts.put(id.toLowerCase(), catalyst);
            } catch (Exception e) {
                plugin.getLogger().warning(
                        "[CatalystManager] Failed to load catalyst '" + id + "': " + e.getMessage());
            }
        }

        plugin.getLogger().info(
                "[CatalystManager] Loaded " + catalysts.size() + " catalyst(s).");
        registerRecipes();
    }

    private void registerRecipes() {
        for (Catalyst catalyst : catalysts.values()) {
            if (catalyst.getRecipeShape().isEmpty()
                    || catalyst.getRecipeIngredients().isEmpty()) continue;
            try {
                NamespacedKey key = new NamespacedKey(plugin, "catalyst_" + catalyst.getId());
                Bukkit.removeRecipe(key);
                ItemStack result = itemFactory.createCatalyst(catalyst);
                ShapedRecipe recipe = new ShapedRecipe(key, result);
                recipe.shape(catalyst.getRecipeShape().toArray(new String[0]));
                for (Map.Entry<Character, Material> entry
                        : catalyst.getRecipeIngredients().entrySet()) {
                    recipe.setIngredient(entry.getKey(), entry.getValue());
                }
                Bukkit.addRecipe(recipe);
                plugin.getLogger().info(
                        "[CatalystManager] Registered recipe for: " + catalyst.getDisplayName());
            } catch (Exception e) {
                plugin.getLogger().warning(
                        "[CatalystManager] Failed to register recipe for '"
                                + catalyst.getId() + "': " + e.getMessage());
            }
        }
    }

    public Catalyst getCatalyst(String id) {
        return id == null ? null : catalysts.get(id.toLowerCase());
    }

    public Collection<Catalyst> getAllCatalysts() {
        return Collections.unmodifiableCollection(catalysts.values());
    }

    public CatalystItemFactory getItemFactory() {
        return itemFactory;
    }

    public void reload() {
        catalysts.clear();
        loadCatalysts();
    }
}