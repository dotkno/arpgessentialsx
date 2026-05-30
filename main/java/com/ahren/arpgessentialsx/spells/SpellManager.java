package com.ahren.arpgessentialsx.spells;

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
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

public final class SpellManager {

    private final ARPGEssentialsX plugin;
    private final File spellsFile;
    private FileConfiguration spellsConfig;

    private final Map<String, Spell> spells = new LinkedHashMap<>();
    private final SpellBookFactory bookFactory;
    private final SpellEffectRegistry effectRegistry;

    public SpellManager(ARPGEssentialsX plugin) {
        this.plugin = plugin;
        this.spellsFile = new File(plugin.getDataFolder(), "spells.yml");
        this.bookFactory = new SpellBookFactory(plugin);
        this.effectRegistry = new SpellEffectRegistry(plugin.getLogger());

        loadSpells();
    }

    public void loadSpells() {
        plugin.saveResource("spells.yml", false);
        spellsConfig = YamlConfiguration.loadConfiguration(spellsFile);
        spells.clear();

        ConfigurationSection spellsSection = spellsConfig.getConfigurationSection("spells");
        if (spellsSection == null) {
            plugin.getLogger().warning("[SpellManager] No 'spells:' section found in spells.yml.");
            return;
        }

        for (String spellId : spellsSection.getKeys(false)) {
            ConfigurationSection spellSec = spellsSection.getConfigurationSection(spellId);
            if (spellSec == null) continue;

            try {
                Spell spell = new Spell(spellId, spellSec);
                resolveEffects(spell);
                spells.put(spellId.toLowerCase(), spell);
            } catch (Exception e) {
                plugin.getLogger().warning("[SpellManager] Failed to load spell '" + spellId + "': " + e.getMessage());
            }
        }

        plugin.getLogger().info("[SpellManager] Loaded " + spells.size() + " spell(s).");
        registerRecipes();
    }

    /**
     * Resolves each effect config section in a spell to a SpellEffect instance
     * via the registry. Logs a warning for unknown effect types instead of crashing.
     */
    private void resolveEffects(Spell spell) {
        for (ConfigurationSection effectConfig : spell.getEffectConfigs()) {
            String type = effectConfig.getString("type", "");
            if (type.isEmpty()) {
                plugin.getLogger().warning("[SpellManager] Spell '" + spell.getId()
                        + "' has an effect with no 'type' field — skipping.");
                continue;
            }

            SpellEffect effect = effectRegistry.get(type);
            if (effect == null) {
                plugin.getLogger().warning("[SpellManager] Spell '" + spell.getId()
                        + "' references unknown effect type '" + type + "' — skipping.");
                continue;
            }

            spell.addEffect(effect);
        }
    }

    private void registerRecipes() {
        for (Spell spell : spells.values()) {
            if (spell.getRecipeShape().isEmpty() || spell.getRecipeIngredients().isEmpty()) continue;

            try {
                NamespacedKey key = new NamespacedKey(plugin, "spell_" + spell.getId());

                // Remove old recipe before re-registering (safe on reload)
                Bukkit.removeRecipe(key);

                ItemStack result = bookFactory.createSpellBook(spell);
                ShapedRecipe recipe = new ShapedRecipe(key, result);
                recipe.shape(spell.getRecipeShape().toArray(new String[0]));

                for (Map.Entry<Character, Material> entry : spell.getRecipeIngredients().entrySet()) {
                    recipe.setIngredient(entry.getKey(), entry.getValue());
                }

                Bukkit.addRecipe(recipe);
                plugin.getLogger().info("[SpellManager] Registered recipe for: " + spell.getDisplayName());
            } catch (Exception e) {
                plugin.getLogger().warning("[SpellManager] Failed to register recipe for '"
                        + spell.getId() + "': " + e.getMessage());
            }
        }
    }

    public Spell getSpell(String id) {
        return id == null ? null : spells.get(id.toLowerCase());
    }

    public Collection<Spell> getAllSpells() {
        return Collections.unmodifiableCollection(spells.values());
    }

    public SpellBookFactory getBookFactory() { return bookFactory; }
    public SpellEffectRegistry getEffectRegistry() { return effectRegistry; }

    public void reload() {
        spells.clear();
        loadSpells();
    }
}