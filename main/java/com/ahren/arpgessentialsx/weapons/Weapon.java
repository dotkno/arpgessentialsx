package com.ahren.arpgessentialsx.weapons;

import com.ahren.arpgessentialsx.weapons.passives.WeaponPassive;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.*;

/**
 * Represents a single custom weapon definition loaded from weapons.yml.
 *
 * Fields over the original:
 *   - weapon_type: sword | claymore | polearm | dagger | bow | catalyst
 *   - stars: 1–5 rarity (1-3 = no skill, 4-5 = can have skill)
 *   - natural_classes: class tags that use this weapon at full power
 *   - skill.class_tag: which class can activate the skill (-1 = any natural class)
 *   - passives: list of passive configs resolved into WeaponPassive instances (NEW)
 */
public final class Weapon {

    private final String id;
    private final String displayName;
    private final WeaponType weaponType;
    private final int stars;
    private final List<Integer> naturalClasses;
    private final Material baseItem;
    private final int customModelData;
    private final int maxDurability;
    private final double damageBonus;
    private final double attackSpeedBonus;

    // ── Resource Pack Support ─────────────────────────────────────────────────────
    private final String texturePath;
    private final String modelPath;
    private final boolean generateModel;

    // ── On-hit effects ────────────────────────────────────────────────────────
    private final List<ConfigurationSection> onHitEffectConfigs;
    private final List<WeaponEffect> onHitEffects = new ArrayList<>();

    // ── Skill (optional, typically 4-5 star only) ─────────────────────────────
    private final String skillName;
    private final double skillCooldown;
    private final int skillClassTag;
    private final List<ConfigurationSection> skillEffectConfigs;
    private final List<WeaponEffect> skillEffects = new ArrayList<>();

    // ── Passives ──────────────────────────────────────────────────────────────
    private final List<ConfigurationSection> passiveConfigs;
    private final List<WeaponPassive> passives = new ArrayList<>();

    // ── Lore / Recipe ─────────────────────────────────────────────────────────
    private final List<String> lore;
    private final List<String> recipeShape;
    private final Map<Character, Material> recipeIngredients;

    public Weapon(String id, ConfigurationSection section) {
        this.id              = id;
        this.displayName     = section.getString("display_name", id);
        this.weaponType      = WeaponType.fromString(section.getString("weapon_type", "sword"));
        this.stars           = Math.max(1, Math.min(5, section.getInt("stars", 1)));
        this.customModelData = section.getInt("custom_model_data", -1);
        this.maxDurability   = section.getInt("durability", -1); // -1 = use vanilla durability
        this.lore            = section.getStringList("lore");

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

        // Natural classes — overrides weapon type defaults if explicitly set
        List<Integer> nc = new ArrayList<>();
        List<?> ncList = section.getList("natural_classes");
        if (ncList != null) {
            for (Object o : ncList) {
                if (o instanceof Integer i) nc.add(i);
                else if (o instanceof Number n) nc.add(n.intValue());
            }
        }
        this.naturalClasses = nc.isEmpty() ? weaponType.getNaturalClasses() : nc;

        // Base item
        String matName = section.getString("base_item", "IRON_SWORD");
        Material mat = Material.matchMaterial(matName);
        this.baseItem = (mat != null) ? mat : Material.IRON_SWORD;

        // Combat stats
        ConfigurationSection combat = section.getConfigurationSection("combat");
        this.damageBonus      = combat != null ? combat.getDouble("damage_bonus", 0.0)      : 0.0;
        this.attackSpeedBonus = combat != null ? combat.getDouble("attack_speed_bonus", 0.0) : 0.0;

        // On-hit effects
        this.onHitEffectConfigs = parseEffectList(section, "effects_on_hit");

        // Skill
        ConfigurationSection skillSection = section.getConfigurationSection("skill");
        if (skillSection != null) {
            this.skillName          = skillSection.getString("name", "&fSkill");
            this.skillCooldown      = skillSection.getDouble("cooldown", 10.0);
            this.skillClassTag      = skillSection.getInt("class_tag", -1);
            this.skillEffectConfigs = parseEffectList(skillSection, "effects");
        } else {
            this.skillName          = null;
            this.skillCooldown      = 10.0;
            this.skillClassTag      = -1;
            this.skillEffectConfigs = new ArrayList<>();
        }

        // Passives
        this.passiveConfigs = parseEffectList(section, "passives");

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

    // ── Internal setters (called by WeaponManager after resolution) ───────────
    void addOnHitEffect(WeaponEffect e)  { onHitEffects.add(e); }
    void addSkillEffect(WeaponEffect e)  { skillEffects.add(e); }
    void addPassive(WeaponPassive p)     { passives.add(p); }

    // ── Proficiency helpers ───────────────────────────────────────────────────

    /** Returns true if the given class tag is a natural user of this weapon. */
    public boolean isNaturalFor(int classTag) {
        return naturalClasses.contains(classTag);
    }

    /**
     * Returns the damage multiplier for a given class tag.
     * Natural classes = 1.0. Others = weapon type's off-class multiplier.
     */
    public double getDamageMultiplierFor(int classTag) {
        if (isNaturalFor(classTag)) return 1.0;
        return weaponType.getOffClassDamageMultiplier();
    }

    /**
     * Returns the attack speed modifier for an off-class player.
     * 0.0 for natural classes (no penalty).
     */
    public double getSpeedPenaltyFor(int classTag) {
        if (isNaturalFor(classTag)) return 0.0;
        return weaponType.getOffClassSpeedModifier();
    }

    // ── Getters ───────────────────────────────────────────────────────────────
    public String getId()                    { return id; }
    public String getDisplayName()           { return displayName; }
    public WeaponType getWeaponType()        { return weaponType; }
    public int getStars()                    { return stars; }
    public List<Integer> getNaturalClasses() { return naturalClasses; }
    public Material getBaseItem()            { return baseItem; }
    public int getCustomModelData()          { return customModelData; }
    public int getMaxDurability()            { return maxDurability; }
    public boolean hasCustomDurability()     { return maxDurability > 0; }
    public double getDamageBonus()           { return damageBonus; }
    public double getAttackSpeedBonus()      { return attackSpeedBonus; }
    public List<String> getLore()            { return lore; }
    public List<String> getRecipeShape()     { return recipeShape; }
    public Map<Character, Material> getRecipeIngredients() {
        return Collections.unmodifiableMap(recipeIngredients);
    }

    public List<ConfigurationSection> getOnHitEffectConfigs() {
        return Collections.unmodifiableList(onHitEffectConfigs);
    }
    public List<WeaponEffect> getOnHitEffects() {
        return Collections.unmodifiableList(onHitEffects);
    }

    public boolean hasSkill()              { return skillName != null && !skillEffects.isEmpty(); }
    public String getSkillName()           { return skillName; }
    public double getSkillCooldown()       { return skillCooldown; }
    public int getSkillClassTag()          { return skillClassTag; }
    public List<ConfigurationSection> getSkillEffectConfigs() {
        return Collections.unmodifiableList(skillEffectConfigs);
    }
    public List<WeaponEffect> getSkillEffects() {
        return Collections.unmodifiableList(skillEffects);
    }

    public List<ConfigurationSection> getPassiveConfigs() {
        return Collections.unmodifiableList(passiveConfigs);
    }
    public List<WeaponPassive> getPassives() {
        return Collections.unmodifiableList(passives);
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