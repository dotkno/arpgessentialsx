package com.ahren.arpgessentialsx.gui;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.classes.RPGClass;
import com.ahren.arpgessentialsx.data.PlayerData;
import com.ahren.arpgessentialsx.util.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * Builds and opens the class selection GUI.
 *
 * Layout: A 3-row chest (27 slots).
 *   Row 1: Filler
 *   Row 2: Classes centered dynamically (works for any number of classes ≤ 7)
 *   Row 3: Filler
 *
 * The GUI is rebuilt fresh every time open() is called, so:
 *   - /arpg reload is instantly reflected without restarting
 *   - The currently selected class is highlighted with an enchant glow
 */
public final class ClassSelectionGUI {

    private final ARPGEssentialsX plugin;

    public static final Component GUI_TITLE =
            LegacyComponentSerializer.legacyAmpersand().deserialize("&8&lChoose Your Class");

    private final NamespacedKey classIdKey;
    private final LegacyComponentSerializer serializer;

    // Filler material — change once here to update the whole GUI
    private static final Material FILLER_MATERIAL = Material.GRAY_STAINED_GLASS_PANE;

    public ClassSelectionGUI(ARPGEssentialsX plugin) {
        this.plugin = plugin;
        this.classIdKey = new NamespacedKey(plugin, "class_id");
        this.serializer = LegacyComponentSerializer.legacyAmpersand();
    }

    /**
     * Opens the class selection GUI for the given player.
     *
     * The inventory is built fresh every call so it always reflects:
     *   - The current classes.yml (post-reload)
     *   - The player's already-selected class (shown with an enchant glow)
     *
     * @param player The player to open the GUI for
     */
    public void open(Player player) {
        Inventory inventory = buildInventory(player);
        player.openInventory(inventory);
    }

    private Inventory buildInventory(Player player) {
        Inventory inventory = Bukkit.createInventory(null, 27, GUI_TITLE);

        // Fill all slots with a blank pane
        ItemStack filler = makeFiller();
        for (int i = 0; i < 27; i++) {
            inventory.setItem(i, filler);
        }

        // Figure out which class this player already has (may be null)
        String currentClassId = null;
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data != null && data.hasClass()) {
            currentClassId = data.getClassId();
        }

        // Place classes centered in row 2 (slots 9–17)
        Collection<RPGClass> classes = plugin.getClassManager().getAllClasses();
        int[] slots = centerSlots(classes.size());
        int i = 0;
        for (RPGClass rpgClass : classes) {
            if (i >= slots.length) break;
            boolean isSelected = rpgClass.getId().equalsIgnoreCase(currentClassId);
            inventory.setItem(slots[i], buildClassItem(rpgClass, isSelected));
            i++;
        }

        return inventory;
    }

    /**
     * Calculates centered slot positions in row 2 (slots 9–17) for N classes.
     *
     * Examples:
     *   1 class  → [13]
     *   2 classes → [12, 14]
     *   5 classes → [11, 12, 13, 14, 15]
     *   7 classes → [9, 10, 11, 12, 13, 14, 15]  (fills the row)
     *
     * Capped at 7 to stay within the row.
     */
    private int[] centerSlots(int count) {
        int capped = Math.min(count, 7);
        int[] slots = new int[capped];
        // Row 2 center is slot 13. Offset left by half the count.
        int start = 13 - (capped / 2);
        for (int i = 0; i < capped; i++) {
            slots[i] = start + i;
        }
        return slots;
    }

    private ItemStack buildClassItem(RPGClass rpgClass, boolean isSelected) {
        ItemStack item = new ItemStack(rpgClass.getIconMaterial());
        ItemMeta meta = item.getItemMeta();

        // Display name
        String coloredName = ColorUtil.translate(rpgClass.getDisplayName());
        meta.displayName(serializer.deserialize(coloredName));

        // Lore — start with the class lore from the config
        List<Component> loreComponents = new ArrayList<>(
                rpgClass.getLore().stream()
                        .map(ColorUtil::translate)
                        .map(serializer::deserialize)
                        .toList()
        );

        // Append a "✔ Currently selected" line if this is the player's class
        if (isSelected) {
            loreComponents.add(Component.empty());
            loreComponents.add(serializer.deserialize(ColorUtil.translate("&a&l✔ Currently Selected")));
        }

        meta.lore(loreComponents);

        // Apply enchant glow to the selected class icon (hidden enchantment, just the shimmer)
        if (isSelected) {
            meta.addEnchant(Enchantment.UNBREAKING, 1, true);
            meta.addItemFlags(ItemFlag.HIDE_ENCHANTS);
        }

        if (rpgClass.getCustomModelData() != -1) {
            meta.setCustomModelData(rpgClass.getCustomModelData());
        }

        // Tag the item with the class ID so ClassSelectionListener can read it
        meta.getPersistentDataContainer().set(
                classIdKey,
                PersistentDataType.STRING,
                rpgClass.getId()
        );

        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeFiller() {
        ItemStack filler = new ItemStack(FILLER_MATERIAL);
        ItemMeta meta = filler.getItemMeta();
        meta.displayName(Component.text(" "));
        filler.setItemMeta(meta);
        return filler;
    }

    public NamespacedKey getClassIdKey() {
        return classIdKey;
    }
}