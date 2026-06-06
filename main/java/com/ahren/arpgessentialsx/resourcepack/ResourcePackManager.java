package com.ahren.arpgessentialsx.resourcepack;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.armors.ArmorManager;
import com.ahren.arpgessentialsx.armors.Armor;
import com.ahren.arpgessentialsx.weapons.WeaponManager;
import com.ahren.arpgessentialsx.weapons.Weapon;
import com.ahren.arpgessentialsx.relics.RelicManager;
import com.ahren.arpgessentialsx.relics.Relic;
import com.ahren.arpgessentialsx.customitems.CustomItemManager;
import com.ahren.arpgessentialsx.customitems.CustomItem;

import java.io.*;
import java.nio.file.*;
import java.util.*;
import java.util.logging.Level;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * Manages resource pack generation for custom items, armors, weapons, and relics.
 * Uses standard Java libraries to generate resource packs programmatically.
 */
public class ResourcePackManager {

    private final ARPGEssentialsX plugin;
    private final File resourcePackFolder;
    private final File texturesFolder;
    private final File modelsFolder;
    private final File bedrockModelsFolder;
    private final File outputPackFile;
    private final File outputBedrockPackFile;

    public ResourcePackManager(ARPGEssentialsX plugin) {
        this.plugin = plugin;
        this.resourcePackFolder = new File(plugin.getDataFolder(), "resourcepack");
        this.texturesFolder = new File(resourcePackFolder, "textures");
        this.modelsFolder = new File(resourcePackFolder, "models");
        this.bedrockModelsFolder = new File(resourcePackFolder, "bedrock_models");
        this.outputPackFile = new File(plugin.getDataFolder(), "arpg-resourcepack.zip");
        this.outputBedrockPackFile = new File(plugin.getDataFolder(), "arpg-resourcepack.mcpack");

        createDirectories();
    }

    private void createDirectories() {
        if (!resourcePackFolder.exists()) {
            resourcePackFolder.mkdirs();
        }
        if (!texturesFolder.exists()) {
            texturesFolder.mkdirs();
        }
        if (!modelsFolder.exists()) {
            modelsFolder.mkdirs();
        }
        if (!bedrockModelsFolder.exists()) {
            bedrockModelsFolder.mkdirs();
        }
    }

    /**
     * Generates the resource pack from current configurations and texture files.
     */
    public void generateResourcePack() {
        plugin.getLogger().info("Starting Java resource pack generation...");

        try {
            // Create a temporary directory for the resource pack structure
            File tempDir = new File(plugin.getDataFolder(), "resourcepack_temp");
            if (tempDir.exists()) {
                deleteDirectory(tempDir);
            }
            tempDir.mkdirs();

            // Create pack.mcmeta
            createPackMeta(tempDir);

            // Generate item models from configurations
            generateArmorModels(tempDir);
            generateWeaponModels(tempDir);
            generateRelicModels(tempDir);
            generateCustomItemModels(tempDir);

            // Copy texture files to resource pack
            copyTextures(tempDir);

            // Copy custom model files if they exist
            copyCustomModels(tempDir);

            // Create ZIP file
            createZipFile(tempDir, outputPackFile);

            // Clean up temp directory
            deleteDirectory(tempDir);

            plugin.getLogger().info("Java resource pack generated successfully: " + outputPackFile.getAbsolutePath());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to generate resource pack", e);
        }
    }

    /**
     * Generates the Bedrock resource pack from current configurations and texture files.
     */
    public void generateBedrockResourcePack() {
        plugin.getLogger().info("Starting Bedrock resource pack generation...");

        try {
            // Create a temporary directory for the Bedrock resource pack structure
            File tempDir = new File(plugin.getDataFolder(), "resourcepack_bedrock_temp");
            if (tempDir.exists()) {
                deleteDirectory(tempDir);
            }
            tempDir.mkdirs();

            // Create manifest.json
            createBedrockManifest(tempDir);

            // Copy texture files to Bedrock resource pack
            copyBedrockTextures(tempDir);

            // Copy Bedrock model files if they exist
            copyBedrockModels(tempDir);

            // Create MCPACK file (ZIP with .mcpack extension)
            createZipFile(tempDir, outputBedrockPackFile);

            // Clean up temp directory
            deleteDirectory(tempDir);

            plugin.getLogger().info("Bedrock resource pack generated successfully: " + outputBedrockPackFile.getAbsolutePath());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to generate Bedrock resource pack", e);
        }
    }

    /**
     * Generates both Java and Bedrock resource packs.
     */
    public void generateAllResourcePacks() {
        generateResourcePack();
        generateBedrockResourcePack();
    }

