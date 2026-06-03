package com.ahren.arpgessentialsx.gui;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.armors.Armor;
import com.ahren.arpgessentialsx.armors.ArmorManager;
import com.ahren.arpgessentialsx.customitems.CustomItem;
import com.ahren.arpgessentialsx.customitems.CustomItemManager;
import com.ahren.arpgessentialsx.relics.Relic;
import com.ahren.arpgessentialsx.relics.RelicManager;
import com.ahren.arpgessentialsx.spells.Spell;
import com.ahren.arpgessentialsx.spells.SpellManager;
import com.ahren.arpgessentialsx.util.ColorUtil;
import com.ahren.arpgessentialsx.weapons.Weapon;
import com.ahren.arpgessentialsx.weapons.WeaponManager;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Admin menu GUI for testing - provides access to all spells, relics, weapons, and armors.
 */
public final class AdminMenuGUI {

    private final ARPGEssentialsX plugin;
    private final SpellManager spellManager;
    private final RelicManager relicManager;
    private final WeaponManager weaponManager;
    private final ArmorManager armorManager;
    private final CustomItemManager customItemManager;
    private final LegacyComponentSerializer serializer;

    public static final Component GUI_TITLE =
            LegacyComponentSerializer.legacyAmpersand().deserialize("&c&lAdmin Item Menu");

    public AdminMenuGUI(ARPGEssentialsX plugin) {
        this.plugin = plugin;
        this.spellManager = plugin.getSpellManager();
        this.relicManager = plugin.getRelicManager();
        this.weaponManager = plugin.getWeaponManager();
        this.armorManager = plugin.getArmorManager();
        this.customItemManager = plugin.getCustomItemManager();
        this.serializer = LegacyComponentSerializer.legacyAmpersand();
    }

    public void open(Player player) {
        Inventory inventory = buildInventory();
        player.openInventory(inventory);
    }

