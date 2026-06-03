package com.ahren.arpgessentialsx.relics;

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

public final class RelicManager {

    private final ARPGEssentialsX plugin;
    private final File relicsFile;
    private FileConfiguration relicsConfig;

    private final Map<String, Relic> relics = new LinkedHashMap<>();
    private final RelicItemFactory itemFactory;
    private final RelicEffectRegistry effectRegistry;

    public RelicManager(ARPGEssentialsX plugin) {
        this.plugin = plugin;
        this.relicsFile = new File(plugin.getDataFolder(), "relics.yml");
        this.itemFactory = new RelicItemFactory(plugin);
        this.effectRegistry = new RelicEffectRegistry(plugin.getLogger());
        loadRelics();
    }

    public void loadRelics() {
        plugin.saveResource("relics.yml", false);
        relicsConfig = YamlConfiguration.loadConfiguration(relicsFile);
        relics.clear();

        ConfigurationSection relicsSection = relicsConfig.getConfigurationSection("relics");
        if (relicsSection == null) {
            plugin.getLogger().warning("[RelicManager] No 'relics:' section in relics.yml.");
            return;
        }

        for (String id : relicsSection.getKeys(false)) {
            ConfigurationSection section = relicsSection.getConfigurationSection(id);
            if (section == null) continue;

            try {
                Relic relic = new Relic(id, section);
                resolveEffects(relic);
                relics.put(id.toLowerCase(), relic);
            } catch (Exception e) {
                plugin.getLogger().warning("[RelicManager] Failed to load relic '" + id + "': " + e.getMessage());
            }
        }

        plugin.getLogger().info("[RelicManager] Loaded " + relics.size() + " relic(s).");
        registerRecipes();
    }

    private void resolveEffects(Relic relic) {
        for (ConfigurationSection effectConfig : relic.getEffectConfigs()) {
            String type = effectConfig.getString("type", "");
            if (type.isEmpty()) {
                plugin.getLogger().warning("[RelicManager] Relic '" + relic.getId()
                        + "' has an effect with no 'type' field — skipping.");
                continue;
            }

            RelicEffect effect = effectRegistry.get(type);
            if (effect == null) {
                plugin.getLogger().warning("[RelicManager] Relic '" + relic.getId()
                        + "' references unknown effect type '" + type + "' — skipping.");
                continue;
            }

            relic.addEffect(effect);
        }
    }

    private void registerRecipes() {
        for (Relic relic : relics.values()) {
            if (relic.getRecipeShape().isEmpty() || relic.getRecipeIngredients().isEmpty()) continue;

            try {
                NamespacedKey key = new NamespacedKey(plugin, "relic_" + relic.getId());

                // Remove old recipe before re-registering (safe on reload)
                Bukkit.removeRecipe(key);

                ItemStack result = itemFactory.createRelic(relic);
                ShapedRecipe recipe = new ShapedRecipe(key, result);
                recipe.shape(relic.getRecipeShape().toArray(new String[0]));

                for (Map.Entry<Character, String> entry : relic.getRecipeIngredients().entrySet()) {
                    Material material = Material.matchMaterial(entry.getValue());
                    if (material != null) {
                        recipe.setIngredient(entry.getKey(), material);
                    }
                }

                Bukkit.addRecipe(recipe);
                plugin.getLogger().info("[RelicManager] Registered recipe for: " + relic.getDisplayName());
            } catch (Exception e) {
                plugin.getLogger().warning("[RelicManager] Failed to register recipe for '"
                        + relic.getId() + "': " + e.getMessage());
            }
        }
    }

    public Relic getRelic(String id)               { return id == null ? null : relics.get(id.toLowerCase()); }
    public Collection<Relic> getAllRelics()         { return Collections.unmodifiableCollection(relics.values()); }
    public RelicItemFactory getItemFactory()        { return itemFactory; }
    public RelicEffectRegistry getEffectRegistry()  { return effectRegistry; }

    public void reload() {
        relics.clear();
        loadRelics();
    }
}