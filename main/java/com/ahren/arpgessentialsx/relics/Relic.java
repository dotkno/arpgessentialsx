package com.ahren.arpgessentialsx.relics;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Represents a single relic definition loaded from relics.yml.
 *
 * Key differences from spells:
 * - Instant activation (no charge phase)
 * - No mana cost
 * - Limited uses — item disappears at 0
 * - Each relic has its own base_item material
 */
public final class Relic {

    private final String id;
    private final String displayName;
    private final int stars; // Added stars field
    private final int classTag;
    private final String type;
    private final double cooldown;
    private final int maxUses;
    private final Material baseItem;
    private final int customModelData;
    private final List<String> lore;

    private final List<ConfigurationSection> effectConfigs;
    private final List<RelicEffect> effects = new ArrayList<>();

    private final List<String> recipeShape;
    private final Map<Character, Material> recipeIngredients;

    public Relic(String id, ConfigurationSection section) {
        this.id = id;
        this.displayName = section.getString("display_name", id);
        this.stars = section.getInt("stars", 1); // Reads stars from config (defaults to 1)
        this.classTag = section.getInt("class_tag", 0);
        this.type = section.getString("type", "utility").toLowerCase();
        this.cooldown = section.getDouble("cooldown", 10.0);
        this.maxUses = section.getInt("max_uses", 30);
        this.customModelData = section.getInt("custom_model_data", -1);
        this.lore = section.getStringList("lore");

        // Parse base item — default GOAT_HORN but fully configurable
        String matName = section.getString("base_item", "GOAT_HORN");
        Material mat = Material.matchMaterial(matName);
        this.baseItem = (mat != null) ? mat : Material.GOAT_HORN;

        // Parse effect config sections
        this.effectConfigs = new ArrayList<>();
        List<?> effectsList = section.getList("effects");
        if (effectsList != null) {
            for (Object obj : effectsList) {
                if (obj instanceof Map) {
                    ConfigurationSection effectSection = section.createSection(
                            "_eff_" + effectConfigs.size());
                    ((Map<?, ?>) obj).forEach((k, v) -> effectSection.set(k.toString(), v));
                    effectConfigs.add(effectSection);
                }
            }
        }

        // Parse recipe
        this.recipeShape = section.getStringList("recipe.shape");
        this.recipeIngredients = new LinkedHashMap<>();
        ConfigurationSection ingredSection = section.getConfigurationSection("recipe.ingredients");
        if (ingredSection != null) {
            for (String key : ingredSection.getKeys(false)) {
                if (key.length() == 1) {
                    Material m = Material.matchMaterial(ingredSection.getString(key, ""));
                    if (m != null) recipeIngredients.put(key.charAt(0), m);
                }
            }
        }
    }

    // ── Getters ──────────────────────────────────────────────────────────────

    public String getId()                  { return id; }
    public String getDisplayName()         { return displayName; }
    public int getStars()                  { return stars; } // Added stars getter
    public int getClassTag()               { return classTag; }
    public String getType()                { return type; }
    public double getCooldown()            { return cooldown; }
    public int getMaxUses()                { return maxUses; }
    public Material getBaseItem()          { return baseItem; }
    public int getCustomModelData()        { return customModelData; }
    public List<String> getLore()          { return lore; }

    public List<ConfigurationSection> getEffectConfigs() {
        return Collections.unmodifiableList(effectConfigs);
    }

    public List<RelicEffect> getEffects() {
        return Collections.unmodifiableList(effects);
    }

    void addEffect(RelicEffect effect) {
        effects.add(effect);
    }

    public List<String> getRecipeShape()                      { return recipeShape; }
    public Map<Character, Material> getRecipeIngredients()    {
        return Collections.unmodifiableMap(recipeIngredients);
    }
}