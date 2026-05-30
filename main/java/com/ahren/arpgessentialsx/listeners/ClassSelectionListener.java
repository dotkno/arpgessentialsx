package com.ahren.arpgessentialsx.listeners;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.classes.RPGClass;
import com.ahren.arpgessentialsx.gui.ClassSelectionGUI;
import com.ahren.arpgessentialsx.util.ColorUtil;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

/**
 * Listens for clicks inside the Class Selection GUI.
 *
 * Why a separate listener instead of putting this in PlayerJoinListener?
 * Because listeners should do ONE job. PlayerJoin handles joining.
 * ClassSelection handles GUI clicks. This keeps code clean and debuggable.
 */
public final class ClassSelectionListener implements Listener {

    private final ARPGEssentialsX plugin;
    private final NamespacedKey classIdKey;

    public ClassSelectionListener(ARPGEssentialsX plugin, NamespacedKey classIdKey) {
        this.plugin = plugin;
        this.classIdKey = classIdKey;
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        // Step 1: Check if the clicked inventory even has our GUI title
        // If someone clicks in their own inventory or a chest, this fails immediately
        if (event.getView().title().equals(ClassSelectionGUI.GUI_TITLE)) {
            System.out.println("INSIDE GUI CHECK");
            System.out.println("CLICK DETECTED: " + event.getView().getTitle());

            // Step 2: Cancel the event. This prevents players from picking up
            // the class items and putting them in their inventory.
            event.setCancelled(true);

            // Step 3: Make sure a human clicked, not some plugin fake-clicking
            if (!(event.getWhoClicked() instanceof Player player)) return;

            // Step 4: Check if they actually clicked on an item (not an empty slot)
            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType().isAir()) return;

            // Step 5: Read the invisible PDC tag from the clicked item
            PersistentDataContainer pdc = clickedItem.getItemMeta().getPersistentDataContainer();

            if (pdc.has(classIdKey, PersistentDataType.STRING)) {
                // The tag exists! This is definitely a class item.
                String classId = pdc.get(classIdKey, PersistentDataType.STRING);

                // Look up the class definition
                RPGClass rpgClass = plugin.getClassManager().getClass(classId);

                if (rpgClass != null) {
                    // Save the choice to disk
                    plugin.getPlayerDataManager().setPlayerClass(player.getUniqueId(), classId);

                    // Apply the attributes immediately
                    plugin.applyClassToPlayer(player);

                    // Close the GUI
                    player.closeInventory();

                    // Send a success message
                    player.sendMessage(ColorUtil.translate(""));
                    player.sendMessage(ColorUtil.translate("&a&l✦ &aYou have selected the &f"
                            + rpgClass.getDisplayName() + "&a class!"));
                    player.sendMessage(ColorUtil.translate("&7Your attributes have been updated."));
                    player.sendMessage(ColorUtil.translate(""));
                }
            }
        }
    }
}