    private void createPackMeta(File baseDir) throws IOException {
        File assetsDir = new File(baseDir, "assets");
        assetsDir.mkdirs();

        String packMetaContent = "{\"pack\":{\"pack_format\":15,\"description\":\"ARPGEssentialsX Custom Items\"}}";
        File packMetaFile = new File(baseDir, "pack.mcmeta");
        Files.writeString(packMetaFile.toPath(), packMetaContent);
    }

    private void createBedrockManifest(File baseDir) throws IOException {
        String manifestContent = "{\n" +
                "  \"format_version\": 2,\n" +
                "  \"header\": {\n" +
                "    \"description\": \"ARPGEssentialsX Custom Items\",\n" +
                "    \"name\": \"ARPGEssentialsX\",\n" +
                "    \"uuid\": \"00000000-0000-0000-0000-000000000000\",\n" +
                "    \"version\": [1, 0, 0],\n" +
                "    \"min_engine_version\": [1, 16, 0]\n" +
                "  },\n" +
                "  \"modules\": [\n" +
                "    {\n" +
                "      \"description\": \"ARPGEssentialsX Custom Items\",\n" +
                "      \"type\": \"resources\",\n" +
                "      \"uuid\": \"00000000-0000-0000-0000-000000000001\",\n" +
                "      \"version\": [1, 0, 0]\n" +
                "    }\n" +
                "  ]\n" +
                "}";

        File manifestFile = new File(baseDir, "manifest.json");
        Files.writeString(manifestFile.toPath(), manifestContent);
    }

    private void generateArmorModels(File baseDir) throws IOException {
        ArmorManager armorManager = plugin.getArmorManager();
        if (armorManager == null) return;

        for (Armor armor : armorManager.getAllArmors()) {
            if (armor.getCustomModelData() >= 0 && armor.hasResourceConfig()) {
                String texturePath = armor.getTexturePath();
                if (texturePath != null && armor.shouldGenerateModel()) {
                    generateItemModel(baseDir, armor.getId(), texturePath);
                    plugin.getLogger().info("Generated model for armor: " + armor.getId());
                }
            }
        }
    }

    private void generateWeaponModels(File baseDir) throws IOException {
        WeaponManager weaponManager = plugin.getWeaponManager();
        if (weaponManager == null) return;

        for (Weapon weapon : weaponManager.getAllWeapons()) {
            if (weapon.getCustomModelData() >= 0 && weapon.hasResourceConfig()) {
                String texturePath = weapon.getTexturePath();
                if (texturePath != null && weapon.shouldGenerateModel()) {
                    generateItemModel(baseDir, weapon.getId(), texturePath);
                    plugin.getLogger().info("Generated model for weapon: " + weapon.getId());
                }
            }
        }
    }

    private void generateRelicModels(File baseDir) throws IOException {
        RelicManager relicManager = plugin.getRelicManager();
        if (relicManager == null) return;

        for (Relic relic : relicManager.getAllRelics()) {
            if (relic.getCustomModelData() >= 0 && relic.hasResourceConfig()) {
                String texturePath = relic.getTexturePath();
                if (texturePath != null && relic.shouldGenerateModel()) {
                    generateItemModel(baseDir, relic.getId(), texturePath);
                    plugin.getLogger().info("Generated model for relic: " + relic.getId());
                }
            }
        }
    }

    private void generateCustomItemModels(File baseDir) throws IOException {
        CustomItemManager customItemManager = plugin.getCustomItemManager();
        if (customItemManager == null) return;

        for (CustomItem customItem : customItemManager.getAllItems()) {
            if (customItem.getCustomModelData() >= 0 && customItem.hasResourceConfig()) {
                String texturePath = customItem.getTexturePath();
                if (texturePath != null && customItem.shouldGenerateModel()) {
                    generateItemModel(baseDir, customItem.getId(), texturePath);
                    plugin.getLogger().info("Generated model for custom item: " + customItem.getId());
                }
            }
        }
    }

    private void generateItemModel(File baseDir, String itemId, String texturePath) throws IOException {
        // Convert texture path to namespace format
        String namespaceTexture = "arpg:item/" + texturePath.replace(".png", "");

        String modelJson = String.format(
                "{\"parent\":\"minecraft:item/generated\",\"textures\":{\"layer0\":\"%s\"}}",
                namespaceTexture
        );

        File modelFile = new File(baseDir, "assets/minecraft/models/item/" + itemId + ".json");
        modelFile.getParentFile().mkdirs();
        Files.writeString(modelFile.toPath(), modelJson);
    }

