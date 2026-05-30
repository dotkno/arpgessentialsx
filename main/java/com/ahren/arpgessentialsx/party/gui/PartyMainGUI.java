package com.ahren.arpgessentialsx.party.gui;

import com.ahren.arpgessentialsx.util.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.List;

/**
 * The first screen a player sees when they run /arpg party.
 *
 * Layout (3-row chest, 27 slots):
 *   All slots: gray pane filler
 *   Slot 11: Create Party (lime green concrete)
 *   Slot 13: (spacer — decorative nether star)
 *   Slot 15: Join Party   (cyan concrete)
 */
public final class PartyMainGUI {

    public static final Component TITLE =
            LegacyComponentSerializer.legacyAmpersand().deserialize("&8&l⚔ Party Menu");

    private PartyMainGUI() {}

    public static void open(Player player) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        fillBorder(inv);

        inv.setItem(11, buildItem(
                Material.LIME_CONCRETE,
                "&a&lCreate Party",
                List.of("&7Start your own party.", "", "&eClick to create!")
        ));

        // Decorative center
        inv.setItem(13, buildItem(
                Material.NETHER_STAR,
                "&f&l✦",
                List.of("&7Lead or join a party", "&7to adventure together.")
        ));

        inv.setItem(15, buildItem(
                Material.CYAN_CONCRETE,
                "&b&lJoin Party",
                List.of("&7Browse active parties.", "", "&eClick to browse!")
        ));

        player.openInventory(inv);
    }

    static ItemStack buildItem(Material material, String name, List<String> lore) {
        ItemStack item = new ItemStack(material);
        ItemMeta meta = item.getItemMeta();
        LegacyComponentSerializer ser = LegacyComponentSerializer.legacyAmpersand();

        meta.displayName(ser.deserialize(ColorUtil.translate(name)));
        meta.lore(lore.stream()
                .map(ColorUtil::translate)
                .map(ser::deserialize)
                .toList());
        item.setItemMeta(meta);
        return item;
    }

    static void fillBorder(Inventory inv) {
        ItemStack pane = buildItem(Material.GRAY_STAINED_GLASS_PANE, " ", List.of());
        for (int i = 0; i < inv.getSize(); i++) {
            inv.setItem(i, pane);
        }
    }
}