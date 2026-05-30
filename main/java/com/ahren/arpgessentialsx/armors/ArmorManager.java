package com.ahren.arpgessentialsx.armors;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.armors.passives.ArmorPassive;
import com.ahren.arpgessentialsx.armors.passives.ArmorPassiveRegistry;
import com.ahren.arpgessentialsx.armors.setbonus.ArmorSetBonusRegistry;
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

public final class ArmorManager {

    private final ARPGEssentialsX plugin;
    private final File armorsFile;
    private FileConfiguration armorsConfig;

    private final Map<String, Armor> armors = new LinkedHashMap<>();
    private final ArmorItemFactory itemFactory;
    private final ArmorPassiveRegistry passiveRegistry;
    private final ArmorSetBonusRegistry setBonusRegistry;

    public ArmorManager(ARPGEssentialsX plugin) {
        this.plugin = plugin;
        this.armorsFile = new File(plugin.getDataFolder(), "armors.yml");
        this.itemFactory = new ArmorItemFactory(plugin);
        this.passiveRegistry = new ArmorPassiveRegistry(plugin.getLogger());
        this.setBonusRegistry = new ArmorSetBonusRegistry(plugin.getLogger());
        loadArmors();
    }

    public void loadArmors() {
        plugin.saveResource("armors.yml", false);
        armorsConfig = YamlConfiguration.loadConfiguration(armorsFile);
        armors.clear();

        ConfigurationSection armorsSection = armorsConfig.getConfigurationSection("armors");
        if (armorsSection == null) {
            plugin.getLogger().warning("[ArmorManager] No 'armors:' section in armors.yml.");
            return;
        }

        for (String id : armorsSection.getKeys(false)) {
            ConfigurationSection section = armorsSection.getConfigurationSection(id);
            if (section == null) continue;
            try {
                Armor armor = new Armor(id, section);
                resolvePassives(armor);
                armors.put(id.toLowerCase(), armor);
            } catch (Exception e) {
                plugin.getLogger().warning("[ArmorManager] Failed to load armor '"
                        + id + "': " + e.getMessage());
            }
        }

        plugin.getLogger().info("[ArmorManager] Loaded " + armors.size() + " armor(s).");
        registerRecipes();
    }

    private void resolvePassives(Armor armor) {
        for (ConfigurationSection cfg : armor.getPassiveConfigs()) {
            String type = cfg.getString("type", "");
            if (type.isEmpty()) continue;
            ArmorPassive passive = passiveRegistry.get(type);
            if (passive == null) {
                plugin.getLogger().warning("[ArmorManager] Armor '" + armor.getId()
                        + "' has unknown passive type '" + type + "' — skipping.");
                continue;
            }
            armor.addPassive(passive);
        }
    }

    private void registerRecipes() {
        for (Armor armor : armors.values()) {
            if (armor.getRecipeShape().isEmpty() || armor.getRecipeIngredients().isEmpty()) continue;
            try {
                NamespacedKey key = new NamespacedKey(plugin, "armor_" + armor.getId());
                Bukkit.removeRecipe(key);
                ItemStack result = itemFactory.createArmor(armor);
                ShapedRecipe recipe = new ShapedRecipe(key, result);
                recipe.shape(armor.getRecipeShape().toArray(new String[0]));
                for (Map.Entry<Character, Material> entry : armor.getRecipeIngredients().entrySet()) {
                    recipe.setIngredient(entry.getKey(), entry.getValue());
                }
                Bukkit.addRecipe(recipe);
                plugin.getLogger().info("[ArmorManager] Registered recipe for: "
                        + armor.getDisplayName());
            } catch (Exception e) {
                plugin.getLogger().warning("[ArmorManager] Failed to register recipe for '"
                        + armor.getId() + "': " + e.getMessage());
            }
        }
    }

    public Armor getArmor(String id) {
        return id == null ? null : armors.get(id.toLowerCase());
    }

    public Collection<Armor> getAllArmors() {
        return Collections.unmodifiableCollection(armors.values());
    }

    public ArmorItemFactory getItemFactory() {
        return itemFactory;
    }

    public ArmorPassiveRegistry getPassiveRegistry() {
        return passiveRegistry;
    }

    public ArmorSetBonusRegistry getSetBonusRegistry() {
        return setBonusRegistry;
    }

    public void reload() {
        armors.clear();
        loadArmors();
    }
}