    private void copyTextures(File baseDir) throws IOException {
        File[] categoryFolders = texturesFolder.listFiles(File::isDirectory);
        if (categoryFolders == null) return;

        for (File categoryFolder : categoryFolders) {
            File[] textureFiles = categoryFolder.listFiles((dir, name) -> name.endsWith(".png"));
            if (textureFiles == null) continue;

            for (File textureFile : textureFiles) {
                File destDir = new File(baseDir, "assets/arpg/textures/" + categoryFolder.getName());
                destDir.mkdirs();
                Files.copy(textureFile.toPath(), new File(destDir, textureFile.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                plugin.getLogger().info("Added texture: " + categoryFolder.getName() + "/" + textureFile.getName());
            }
        }
    }

    private void copyCustomModels(File baseDir) throws IOException {
        File[] categoryFolders = modelsFolder.listFiles(File::isDirectory);
        if (categoryFolders == null) return;

        for (File categoryFolder : categoryFolders) {
            File[] modelFiles = categoryFolder.listFiles((dir, name) -> name.endsWith(".json"));
            if (modelFiles == null) continue;

            for (File modelFile : modelFiles) {
                File destDir = new File(baseDir, "assets/arpg/models/" + categoryFolder.getName());
                destDir.mkdirs();
                Files.copy(modelFile.toPath(), new File(destDir, modelFile.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                plugin.getLogger().info("Added custom model: " + categoryFolder.getName() + "/" + modelFile.getName());
            }
        }
    }

    private void copyBedrockTextures(File baseDir) throws IOException {
        File[] categoryFolders = texturesFolder.listFiles(File::isDirectory);
        if (categoryFolders == null) return;

        for (File categoryFolder : categoryFolders) {
            File[] textureFiles = categoryFolder.listFiles((dir, name) -> name.endsWith(".png"));
            if (textureFiles == null) continue;

            for (File textureFile : textureFiles) {
                File destDir = new File(baseDir, "textures/" + categoryFolder.getName());
                destDir.mkdirs();
                Files.copy(textureFile.toPath(), new File(destDir, textureFile.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                plugin.getLogger().info("Added Bedrock texture: " + categoryFolder.getName() + "/" + textureFile.getName());
            }
        }
    }

    private void copyBedrockModels(File baseDir) throws IOException {
        File[] categoryFolders = bedrockModelsFolder.listFiles(File::isDirectory);
        if (categoryFolders == null) return;

        for (File categoryFolder : categoryFolders) {
            File[] modelFiles = categoryFolder.listFiles((dir, name) -> name.endsWith(".geo.json"));
            if (modelFiles == null) continue;

            for (File modelFile : modelFiles) {
                File destDir = new File(baseDir, "models/entity/" + categoryFolder.getName());
                destDir.mkdirs();
                Files.copy(modelFile.toPath(), new File(destDir, modelFile.getName()).toPath(), StandardCopyOption.REPLACE_EXISTING);
                plugin.getLogger().info("Added Bedrock model: " + categoryFolder.getName() + "/" + modelFile.getName());
            }
        }
    }

    private void createZipFile(File sourceDir, File zipFile) throws IOException {
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipFile))) {
            zipDirectory(sourceDir, sourceDir.getName(), zos);
        }
    }

    private void zipDirectory(File sourceDir, String basePath, ZipOutputStream zos) throws IOException {
        File[] files = sourceDir.listFiles();
        if (files == null) return;

        for (File file : files) {
            if (file.isDirectory()) {
                zipDirectory(file, basePath + "/" + file.getName(), zos);
            } else {
                String entryPath = basePath + "/" + file.getName();
                ZipEntry entry = new ZipEntry(entryPath);
                zos.putNextEntry(entry);
                Files.copy(file.toPath(), zos);
                zos.closeEntry();
            }
        }
    }

    private void deleteDirectory(File directory) {
        File[] files = directory.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    deleteDirectory(file);
                } else {
                    file.delete();
                }
            }
        }
        directory.delete();
    }

    /**
     * Sends the resource pack to a player.
     */
    public void sendResourcePack(org.bukkit.entity.Player player) {
        if (!outputPackFile.exists()) {
            plugin.getLogger().warning("Resource pack file does not exist. Generate it first with /arpg generatepack");
            return;
        }

        try {
            String packUrl = outputPackFile.toURI().toURL().toString();
            player.setResourcePack(packUrl);
            plugin.getLogger().info("Sent resource pack to " + player.getName());
        } catch (IOException e) {
            plugin.getLogger().log(Level.SEVERE, "Failed to send resource pack to " + player.getName(), e);
        }
    }

    /**
     * Gets the resource pack file.
     */
    public File getResourcePackFile() {
        return outputPackFile;
    }

    /**
     * Gets the Bedrock resource pack file.
     */
    public File getBedrockResourcePackFile() {
        return outputBedrockPackFile;
    }

    /**
     * Gets the textures folder where users should place their texture files.
     */
    public File getTexturesFolder() {
        return texturesFolder;
    }

    /**
     * Gets the models folder where users should place their custom model JSON files.
     */
    public File getModelsFolder() {
        return modelsFolder;
    }

    /**
     * Gets the Bedrock models folder where users should place their .geo.json files.
     */
    public File getBedrockModelsFolder() {
        return bedrockModelsFolder;
    }
}
