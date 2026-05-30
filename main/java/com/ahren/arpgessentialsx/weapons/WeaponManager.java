package com.ahren.arpgessentialsx.weapons;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.weapons.passives.WeaponPassive;
import com.ahren.arpgessentialsx.weapons.passives.WeaponPassiveRegistry;
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

public final class WeaponManager {

    private final ARPGEssentialsX plugin;
    private final File weaponsFile;
    private FileConfiguration weaponsConfig;

    private final Map<String, Weapon> weapons = new LinkedHashMap<>();
    private final WeaponItemFactory itemFactory;
    private final WeaponEffectRegistry effectRegistry;
    private final WeaponPassiveRegistry passiveRegistry;
    private final SkillCooldownTracker skillCooldownTracker;

    public WeaponManager(ARPGEssentialsX plugin) {
        this.plugin          = plugin;
        this.weaponsFile     = new File(plugin.getDataFolder(), "weapons.yml");
        this.itemFactory     = new WeaponItemFactory(plugin);
        this.effectRegistry  = new WeaponEffectRegistry(plugin.getLogger());
        this.passiveRegistry = new WeaponPassiveRegistry(plugin.getLogger());
        this.skillCooldownTracker = new SkillCooldownTracker(plugin);
        loadWeapons();
    }

    public void loadWeapons() {
        plugin.saveResource("weapons.yml", false);
        weaponsConfig = YamlConfiguration.loadConfiguration(weaponsFile);
        weapons.clear();

        ConfigurationSection weaponsSection = weaponsConfig.getConfigurationSection("weapons");
        if (weaponsSection == null) {
            plugin.getLogger().warning("[WeaponManager] No 'weapons:' section in weapons.yml.");
            return;
        }

        for (String id : weaponsSection.getKeys(false)) {
            ConfigurationSection section = weaponsSection.getConfigurationSection(id);
            if (section == null) continue;
            try {
                Weapon weapon = new Weapon(id, section);
                resolveEffects(weapon);
                resolvePassives(weapon);
                weapons.put(id.toLowerCase(), weapon);
            } catch (Exception e) {
                plugin.getLogger().warning("[WeaponManager] Failed to load weapon '"
                        + id + "': " + e.getMessage());
            }
        }

        plugin.getLogger().info("[WeaponManager] Loaded " + weapons.size() + " weapon(s).");
        registerRecipes();
    }

    private void resolveEffects(Weapon weapon) {
        // On-hit effects
        for (ConfigurationSection cfg : weapon.getOnHitEffectConfigs()) {
            String type = cfg.getString("type", "");
            if (type.isEmpty()) continue;
            WeaponEffect effect = effectRegistry.get(type);
            if (effect == null) {
                plugin.getLogger().warning("[WeaponManager] Weapon '" + weapon.getId()
                        + "' has unknown on_hit effect type '" + type + "' — skipping.");
                continue;
            }
            weapon.addOnHitEffect(effect);
        }

        // Skill effects
        for (ConfigurationSection cfg : weapon.getSkillEffectConfigs()) {
            String type = cfg.getString("type", "");
            if (type.isEmpty()) continue;
            WeaponEffect effect = effectRegistry.get(type);
            if (effect == null) {
                plugin.getLogger().warning("[WeaponManager] Weapon '" + weapon.getId()
                        + "' has unknown skill effect type '" + type + "' — skipping.");
                continue;
            }
            weapon.addSkillEffect(effect);
        }
    }

    /**
     * Resolves passive config sections into WeaponPassive instances.
     * Each passive entry in weapons.yml must have a "type" key matching
     * a registered passive in WeaponPassiveRegistry.
     *
     * Example yml:
     *   passives:
     *     - type: flat_damage
     *       amount: 2.0
     *     - type: kill_heal
     *       amount: 4.0
     */
    private void resolvePassives(Weapon weapon) {
        for (ConfigurationSection cfg : weapon.getPassiveConfigs()) {
            String type = cfg.getString("type", "");
            if (type.isEmpty()) continue;
            WeaponPassive passive = passiveRegistry.get(type);
            if (passive == null) {
                plugin.getLogger().warning("[WeaponManager] Weapon '" + weapon.getId()
                        + "' has unknown passive type '" + type + "' — skipping.");
                continue;
            }
            weapon.addPassive(passive);
        }
    }

    private void registerRecipes() {
        for (Weapon weapon : weapons.values()) {
            if (weapon.getRecipeShape().isEmpty() || weapon.getRecipeIngredients().isEmpty()) continue;
            try {
                NamespacedKey key = new NamespacedKey(plugin, "weapon_" + weapon.getId());
                Bukkit.removeRecipe(key);
                ItemStack result = itemFactory.createWeapon(weapon);
                ShapedRecipe recipe = new ShapedRecipe(key, result);
                recipe.shape(weapon.getRecipeShape().toArray(new String[0]));
                for (Map.Entry<Character, Material> entry : weapon.getRecipeIngredients().entrySet()) {
                    recipe.setIngredient(entry.getKey(), entry.getValue());
                }
                Bukkit.addRecipe(recipe);
                plugin.getLogger().info("[WeaponManager] Registered recipe for: "
                        + weapon.getDisplayName());
            } catch (Exception e) {
                plugin.getLogger().warning("[WeaponManager] Failed to register recipe for '"
                        + weapon.getId() + "': " + e.getMessage());
            }
        }
    }

    public Weapon getWeapon(String id)              { return id == null ? null : weapons.get(id.toLowerCase()); }
    public Collection<Weapon> getAllWeapons()        { return Collections.unmodifiableCollection(weapons.values()); }
    public WeaponItemFactory getItemFactory()        { return itemFactory; }
    public WeaponEffectRegistry getEffectRegistry()  { return effectRegistry; }
    public WeaponPassiveRegistry getPassiveRegistry(){ return passiveRegistry; }
    public SkillCooldownTracker getSkillCooldownTracker() { return skillCooldownTracker; }

    public void reload() {
        weapons.clear();
        loadWeapons();
    }
}