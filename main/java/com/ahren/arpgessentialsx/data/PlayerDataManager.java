package com.ahren.arpgessentialsx.data;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;

import java.io.File;
import java.io.IOException;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Manages player data — loading, saving, and storing in memory.
 *
 * How it works:
 *   - On startup: reads playerdata.yml and loads everything into memory (a Map)
 *   - During gameplay: reads from memory (instant, no disk access)
 *   - On class change: saves that one player to disk immediately (crash-safe)
 *   - On shutdown: saves everything to disk one final time
 *
 * Why a Map in memory instead of reading the file every time?
 *   Disk reads are SLOW. Memory reads are INSTANT. If 50 players are online
 *   and you check their class every time they swing a sword, you want that
 *   to take 0 milliseconds — not 10+ milliseconds per file read.
 *
 * Why ConcurrentHashMap instead of HashMap?
 *   Minecraft servers are multi-threaded. The main server thread handles gameplay,
 *   but async tasks (like auto-saving) run on different threads. If a HashMap
 *   is modified from two threads at the same time, it crashes. ConcurrentHashMap
 *   prevents that. This is called "thread safety."
 */
public final class PlayerDataManager {

    private final ARPGEssentialsX plugin;

    /** The physical file on disk */
    private final File dataFile;

    /** The YAML representation of that file */
    private FileConfiguration dataConfig;

    /**
     * All loaded player data, keyed by UUID.
     * ConcurrentHashMap = thread-safe HashMap (explained above).
     */
    private final Map<UUID, PlayerData> playerDataMap;

    public PlayerDataManager(ARPGEssentialsX plugin) {
        this.plugin = plugin;
        this.dataFile = new File(plugin.getDataFolder(), "playerdata.yml");
        this.playerDataMap = new ConcurrentHashMap<>();

        loadAll();
    }

    /**
     * Loads all player data from playerdata.yml into memory.
     *
     * File format looks like:
     *   550e8400-e29b-41d4-a716-446655440000:
     *     class: fighter
     *     active_set_bonuses: gladiators_finale_2pc,dragon_4pc
     *   another-uuid-here:
     *     class: mage
     */
    public void loadAll() {
        // Create the file if it doesn't exist yet
        if (!dataFile.exists()) {
            try {
                dataFile.createNewFile();
            } catch (IOException e) {
                plugin.getLogger().severe("Could not create playerdata.yml!");
                e.printStackTrace();
                return;
            }
        }

        dataConfig = YamlConfiguration.loadConfiguration(dataFile);
        playerDataMap.clear();

        // Every top-level key in playerdata.yml should be a UUID
        for (String key : dataConfig.getKeys(false)) {
            try {
                UUID uuid = UUID.fromString(key);
                String classId = dataConfig.getString(key + ".class");
                String activeSetBonuses = dataConfig.getString(key + ".active_set_bonuses");

                // Store in memory. classId can be null (no class chosen yet)
                playerDataMap.put(uuid, new PlayerData(uuid, classId, activeSetBonuses));

            } catch (IllegalArgumentException e) {
                // This fires if someone manually edited the file
                // and put a non-UUID key in there
                plugin.getLogger().warning(
                        "playerdata.yml: Skipping invalid UUID key: " + key
                );
            }
        }

        plugin.getLogger().info("Loaded data for " + playerDataMap.size() + " player(s).");
    }

    /**
     * Saves ALL player data from memory to disk.
     * Called on server shutdown to make sure nothing is lost.
     */
    public void saveAll() {
        // Rewrite the entire file from scratch from what's in memory.
        // This is cleaner than trying to update individual sections.
        for (Map.Entry<UUID, PlayerData> entry : playerDataMap.entrySet()) {
            String path = entry.getKey().toString();
            dataConfig.set(path + ".class", entry.getValue().getClassId());
            dataConfig.set(path + ".active_set_bonuses", entry.getValue().getActiveSetBonuses());
        }

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save playerdata.yml!");
            e.printStackTrace();
        }
    }

    /**
     * Saves a SINGLE player's data to disk.
     * Called immediately when a player picks a class, so if the server
     * crashes 2 seconds later, their choice is already saved.
     *
     * @param uuid The player to save
     */
    private void savePlayer(UUID uuid) {
        PlayerData data = playerDataMap.get(uuid);
        if (data == null) return;

        String path = uuid.toString();
        dataConfig.set(path + ".class", data.getClassId());
        dataConfig.set(path + ".active_set_bonuses", data.getActiveSetBonuses());

        try {
            dataConfig.save(dataFile);
        } catch (IOException e) {
            plugin.getLogger().severe("Could not save playerdata.yml!");
            e.printStackTrace();
        }
    }

    /**
     * Gets a player's data from memory.
     *
     * @param uuid The player's UUID
     * @return Their PlayerData, or null if they've never joined before
     */
    public PlayerData getPlayerData(UUID uuid) {
        return playerDataMap.get(uuid);
    }

    /**
     * Gets or creates player data. If the player has never joined before,
     * this creates a blank entry for them (no class selected).
     *
     * @param uuid The player's UUID
     * @return Their PlayerData (never null)
     */
    public PlayerData getOrCreatePlayerData(UUID uuid) {
        return playerDataMap.computeIfAbsent(uuid, k -> new PlayerData(uuid, null));
    }

    /**
     * Sets a player's class and saves immediately.
     *
     * @param uuid    The player's UUID
     * @param classId The new class ID (e.g., "fighter"), or null to reset
     */
    public void setPlayerClass(UUID uuid, String classId) {
        PlayerData existing = playerDataMap.get(uuid);
        String activeSetBonuses = existing != null ? existing.getActiveSetBonuses() : null;
        PlayerData data = new PlayerData(uuid, classId, activeSetBonuses);
        playerDataMap.put(uuid, data);
        savePlayer(uuid); // Save to disk RIGHT NOW — crash-safe
    }

    /**
     * Sets a player's active set bonuses and saves immediately.
     *
     * @param uuid             The player's UUID
     * @param activeSetBonuses Serialized active set bonuses (comma-separated)
     */
    public void setActiveSetBonuses(UUID uuid, String activeSetBonuses) {
        PlayerData existing = playerDataMap.get(uuid);
        String classId = existing != null ? existing.getClassId() : null;
        PlayerData data = new PlayerData(uuid, classId, activeSetBonuses);
        playerDataMap.put(uuid, data);
        savePlayer(uuid); // Save to disk RIGHT NOW — crash-safe
    }

    /**
     * @return The raw map of all player data. Needed by background tasks for iteration.
     */
    public Map<UUID, PlayerData> getPlayerDataMap() {
        return playerDataMap;
    }
}