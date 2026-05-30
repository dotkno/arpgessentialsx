package com.ahren.arpgessentialsx.weapons;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a single catalyst/wand definition loaded from catalysts.yml.
 *
 * Catalysts are offhand items for Mages that amplify their spells.
 * Star rating determines how strong the multiplier is.
 * Non-Mages holding a catalyst as a weapon deal 0 damage.
 *
 * Naming: tag2catalyst<number> — always class tag 2 (Mage).
 */
public final class Catalyst {

    private final String id;
    private final String displayName;
    private final int stars;
    private final Material baseItem;
    private final int customModelData;
    private final List<String> lore;
    private final List<String> recipeShape;
    private final Map<Character, Material> recipeIngredients;

    /** Pre-built multiplier for this catalyst's star rating */
    private final CatalystMultiplier multiplier;

    public Catalyst(String id, ConfigurationSection section) {
        this.id              = id;
        this.displayName     = section.getString("display_name", id);
        this.stars           = Math.max(1, Math.min(5, section.getInt("stars", 1)));
        this.customModelData = section.getInt("custom_model_data", -1);
        this.lore            = section.getStringList("lore");
        this.multiplier      = new CatalystMultiplier(stars);

        String matName = section.getString("base_item", "BLAZE_ROD");
        Material mat = Material.matchMaterial(matName);
        this.baseItem = (mat != null) ? mat : Material.BLAZE_ROD;

        this.recipeShape = section.getStringList("recipe.shape");
        this.recipeIngredients = new LinkedHashMap<>();
        ConfigurationSection recipeSection = section.getConfigurationSection("recipe.ingredients");
        if (recipeSection != null) {
            for (String key : recipeSection.getKeys(false)) {
                if (key.length() == 1) {
                    Material m = Material.matchMaterial(recipeSection.getString(key, ""));
                    if (m != null) recipeIngredients.put(key.charAt(0), m);
                }
            }
        }
    }

    public String getId()                   { return id; }
    public String getDisplayName()          { return displayName; }
    public int getStars()                   { return stars; }
    public Material getBaseItem()           { return baseItem; }
    public int getCustomModelData()         { return customModelData; }
    public List<String> getLore()           { return lore; }
    public CatalystMultiplier getMultiplier() { return multiplier; }
    public List<String> getRecipeShape()    { return recipeShape; }
    public Map<Character, Material> getRecipeIngredients() {
        return Collections.unmodifiableMap(recipeIngredients);
    }
}