    private Inventory buildInventory() {
        // 6 rows (54 slots) for categories
        Inventory inventory = Bukkit.createInventory(null, 54, GUI_TITLE);

        // Fill with glass panes
        ItemStack filler = makeFiller();
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, filler);
        }

        // Category buttons in row 1 (slots 10-16)
        inventory.setItem(10, makeCategoryButton("Spells", Material.ENCHANTED_BOOK, "&e&lSpells", "&7Click to view all spells"));
        inventory.setItem(12, makeCategoryButton("Relics", Material.DRAGON_EGG, "&d&lRelics", "&7Click to view all relics"));
        inventory.setItem(14, makeCategoryButton("Weapons", Material.DIAMOND_SWORD, "&b&lWeapons", "&7Click to view all weapons"));
        inventory.setItem(16, makeCategoryButton("Armors", Material.DIAMOND_CHESTPLATE, "&a&lArmors", "&7Click to view all armors"));
        inventory.setItem(18, makeCategoryButton("Custom Items", Material.AMETHYST_SHARD, "&6&lCustom Items", "&7Click to view all custom items"));

        return inventory;
    }

    public void openSpellsMenu(Player player, int page) {
        List<Spell> allSpells = new ArrayList<>(spellManager.getAllSpells());
        int itemsPerPage = 45;
        int totalPages = (int) Math.ceil((double) allSpells.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
        page = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inventory = Bukkit.createInventory(null, 54, Component.text("Spells - Page " + (page + 1) + "/" + totalPages));
        ItemStack filler = makeFiller();
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, filler);
        }

        // Back button
        inventory.setItem(45, makeBackButton());

        // Previous page button
        if (page > 0) {
            inventory.setItem(46, makePreviousPageButton());
        }

        // Next page button
        if (page < totalPages - 1) {
            inventory.setItem(47, makeNextPageButton());
        }

        int startIndex = page * itemsPerPage;
        int slot = 0;
        for (int i = startIndex; i < allSpells.size() && slot < itemsPerPage; i++) {
            Spell spell = allSpells.get(i);
            ItemStack spellBook = spellManager.getBookFactory().createSpellBook(spell);
            inventory.setItem(slot, spellBook);
            slot++;
        }

        player.openInventory(inventory);
    }

    public void openRelicsMenu(Player player, int page) {
        List<Relic> allRelics = new ArrayList<>(relicManager.getAllRelics());
        int itemsPerPage = 45;
        int totalPages = (int) Math.ceil((double) allRelics.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
        page = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inventory = Bukkit.createInventory(null, 54, Component.text("Relics - Page " + (page + 1) + "/" + totalPages));
        ItemStack filler = makeFiller();
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, filler);
        }

        // Back button
        inventory.setItem(45, makeBackButton());

        // Previous page button
        if (page > 0) {
            inventory.setItem(46, makePreviousPageButton());
        }

        // Next page button
        if (page < totalPages - 1) {
            inventory.setItem(47, makeNextPageButton());
        }

        int startIndex = page * itemsPerPage;
        int slot = 0;
        for (int i = startIndex; i < allRelics.size() && slot < itemsPerPage; i++) {
            Relic relic = allRelics.get(i);
            ItemStack relicItem = relicManager.getItemFactory().createRelic(relic);
            inventory.setItem(slot, relicItem);
            slot++;
        }

        player.openInventory(inventory);
    }

    public void openWeaponsMenu(Player player, int page) {
        List<Weapon> allWeapons = new ArrayList<>(weaponManager.getAllWeapons());
        int itemsPerPage = 45;
        int totalPages = (int) Math.ceil((double) allWeapons.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
        page = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inventory = Bukkit.createInventory(null, 54, Component.text("Weapons - Page " + (page + 1) + "/" + totalPages));
        ItemStack filler = makeFiller();
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, filler);
        }

        // Back button
        inventory.setItem(45, makeBackButton());

        // Previous page button
        if (page > 0) {
            inventory.setItem(46, makePreviousPageButton());
        }

        // Next page button
        if (page < totalPages - 1) {
            inventory.setItem(47, makeNextPageButton());
        }

        int startIndex = page * itemsPerPage;
        int slot = 0;
        for (int i = startIndex; i < allWeapons.size() && slot < itemsPerPage; i++) {
            Weapon weapon = allWeapons.get(i);
            ItemStack weaponItem = weaponManager.getItemFactory().createWeapon(weapon);
            inventory.setItem(slot, weaponItem);
            slot++;
        }

        player.openInventory(inventory);
    }

    public void openArmorsMenu(Player player, int page) {
        List<Armor> allArmors = new ArrayList<>(armorManager.getAllArmors());
        int itemsPerPage = 45;
        int totalPages = (int) Math.ceil((double) allArmors.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
        page = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inventory = Bukkit.createInventory(null, 54, Component.text("Armors - Page " + (page + 1) + "/" + totalPages));
        ItemStack filler = makeFiller();
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, filler);
        }

        // Back button
        inventory.setItem(45, makeBackButton());

        // Previous page button
        if (page > 0) {
            inventory.setItem(46, makePreviousPageButton());
        }

        // Next page button
        if (page < totalPages - 1) {
            inventory.setItem(47, makeNextPageButton());
        }

        int startIndex = page * itemsPerPage;
        int slot = 0;
        for (int i = startIndex; i < allArmors.size() && slot < itemsPerPage; i++) {
            Armor armor = allArmors.get(i);
            ItemStack armorItem = armorManager.getItemFactory().createArmor(armor);
            inventory.setItem(slot, armorItem);
            slot++;
        }

        player.openInventory(inventory);
    }

    public void openCustomItemsMenu(Player player, int page) {
        List<CustomItem> allItems = new ArrayList<>(customItemManager.getAllItems());
        int itemsPerPage = 45;
        int totalPages = (int) Math.ceil((double) allItems.size() / itemsPerPage);
        if (totalPages == 0) totalPages = 1;
        page = Math.max(0, Math.min(page, totalPages - 1));

        Inventory inventory = Bukkit.createInventory(null, 54, Component.text("Custom Items - Page " + (page + 1) + "/" + totalPages));
        ItemStack filler = makeFiller();
        for (int i = 0; i < 54; i++) {
            inventory.setItem(i, filler);
        }

        // Back button
        inventory.setItem(45, makeBackButton());

        // Previous page button
        if (page > 0) {
            inventory.setItem(46, makePreviousPageButton());
        }

        // Next page button
        if (page < totalPages - 1) {
            inventory.setItem(47, makeNextPageButton());
        }

        int startIndex = page * itemsPerPage;
        int slot = 0;
        for (int i = startIndex; i < allItems.size() && slot < itemsPerPage; i++) {
            CustomItem item = allItems.get(i);
            ItemStack customItem = customItemManager.createItem(item);
            inventory.setItem(slot, customItem);
            slot++;
        }

        player.openInventory(inventory);
    }

    private ItemStack makeCategoryButton(String category, Material material, String name, String description) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(serializer.deserialize(ColorUtil.translate(name)));
        List<Component> lore = new ArrayList<>();
        lore.add(serializer.deserialize(ColorUtil.translate(description)));
        meta.lore(lore);
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeBackButton() {
        ItemStack item = new ItemStack(Material.ARROW);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(serializer.deserialize(ColorUtil.translate("&c&l← Back")));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makePreviousPageButton() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(serializer.deserialize(ColorUtil.translate("&e&l← Previous Page")));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeNextPageButton() {
        ItemStack item = new ItemStack(Material.PAPER);
        ItemMeta meta = item.getItemMeta();
        meta.displayName(serializer.deserialize(ColorUtil.translate("&e&lNext Page →")));
        item.setItemMeta(meta);
        return item;
    }

    private ItemStack makeFiller() {
        ItemStack filler = new ItemStack(Material.GRAY_STAINED_GLASS_PANE);
        ItemMeta meta = filler.getItemMeta();
        meta.displayName(Component.text(" "));
        filler.setItemMeta(meta);
        return filler;
    }
}
