package com.ahren.arpgessentialsx.classes;

import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;

import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

/**
 * Represents a single RPG class definition (e.g., Fighter, Mage, Assassin).
 *
 * This is a pure data class — it doesn't DO anything on its own.
 * All data is loaded from classes.yml, so you can add, remove, or edit classes
 * without touching any Java code.
 */
public final class RPGClass {

    /** Internal ID used in config and data storage (e.g., "fighter", "mage") */
    private final String id;

    /** The pretty name shown to players (e.g., "&c&lFighter") */
    private final String displayName;

    /** Description lines shown in the GUI tooltip */
    private final List<String> lore;

    /** The material used as the icon in the GUI (e.g., IRON_SWORD) */
    private final Material iconMaterial;

    /** Custom model data for resource pack support. -1 means "not set" */
    private final int customModelData;

    /**
     * Attribute modifiers: key = config attribute name, value = flat modifier amount.
     * e.g. "health" → 4.0, "damage" → -1.0
     */
    private final Map<String, Double> attributes;

    /**
     * Passive settings: key = passive name, value = numeric config value.
     * e.g. "regen_delay" → 10.0, "crit_multiplier_bonus" → 0.15
     */
    private final Map<String, Double> passives;

    /** Numeric tag for this class (1=Fighter, 2=Mage, etc.) */
    private final int classTag;

    /**
     * Constructs an RPGClass by reading from a config section.
     *
     * @param id      The ID of this class (e.g., "fighter")
     * @param section The ConfigurationSection from classes.yml for this class
     */
    public RPGClass(String id, ConfigurationSection section) {
        this.id = id;
        this.displayName = section.getString("display-name", id);
        this.lore = section.getStringList("lore");
        this.classTag = section.getInt("tag", 0);

        // Safely parse the icon material — defaults to BARRIER if name is invalid
        String materialName = section.getString("icon", "BARRIER");
        Material parsed = Material.matchMaterial(materialName);
        this.iconMaterial = (parsed != null) ? parsed : Material.BARRIER;

        this.customModelData = section.getInt("custom-model-data", -1);

        // Load attributes from the "attributes:" sub-section
        this.attributes = new LinkedHashMap<>();
        ConfigurationSection attrSection = section.getConfigurationSection("attributes");
        if (attrSection != null) {
            for (String key : attrSection.getKeys(false)) {
                String normalizedKey = key.toLowerCase().replace("-", "_");
                double value = attrSection.getDouble(key);
                this.attributes.put(normalizedKey, value);
            }
        }

        // Load passives from the "passives:" sub-section
        this.passives = new LinkedHashMap<>();
        ConfigurationSection passiveSection = section.getConfigurationSection("passives");
        if (passiveSection != null) {
            for (String key : passiveSection.getKeys(false)) {
                String normalizedKey = key.toLowerCase().replace("-", "_");
                double value = passiveSection.getDouble(key);
                this.passives.put(normalizedKey, value);
            }
        }
    }

    /** @return The internal ID of this class (never null) */
    public String getId() {
        return id;
    }

    /** @return The display name, may contain & color codes */
    public String getDisplayName() {
        return displayName;
    }

    /** @return The lore lines for GUI tooltips (never null, may be empty) */
    public List<String> getLore() {
        return lore;
    }

    /** @return The material to use as this class's icon in GUIs */
    public Material getIconMaterial() {
        return iconMaterial;
    }

    /** @return Custom model data value, or -1 if none is set */
    public int getCustomModelData() {
        return customModelData;
    }

    /**
     * @param key The attribute name (e.g., "health", "damage")
     * @return The modifier value, or 0.0 if this class doesn't define that attribute
     */
    public double getAttribute(String key) {
        return attributes.getOrDefault(key.toLowerCase().replace("-", "_"), 0.0);
    }

    /** @return An unmodifiable view of ALL attribute modifiers for this class */
    public Map<String, Double> getAttributes() {
        return Collections.unmodifiableMap(attributes);
    }

    /**
     * @param key The passive name (e.g., "crit_multiplier_bonus")
     * @return The value, or 0.0 if not defined
     */
    public double getPassive(String key) {
        return passives.getOrDefault(key.toLowerCase().replace("-", "_"), 0.0);
    }

    /** @return An unmodifiable view of ALL passive settings for this class */
    public Map<String, Double> getPassives() {
        return Collections.unmodifiableMap(passives);
    }

    /** @return The numerical tag for this class (1=Fighter, 2=Mage, etc.) */
    public int getClassTag() {
        return classTag;
    }

    @Override
    public String toString() {
        return "RPGClass{id='" + id + "', name='" + displayName
                + "', attributes=" + attributes + ", passives=" + passives + "}";
    }
}