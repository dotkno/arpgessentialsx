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
    private final int amount;
    private final boolean stackable;
    private final boolean consumable;
    private final boolean throwable;
    private final boolean block;
    private final boolean unbreakable;

    // ── Resource Pack Support ─────────────────────────────────────────────────────
    private final String texturePath;
    private final String modelPath;
    private final boolean generateModel;
    private final List<String> recipeShape;
    private final Map<Character, String> recipeIngredients; // value can be Material name or custom item id prefix
    private final Map<String, Integer> enchantments;
    private final int consumeHeal;
    private final int consumeHunger;
    private final float consumeSaturation;
    private final List<String> consumeEffects;
    private final int consumeCooldown;
    private final String consumeSound;
    private final String consumeParticle;
    private final int consumeParticleCount;

    // throwable settings
    private final int throwCooldown;
    private final double throwPower;
    private final double throwDamage;
    private final boolean throwExplode;
    private final float throwExplosionPower;
    private final List<String> throwOnHitEffects;

    public CustomItem(String id, ConfigurationSection section) {
        this.id = id;
        this.displayName = section.getString("display_name", id);
        this.material = Material.matchMaterial(section.getString("material", "BARRIER"));
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

        this.amount = Math.max(1, section.getInt("amount", 1));
        // default stackable based on material max stack size when not explicitly set
        if (section.contains("stackable")) this.stackable = section.getBoolean("stackable");
        else this.stackable = (this.material != null && this.material.getMaxStackSize() > 1);
        this.consumable = section.getBoolean("consumable", false);
        this.throwable = section.getBoolean("throwable", false);
        this.block = section.getBoolean("block", false);
        this.unbreakable = section.getBoolean("unbreakable", false);

        this.recipeShape = section.getStringList("recipe.shape");
        this.recipeIngredients = new LinkedHashMap<>();
        this.enchantments = new LinkedHashMap<>();

        ConfigurationSection recipeSection = section.getConfigurationSection("recipe.ingredients");
        if (recipeSection != null) {
            for (String key : recipeSection.getKeys(false)) {
                if (key.length() == 1) {
                    String value = recipeSection.getString(key);
                    if (value != null) recipeIngredients.put(key.charAt(0), value.trim());
                }
            }
        }
        ConfigurationSection ench = section.getConfigurationSection("enchantments");
        if (ench != null) {
            for (String k : ench.getKeys(false)) {
                int lvl = ench.getInt(k, 0);
                if (lvl > 0) enchantments.put(k, lvl);
            }
        }
        this.consumeHeal = section.getInt("consume.heal", 0);
        this.consumeHunger = section.getInt("consume.hunger", 0);
        this.consumeSaturation = (float) section.getDouble("consume.saturation", 0.0);
        this.consumeEffects = section.getStringList("consume.effects");
        this.consumeCooldown = section.getInt("consume.cooldown_seconds", 0);
        this.consumeSound = section.getString("consume.sound", null);
        this.consumeParticle = section.getString("consume.particle", null);
        this.consumeParticleCount = Math.max(0, section.getInt("consume.particle_count", 0));

        this.throwCooldown = section.getInt("throw.cooldown_seconds", 0);
        this.throwPower = section.getDouble("throw.power", 1.5);
        this.throwDamage = section.getDouble("throw.damage", 0.0);
        this.throwExplode = section.getBoolean("throw.explode", false);
        this.throwExplosionPower = (float) section.getDouble("throw.explosion_power", 2.0);
        this.throwOnHitEffects = section.getStringList("throw.on_hit_effects");
    }

    public String getId() { return id; }
    public String getDisplayName() { return displayName; }
    public Material getMaterial() { return material; }
    public int getCustomModelData() { return customModelData; }
    public List<String> getLore() { return lore; }
    public int getAmount() { return amount; }
    public boolean isStackable() { return stackable; }
    public boolean isConsumable() { return consumable; }
    public boolean isThrowable() { return throwable; }
    public boolean isBlock() { return block; }
    public boolean isUnbreakable() { return unbreakable; }
    public List<String> getRecipeShape() { return recipeShape; }
    public Map<Character, String> getRecipeIngredients() { return Collections.unmodifiableMap(recipeIngredients); }
    public Map<String, Integer> getEnchantments() { return Collections.unmodifiableMap(enchantments); }
    public int getConsumeHeal() { return consumeHeal; }
    public int getConsumeHunger() { return consumeHunger; }
    public float getConsumeSaturation() { return consumeSaturation; }
    public List<String> getConsumeEffects() { return consumeEffects; }
    public int getConsumeCooldown() { return consumeCooldown; }
    public String getConsumeSound() { return consumeSound; }
    public String getConsumeParticle() { return consumeParticle; }
    public int getConsumeParticleCount() { return consumeParticleCount; }

    public double getThrowPower() { return throwPower; }
    public double getThrowDamage() { return throwDamage; }
    public boolean isThrowExplode() { return throwExplode; }
    public float getThrowExplosionPower() { return throwExplosionPower; }
    public List<String> getThrowOnHitEffects() { return throwOnHitEffects; }
    public int getThrowCooldown() { return throwCooldown; }

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