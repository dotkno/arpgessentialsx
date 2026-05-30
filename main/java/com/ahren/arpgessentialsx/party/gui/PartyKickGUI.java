package com.ahren.arpgessentialsx.party.gui;

import com.ahren.arpgessentialsx.party.Party;
import com.ahren.arpgessentialsx.util.ColorUtil;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Bukkit;
import org.bukkit.OfflinePlayer;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;
import org.bukkit.Material;

import java.util.List;
import java.util.UUID;

/**
 * Shows the party's current non-leader members as player skull icons.
 * The leader clicks a skull to kick that member.
 *
 * Layout: dynamic rows based on member count (minimum 1 row = 9 slots).
 * Members are laid out left-to-right starting at slot 0.
 *
 * The skull's PDC stores the target's UUID so PartyGUIListener can read it.
 * We store it in the item display name as a hidden tag since PDC on skulls
 * can be unreliable across some server versions. Instead we use a dedicated
 * NamespacedKey set in the meta — see PartyGUIListener for the read side.
 */
public final class PartyKickGUI {

    public static final Component TITLE =
            LegacyComponentSerializer.legacyAmpersand().deserialize("&8&l⚔ Kick Member");

    /** NamespacedKey string used to tag kick-target skulls. Read by PartyGUIListener. */
    public static final String TAG_KEY = "party_kick_target";

    private PartyKickGUI() {}

    public static void open(Player leader, Party party) {
        // Members excluding the leader
        List<UUID> kickable = party.getMembers().stream()
                .filter(uuid -> !uuid.equals(party.getLeaderUUID()))
                .toList();

        // Calculate inventory size: enough rows for all members + 1 row padding, min 1 row
        int rows = Math.max(1, (int) Math.ceil((kickable.size() + 1) / 9.0) + 1);
        rows = Math.min(rows, 6); // Bukkit max is 6 rows
        int size = rows * 9;

        Inventory inv = Bukkit.createInventory(null, size, TITLE);
        PartyMainGUI.fillBorder(inv);

        if (kickable.isEmpty()) {
            inv.setItem(size / 2, PartyMainGUI.buildItem(
                    Material.BARRIER,
                    "&cNo members to kick.",
                    List.of("&7You are the only one here.")
            ));
            leader.openInventory(inv);
            return;
        }

        // Place member skulls starting from slot 0
        int slot = 0;
        for (UUID memberUUID : kickable) {
            if (slot >= size) break;
            inv.setItem(slot, buildMemberSkull(memberUUID));
            slot++;
        }

        leader.openInventory(inv);
    }

    private static ItemStack buildMemberSkull(UUID memberUUID) {
        ItemStack skull = new ItemStack(Material.PLAYER_HEAD);
        SkullMeta meta = (SkullMeta) skull.getItemMeta();

        OfflinePlayer offlinePlayer = Bukkit.getOfflinePlayer(memberUUID);
        meta.setOwningPlayer(offlinePlayer);

        String name = offlinePlayer.getName() != null ? offlinePlayer.getName() : memberUUID.toString().substring(0, 8);
        LegacyComponentSerializer ser = LegacyComponentSerializer.legacyAmpersand();

        meta.displayName(ser.deserialize(ColorUtil.translate("&c" + name)));
        meta.lore(List.of(
                ser.deserialize(ColorUtil.translate("&7Click to kick from party.")),
                Component.empty(),
                ser.deserialize(ColorUtil.translate("&8UUID: &7" + memberUUID.toString().substring(0, 8)))
        ));

        // Store the UUID string in persistent data so the listener can read it
        meta.getPersistentDataContainer().set(
                new org.bukkit.NamespacedKey("arpgessentialsx", TAG_KEY),
                org.bukkit.persistence.PersistentDataType.STRING,
                memberUUID.toString()
        );

        skull.setItemMeta(meta);
        return skull;
    }
}