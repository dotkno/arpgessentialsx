package com.ahren.arpgessentialsx.armors;

import com.ahren.arpgessentialsx.armors.passives.ArmorPassive;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Represents a single custom armor definition loaded from armors.yml.
 *
 * Features:
 *   - armor_type: head | chest | legs | feet
 *   - stars: 1–5 rarity
 *   - base stats: armor_points, armor_toughness, durability
 *   - set_name: for set bonuses (2-piece and 4-piece)
 *   - passives: list of passive configs resolved into ArmorPassive instances
 *   - 1-2 star: only 2-piece set bonus
 *   - 3-5 star: both 2-piece and 4-piece set bonuses
 */
public final class Armor {

    private final String id;
    private final String displayName;
    private final ArmorType armorType;
    private final int stars;
    private final String setName; // For set bonuses
    private final Material baseItem;
    private final int customModelData;
    private final int durability;
    private final double armorPoints;
    private final double armorToughness;

    // ── Resource Pack Support ─────────────────────────────────────────────────────
    private final String texturePath;
    private final String modelPath;
    private final boolean generateModel;

    // ── Passives ──────────────────────────────────────────────────────────────
    private final List<ConfigurationSection> passiveConfigs;
    private final List<ArmorPassive> passives = new ArrayList<>();

    // ── Set Bonuses (separate from passives) ────────────────────────────────────
    private final ConfigurationSection twoPieceBonusConfig;
    private final ConfigurationSection fourPieceBonusConfig;

    // ── Lore / Recipe ─────────────────────────────────────────────────────────
    private final List<String> lore;
    private final List<String> recipeShape;
    private final Map<Character, Material> recipeIngredients;

    public Armor(String id, ConfigurationSection section) {
        this.id = id;
        this.displayName = section.getString("display_name", id);
        this.armorType = ArmorType.fromString(section.getString("armor_type", "chest"));
        this.stars = Math.max(1, Math.min(5, section.getInt("stars", 1)));
        this.setName = section.getString("set_name", null);
        this.customModelData = section.getInt("custom_model_data", -1);
        this.lore = section.getStringList("lore");

        // Resource pack configuration
        ConfigurationSection resourceSection = section.getConfigurationSection("resource");
        if (resourceSection != null) {
            this.texturePath = resourceSection.getString("texture", null);
            this.modelPath = resourceSection.getString("model", null);
            this.generateModel = resourceSection.getBoolean("generate", true);
        } else {
            this.texturePath = null;
            this.modelPath = null;
            this.generateModel = true;
        }

        // Base item
        String matName = section.getString("base_item", "IRON_CHESTPLATE");
        Material mat = Material.matchMaterial(matName);
        this.baseItem = (mat != null) ? mat : Material.IRON_CHESTPLATE;

        // Base stats
        ConfigurationSection stats = section.getConfigurationSection("stats");
        this.armorPoints = stats != null ? stats.getDouble("armor_points", 0.0) : 0.0;
        this.armorToughness = stats != null ? stats.getDouble("armor_toughness", 0.0) : 0.0;
        this.durability = section.getInt("durability", -1); // -1 = use vanilla durability

        // Passives
        this.passiveConfigs = parseEffectList(section, "passives");

        // Set bonuses
        ConfigurationSection setBonuses = section.getConfigurationSection("set_bonuses");
        if (setBonuses != null) {
            this.twoPieceBonusConfig = setBonuses.getConfigurationSection("two_piece");
            this.fourPieceBonusConfig = setBonuses.getConfigurationSection("four_piece");
        } else {
            this.twoPieceBonusConfig = null;
            this.fourPieceBonusConfig = null;
        }

        // Recipe
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

    private List<ConfigurationSection> parseEffectList(ConfigurationSection parent, String key) {
        List<ConfigurationSection> result = new ArrayList<>();
        List<?> list = parent.getList(key);
        if (list == null) return result;
        int i = 0;
        for (Object obj : list) {
            if (obj instanceof Map) {
                ConfigurationSection sec = parent.createSection("_eff_" + key + "_" + i++);
                ((Map<?, ?>) obj).forEach((k, v) -> sec.set(k.toString(), v));
                result.add(sec);
            }
        }
        return result;
    }

    // ── Internal setters (called by ArmorManager after resolution) ───────────
    void addPassive(ArmorPassive p) {
        passives.add(p);
    }

    // ── Set Bonus Helpers ─────────────────────────────────────────────────────
    public boolean hasSetName() {
        return setName != null && !setName.isEmpty();
    }

    public boolean hasTwoPieceBonus() {
        return twoPieceBonusConfig != null;
    }

    public boolean hasFourPieceBonus() {
        return fourPieceBonusConfig != null && stars >= 3;
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    public ArmorType getArmorType() {
        return armorType;
    }

    public int getStars() {
        return stars;
    }

    public String getSetName() {
        return setName;
    }

    public Material getBaseItem() {
        return baseItem;
    }

    public int getCustomModelData() {
        return customModelData;
    }

    public int getDurability() {
        return durability;
    }

    public boolean hasCustomDurability() {
        return durability > 0;
    }

    public double getArmorPoints() {
        return armorPoints;
    }

    public double getArmorToughness() {
        return armorToughness;
    }

    public List<String> getLore() {
        return lore;
    }

    public List<String> getRecipeShape() {
        return recipeShape;
    }

    public Map<Character, Material> getRecipeIngredients() {
        return Collections.unmodifiableMap(recipeIngredients);
    }

    public List<ConfigurationSection> getPassiveConfigs() {
        return Collections.unmodifiableList(passiveConfigs);
    }

    public List<ArmorPassive> getPassives() {
        return Collections.unmodifiableList(passives);
    }

    public ConfigurationSection getTwoPieceBonusConfig() {
        return twoPieceBonusConfig;
    }

    public ConfigurationSection getFourPieceBonusConfig() {
        return fourPieceBonusConfig;
    }

    // ── Resource Pack Getters ────────────────────────────────────────────────────
    public String getTexturePath() {
        return texturePath;
    }

    public String getModelPath() {
        return modelPath;
    }

    public boolean shouldGenerateModel() {
        return generateModel;
    }

    public boolean hasResourceConfig() {
        return texturePath != null;
    }
}
