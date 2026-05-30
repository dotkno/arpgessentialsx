package com.ahren.arpgessentialsx.party.gui;

import com.ahren.arpgessentialsx.party.Party;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;

import java.util.List;

/**
 * The party leader's control panel.
 *
 * Layout (3-row chest, 27 slots):
 *   Slot 11: Invite Player  (emerald)
 *   Slot 13: Party info     (book — decorative, shows party name/size)
 *   Slot 15: Kick Member    (red concrete)
 *   Slot 22: Disband Party  (barrier — bottom center, intentionally hard to misclick)
 */
public final class PartyLeaderGUI {

    public static final Component TITLE =
            LegacyComponentSerializer.legacyAmpersand().deserialize("&8&l⚔ Party — Leader Menu");

    private PartyLeaderGUI() {}

    public static void open(Player leader, Party party) {
        Inventory inv = Bukkit.createInventory(null, 27, TITLE);
        PartyMainGUI.fillBorder(inv);

        inv.setItem(11, PartyMainGUI.buildItem(
                Material.EMERALD,
                "&a&lInvite Player",
                List.of("&7Send a direct invite to", "&7an online player.", "",
                        "&fUsage: &e/arpg party invite <player>", "",
                        "&eClick to invite!")
        ));

        // Decorative info book
        inv.setItem(13, PartyMainGUI.buildItem(
                Material.WRITTEN_BOOK,
                "&f&l" + party.getName(),
                List.of(
                        "&7Members: &f" + party.size() + "&7/&f" + Party.MAX_SIZE,
                        "",
                        "&8Party ID: &7" + party.getPartyId().toString().substring(0, 8)
                )
        ));

        inv.setItem(15, PartyMainGUI.buildItem(
                Material.RED_CONCRETE,
                "&c&lKick Member",
                List.of("&7Remove a player from", "&7your party.", "", "&eClick to open kick menu!")
        ));

        // Disband — bottom center, separated to avoid misclick
        inv.setItem(22, PartyMainGUI.buildItem(
                Material.BARRIER,
                "&4&lDisband Party",
                List.of("&c&lThis cannot be undone!", "&7All members will be removed.", "",
                        "&eClick to disband.")
        ));

        leader.openInventory(inv);
    }
}