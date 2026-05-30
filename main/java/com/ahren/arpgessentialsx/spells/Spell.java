package com.ahren.arpgessentialsx.spells;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a single spell definition loaded from spells.yml.
 *
 * Each spell has:
 *   - Metadata (name, element, type, lore, stars tier)
 *   - Cast stats (mana cost, charge time, cooldown, max uses)
 *   - A list of effect entries — each is a ConfigurationSection
 *     containing "type" and any effect-specific parameters
 *   - A crafting recipe
 *
 * The effect entries are NOT resolved to SpellEffect instances here —
 * that happens in SpellManager via SpellEffectRegistry, so Spell stays
 * a pure data class with no knowledge of effect implementations.
 */
public final class Spell {

    private final String id;
    private final String displayName;
    private final String element;
    private final String type;
    private final List<String> lore;
    private final int stars; // Added: Stores the 1-5 star tier rating

    private final double manaCost;
    private final double chargeTime;
    private final double cooldown;
    private final int maxUses;

    /**
     * Raw effect config sections from spells.yml.
     * Each entry is one "- type: ..." block under "effects:".
     * Resolved to SpellEffect instances by SpellManager after loading.
     */
    private final List<ConfigurationSection> effectConfigs;

    /**
     * Resolved SpellEffect instances — populated by SpellManager
     * after the registry is available. Empty until then.
     */
    private final List<SpellEffect> effects = new ArrayList<>();

    private final List<String> recipeShape;
    private final Map<Character, Material> recipeIngredients;

    public Spell(String id, ConfigurationSection section) {
        this.id = id;
        this.displayName = section.getString("display_name", id);
        this.element = section.getString("element", "none").toLowerCase();
        this.type = section.getString("type", "utility").toLowerCase();
        this.lore = section.getStringList("lore");
        this.stars = section.getInt("stars", 1); // Added: Reads stars from yml, defaults to 1

        this.manaCost = section.getDouble("mana_cost", 1);
        this.chargeTime = section.getDouble("charge_time", 1.0);
        this.cooldown = section.getDouble("cooldown", 1.0);
        this.maxUses = section.getInt("max_uses", 10);

        // Parse effect config sections
        this.effectConfigs = new ArrayList<>();
        List<?> effectsList = section.getList("effects");
        if (effectsList != null) {
            for (Object obj : effectsList) {
                if (obj instanceof Map) {
                    // Convert Map back to ConfigurationSection via a temp MemorySection
                    ConfigurationSection effectSection = section.createSection("_temp_" + effectConfigs.size());
                    ((Map<?, ?>) obj).forEach((k, v) -> effectSection.set(k.toString(), v));
                    effectConfigs.add(effectSection);
                }
            }
        }

        // Parse recipe
        this.recipeShape = section.getStringList("recipe.shape");
        this.recipeIngredients = new LinkedHashMap<>();
        ConfigurationSection ingredientSection = section.getConfigurationSection("recipe.ingredients");
        if (ingredientSection != null) {
            for (String key : ingredientSection.getKeys(false)) {
                if (key.length() == 1) {
                    Material mat = Material.matchMaterial(ingredientSection.getString(key, ""));
                    if (mat != null) recipeIngredients.put(key.charAt(0), mat);
                }
            }
        }
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getId()           { return id; }
    public String getDisplayName()  { return displayName; }
    public String getElement()      { return element; }
    public String getType()         { return type; }
    public List<String> getLore()   { return lore; }
    public int getStars()           { return stars; } // Added: Getter for star tier rating

    public double getManaCost()     { return manaCost; }
    public double getChargeTime()   { return chargeTime; }
    public double getCooldown()     { return cooldown; }
    public int getMaxUses()         { return maxUses; }

    public List<ConfigurationSection> getEffectConfigs() {
        return Collections.unmodifiableList(effectConfigs);
    }

    /**
     * The resolved, executable effects. Populated by SpellManager.
     * Empty list = spell has no effects (harmless but probably a config mistake).
     */
    public List<SpellEffect> getEffects() {
        return Collections.unmodifiableList(effects);
    }

    /**
     * Called by SpellManager after registry resolution.
     * Adds a resolved effect to this spell's execution list.
     */
    void addEffect(SpellEffect effect) {
        effects.add(effect);
    }

    public List<String> getRecipeShape()                         { return recipeShape; }
    public Map<Character, Material> getRecipeIngredients()       { return Collections.unmodifiableMap(recipeIngredients); }
}