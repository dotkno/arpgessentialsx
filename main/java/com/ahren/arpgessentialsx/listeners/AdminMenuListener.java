package com.ahren.arpgessentialsx.listeners;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.gui.AdminMenuGUI;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Listens for clicks inside the Admin Menu GUI.
 */
public final class AdminMenuListener implements Listener {

    private final ARPGEssentialsX plugin;
    private final AdminMenuGUI adminMenuGUI;
    private final LegacyComponentSerializer serializer;

    // Track current page for each player and menu type
    private final Map<UUID, Integer> spellPages = new HashMap<>();
    private final Map<UUID, Integer> relicPages = new HashMap<>();
    private final Map<UUID, Integer> weaponPages = new HashMap<>();
    private final Map<UUID, Integer> armorPages = new HashMap<>();

    public AdminMenuListener(ARPGEssentialsX plugin, AdminMenuGUI adminMenuGUI) {
        this.plugin = plugin;
        this.adminMenuGUI = adminMenuGUI;
        this.serializer = LegacyComponentSerializer.legacyAmpersand();
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        Component title = event.getView().title();
        String titleString = serializer.serialize(title);

        // Main admin menu
        if (title.equals(AdminMenuGUI.GUI_TITLE)) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType().isAir()) return;

            handleMainMenuClick(player, clickedItem);
            return;
        }

        // Spells menu
        if (titleString.startsWith("Spells")) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType().isAir()) return;

            int currentPage = parsePageFromTitle(titleString);
            handleSubmenuClick(player, clickedItem, "Spells", currentPage, spellPages);
            return;
        }

        // Relics menu
        if (titleString.startsWith("Relics")) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType().isAir()) return;

            int currentPage = parsePageFromTitle(titleString);
            handleSubmenuClick(player, clickedItem, "Relics", currentPage, relicPages);
            return;
        }

        // Weapons menu
        if (titleString.startsWith("Weapons")) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType().isAir()) return;

            int currentPage = parsePageFromTitle(titleString);
            handleSubmenuClick(player, clickedItem, "Weapons", currentPage, weaponPages);
            return;
        }

        // Armors menu
        if (titleString.startsWith("Armors")) {
            event.setCancelled(true);
            if (!(event.getWhoClicked() instanceof Player player)) return;

            ItemStack clickedItem = event.getCurrentItem();
            if (clickedItem == null || clickedItem.getType().isAir()) return;

            int currentPage = parsePageFromTitle(titleString);
            handleSubmenuClick(player, clickedItem, "Armors", currentPage, armorPages);
        }
    }

    private void handleMainMenuClick(Player player, ItemStack clickedItem) {
        Material type = clickedItem.getType();
        ItemMeta meta = clickedItem.getItemMeta();
        if (meta == null) return;

        String displayName = serializer.serialize(meta.displayName());

        if (displayName.contains("Spells")) {
            spellPages.put(player.getUniqueId(), 0);
            adminMenuGUI.openSpellsMenu(player, 0);
        } else if (displayName.contains("Relics")) {
            relicPages.put(player.getUniqueId(), 0);
            adminMenuGUI.openRelicsMenu(player, 0);
        } else if (displayName.contains("Weapons")) {
            weaponPages.put(player.getUniqueId(), 0);
            adminMenuGUI.openWeaponsMenu(player, 0);
        } else if (displayName.contains("Armors")) {
            armorPages.put(player.getUniqueId(), 0);
            adminMenuGUI.openArmorsMenu(player, 0);
        }
    }

    private void handleSubmenuClick(Player player, ItemStack clickedItem, String menuType, int currentPage, Map<UUID, Integer> pageMap) {
        // Check for back button
        if (clickedItem.getType() == Material.ARROW) {
            adminMenuGUI.open(player);
            return;
        }

        // Check for previous page button
        if (clickedItem.getType() == Material.PAPER) {
            String displayName = serializer.serialize(clickedItem.getItemMeta().displayName());
            if (displayName.contains("Previous")) {
                int newPage = Math.max(0, currentPage - 1);
                pageMap.put(player.getUniqueId(), newPage);
                openMenuByType(player, menuType, newPage);
                return;
            }
            if (displayName.contains("Next")) {
                int newPage = currentPage + 1;
                pageMap.put(player.getUniqueId(), newPage);
                openMenuByType(player, menuType, newPage);
                return;
            }
        }

        // Give the item to the player
        player.getInventory().addItem(clickedItem.clone());
        player.sendMessage("§aGiven: " + clickedItem.getItemMeta().getDisplayName());
    }

    private void openMenuByType(Player player, String menuType, int page) {
        switch (menuType) {
            case "Spells":
                adminMenuGUI.openSpellsMenu(player, page);
                break;
            case "Relics":
                adminMenuGUI.openRelicsMenu(player, page);
                break;
            case "Weapons":
                adminMenuGUI.openWeaponsMenu(player, page);
                break;
            case "Armors":
                adminMenuGUI.openArmorsMenu(player, page);
                break;
        }
    }

    private int parsePageFromTitle(String title) {
        // Title format: "MenuType - Page X/Y"
        String[] parts = title.split(" - Page ");
        if (parts.length < 2) return 0;
        String pagePart = parts[1].split("/")[0];
        try {
            return Integer.parseInt(pagePart) - 1; // Convert 1-based to 0-based
        } catch (NumberFormatException e) {
            return 0;
        }
    }
}
