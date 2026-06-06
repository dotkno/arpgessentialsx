package com.ahren.arpgessentialsx.drops;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.customitems.CustomItem;
import com.ahren.arpgessentialsx.customitems.CustomItemManager;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.entity.EntityType;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.util.*;

public final class DropManager {

    private final ARPGEssentialsX plugin;
    private final File dropsFile;
    private FileConfiguration dropsConfig;

    private final Map<EntityType, List<DropEntry>> mobDrops = new HashMap<>();
    private final Map<Material, List<DropEntry>> miningDrops = new HashMap<>();

    public DropManager(ARPGEssentialsX plugin) {
        this.plugin = plugin;
        this.dropsFile = new File(plugin.getDataFolder(), "drops.yml");
        loadDrops();
    }

    public void loadDrops() {
        plugin.saveResource("drops.yml", false);
        dropsConfig = YamlConfiguration.loadConfiguration(dropsFile);
        mobDrops.clear();
        miningDrops.clear();

        // Load mob drops
        ConfigurationSection mobSection = dropsConfig.getConfigurationSection("mob_drops");
        if (mobSection != null) {
            for (String mobName : mobSection.getKeys(false)) {
                try {
                    EntityType entityType = EntityType.valueOf(mobName.toUpperCase());
                    List<DropEntry> drops = new ArrayList<>();
                    List<?> dropList = mobSection.getList(mobName);
                    if (dropList != null) {
                        for (Object dropObj : dropList) {
                            if (dropObj instanceof ConfigurationSection) {
                                drops.add(new DropEntry((ConfigurationSection) dropObj));
                            }
                        }
                    }
                    mobDrops.put(entityType, drops);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("[DropManager] Invalid mob type: " + mobName);
                }
            }
        }

        // Load mining drops
        ConfigurationSection miningSection = dropsConfig.getConfigurationSection("mining_drops");
        if (miningSection != null) {
            for (String blockName : miningSection.getKeys(false)) {
                try {
                    Material material = Material.valueOf(blockName.toUpperCase());
                    List<DropEntry> drops = new ArrayList<>();
                    List<?> dropList = miningSection.getList(blockName);
                    if (dropList != null) {
                        for (Object dropObj : dropList) {
                            if (dropObj instanceof ConfigurationSection) {
                                drops.add(new DropEntry((ConfigurationSection) dropObj));
                            }
                        }
                    }
                    miningDrops.put(material, drops);
                } catch (IllegalArgumentException e) {
                    plugin.getLogger().warning("[DropManager] Invalid block type: " + blockName);
                }
            }
        }

        plugin.getLogger().info("[DropManager] Loaded " + mobDrops.size() + " mob drop tables and " + miningDrops.size() + " mining drop tables.");
    }

    public List<DropEntry> getMobDrops(EntityType entityType) {
        return mobDrops.getOrDefault(entityType, Collections.emptyList());
    }

    public List<DropEntry> getMiningDrops(Material material) {
        return miningDrops.getOrDefault(material, Collections.emptyList());
    }

    public ItemStack createDropItem(String itemId) {
        CustomItemManager customItemManager = plugin.getCustomItemManager();
        if (customItemManager == null) return null;

        CustomItem customItem = customItemManager.getItem(itemId);
        if (customItem != null) {
            return customItemManager.createItem(customItem);
        }

        // Fallback to vanilla material
        Material material = Material.matchMaterial(itemId);
        if (material != null) {
            return new ItemStack(material);
        }

        return null;
    }

    public void reload() {
        loadDrops();
    }

    public static class DropEntry {
        private final String itemId;
        private final double chance;
        private final int minAmount;
        private final int maxAmount;

        public DropEntry(ConfigurationSection config) {
            this.itemId = config.getString("item");
            this.chance = config.getDouble("chance", 0.0);
            this.minAmount = config.getInt("min_amount", 1);
            this.maxAmount = config.getInt("max_amount", 1);
        }

        public String getItemId() {
            return itemId;
        }

        public double getChance() {
            return chance;
        }

        public int getMinAmount() {
            return minAmount;
        }

        public int getMaxAmount() {
            return maxAmount;
        }

        public int rollAmount() {
            if (minAmount == maxAmount) return minAmount;
            Random random = new Random();
            return minAmount + random.nextInt(maxAmount - minAmount + 1);
        }

        public boolean rollChance() {
            Random random = new Random();
            return random.nextDouble() * 100 < chance;
        }
    }
}
