package com.ahren.arpgessentialsx.customitems;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public final class CustomItem {
    private final String id;
    private final String displayName;
    private final Material material;
    private final int customModelData;
    private final List<String> lore;
    private final List<String> recipeShape;
    private final Map<Character, Material> recipeIngredients;

    public CustomItem(String id, ConfigurationSection section) {
        this.id = id;
        this.displayName = section.getString("display_name", id);
        this.material = Material.matchMaterial(section.getString("material", "BARRIER"));
        this.customModelData = section.getInt("custom_model_data", -1);
        this.lore = section.getStringList("lore");

        this.recipeShape = section.getStringList("recipe.shape");
        this.recipeIngredients = new LinkedHashMap<>();
        ConfigurationSection recipeSection = section.getConfigurationSection("recipe.ingredients");
        if (recipeSection != null) {
            for (String key : recipeSection.getKeys(false)) {
                if (key.length() == 1) {
                    Material mat = Material.matchMaterial(recipeSection.getString(key));
                    if (mat != null) recipeIngredients.put(key.charAt(0), mat);
                }
            }
        }
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Material getMaterial() { return material; }
    public int getCustomModelData() { return customModelData; }
    public List<String> getLore() { return lore; }
    public List<String> getRecipeShape() { return recipeShape; }
    public Map<Character, Material> getRecipeIngredients() { return Collections.unmodifiableMap(recipeIngredients); }
}