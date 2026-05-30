package com.ahren.arpgessentialsx.classes;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Manages all RPG class definitions.
 *
 * Responsibilities:
 *   1. Extract classes.yml from the jar on first run
 *   2. Read and parse every class defined in classes.yml
 *   3. Store them for quick lookup by ID
 *   4. Support reloading without restarting the server
 *
 * This class does NOT handle applying attributes to players.
 * It only stores the DEFINITIONS. Think of it as a dictionary —
 * it knows what words mean, but it doesn't write sentences for you.
 */
public final class ClassManager {

    /** Reference to the plugin instance */
    private final ARPGEssentialsX plugin;

    /** The physical file on disk (plugins/ARPGEssentialsX/classes.yml) */
    private final File classesFile;

    /** The parsed YAML representation of that file */
    private FileConfiguration classesConfig;

    /**
     * All loaded classes, keyed by their lowercase ID.
     *
     * LinkedHashMap preserves insertion order — meaning classes appear
     * in the GUI in the exact same order you wrote them in classes.yml.
     * This is intentional. If you want Fighter first in the GUI,
     * write Fighter first in the file.
     */
    private final Map<String, RPGClass> classes;

    /**
     * Creates the manager and loads all classes immediately.
     *
     * @param plugin The plugin instance
     */
    public ClassManager(ARPGEssentialsX plugin) {
        this.plugin = plugin;
        this.classes = new LinkedHashMap<>();
        this.classesFile = new File(plugin.getDataFolder(), "classes.yml");

        loadClasses();
    }

    /**
     * Saves the default classes.yml from inside the jar if it doesn't
     * exist on disk yet, then loads all class definitions from it.
     *
     * The "false" parameter in saveResource means:
     *   false = do NOT overwrite if the file already exists
     *   (so server owners don't lose their edits on restart)
     */
    public void loadClasses() {
        plugin.saveResource("classes.yml", false);

        classesConfig = YamlConfiguration.loadConfiguration(classesFile);
        classes.clear();

        // Every top-level key in classes.yml is a class ID
        for (String classId : classesConfig.getKeys(false)) {
            ConfigurationSection section = classesConfig.getConfigurationSection(classId);

            // Defensive check: if someone puts a raw value instead of a section, skip it
            if (section == null) {
                plugin.getLogger().warning(
                        "classes.yml: '" + classId + "' is not a valid class section. Skipping."
                );
                continue;
            }

            try {
                RPGClass rpgClass = new RPGClass(classId, section);
                classes.put(classId.toLowerCase(), rpgClass);
            } catch (Exception e) {
                // Don't crash the whole plugin just because one class is broken
                plugin.getLogger().warning(
                        "classes.yml: Failed to load class '" + classId + "': " + e.getMessage()
                );
            }
        }

        plugin.getLogger().info("Loaded " + classes.size() + " RPG class(es): " + classes.keySet());
    }

    /**
     * Reloads classes from disk. Called by the reload command (built later).
     */
    public void reload() {
        loadClasses();
    }

    /**
     * Gets an RPG class by its ID.
     *
     * @param id The class ID (case-insensitive)
     * @return The RPGClass object, or null if not found
     */
    public RPGClass getClass(String id) {
        if (id == null) return null;
        return classes.get(id.toLowerCase());
    }

    /**
     * @return An unmodifiable collection of ALL loaded classes, in config order.
     *         Unmodifiable so nobody can accidentally add/remove classes at runtime.
     */
    public Collection<RPGClass> getAllClasses() {
        return Collections.unmodifiableCollection(classes.values());
    }

    /**
     * @return The number of loaded classes
     */
    public int getClassCount() {
        return classes.size();
    }

    /**
     * Checks whether a class with the given ID exists.
     *
     * @param id The class ID (case-insensitive)
     * @return true if a class with this ID is loaded
     */
    public boolean classExists(String id) {
        if (id == null) return false;
        return classes.containsKey(id.toLowerCase());
    }
}