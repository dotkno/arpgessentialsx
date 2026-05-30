package com.ahren.arpgessentialsx.party.gui;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.party.Party;
import com.ahren.arpgessentialsx.util.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Collection;
import java.util.List;

/**
 * Displays all active parties so a player can send a join request.
 *
 * Each party is represented by the leader's skull icon.
 * Clicking sends a join request to that leader via PartyGUIListener.
 *
 * If there are no active parties, a "No parties available" item is shown.
 */
public final class PartyJoinGUI {

    public static final Component TITLE =
            LegacyComponentSerializer.legacyAmpersand().deserialize("&8&l⚔ Browse Parties");

    /** PDC key stored on each skull so the listener knows which party was clicked */
    public static final String TAG_KEY = "party_join_target";

    private PartyJoinGUI() {}

    public static void open(Player player, ARPGEssentialsX plugin) {
        Collection<Party> allParties = plugin.getPartyManager().getAllParties();

        // Exclude the player's own party if they somehow have one
        List<Party> joinable = allParties.stream()
                .filter(p -> !p.isMember(player.getUniqueId()))
                .toList();

        // Calculate rows
        int count  = Math.max(1, joinable.size());
        int rows   = Math.min(6, Math.max(1, (int) Math.ceil(count / 9.0) + 1));
        int size   = rows * 9;

        Inventory inv = Bukkit.createInventory(null, size, TITLE);
        PartyMainGUI.fillBorder(inv);

        if (joinable.isEmpty()) {
            inv.setItem(size / 2, PartyMainGUI.buildItem(
                    Material.BARRIER,
                    "&cNo Active Parties",
                    List.of("&7There are no parties to join.", "&7Ask a friend to create one!")
            ));
            player.openInventory(inv);
            return;
        }

        int slot = 0;
        for (Party party : joinable) {
            if (slot >= size) break;
            inv.setItem(slot, buildPartySkull(party));
            slot++;
        }

        player.openInventory(inv);
    }

    private static ItemStack buildPartySkull(Party party) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        OfflinePlayer leader = Bukkit.getOfflinePlayer(party.getLeaderUUID());
        meta.setOwningPlayer(leader);

        String leaderName = leader.getName() != null ? leader.getName() : "Unknown";
        LegacyComponentSerializer ser = LegacyComponentSerializer.legacyAmpersand();

        meta.displayName(ser.deserialize(ColorUtil.translate("&6&l" + party.getName())));
        meta.lore(List.of(
                ser.deserialize(ColorUtil.translate("&7Leader: &f" + leaderName)),
                ser.deserialize(ColorUtil.translate("&7Members: &f" + party.size() + "&7/&f" + Party.MAX_SIZE)),
                Component.empty(),
                ser.deserialize(ColorUtil.translate(
                        party.isFull() ? "&c&lFULL" : "&eClick to request to join!"
                ))
        ));

        // Store leader UUID so listener can look up the party
        meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey("arpgessentialsx", TAG_KEY),
                org.bukkit.persistence.PersistentDataType.STRING,
                party.getLeaderUUID().toString()
        );

        skull.setItemMeta(meta);
        return skull;
    }
}