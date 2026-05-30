package com.ahren.arpgessentialsx.party.listeners;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.party.Party;
import com.ahren.arpgessentialsx.party.PartyManager;
import com.ahren.arpgessentialsx.party.gui.PartyJoinGUI;
import com.ahren.arpgessentialsx.party.gui.PartyKickGUI;
import com.ahren.arpgessentialsx.party.gui.PartyLeaderGUI;
import com.ahren.arpgessentialsx.party.gui.PartyMainGUI;
import com.ahren.arpgessentialsx.party.hud.PartyHUDManager;
import com.ahren.arpgessentialsx.util.ColorUtil;
import net.kyori.adventure.text.Component;
import org.bukkit.Bukkit;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

/**
 * Handles all inventory click events across every party GUI.
 *
 * IMPORTANT: event.setCancelled(true) is called ONLY after confirming
 * the click is inside a known party GUI. Cancelling unconditionally
 * freezes the player's own inventory.
 */
public final class PartyGUIListener implements Listener {

    private final ARPGEssentialsX plugin;
    private final PartyManager partyManager;
    private final PartyHUDManager hudManager;

    private final NamespacedKey kickKey;
    private final NamespacedKey joinKey;

    public PartyGUIListener(ARPGEssentialsX plugin, PartyHUDManager hudManager) {
        this.plugin       = plugin;
        this.partyManager = plugin.getPartyManager();
        this.hudManager   = hudManager;
        this.kickKey      = new NamespacedKey("arpgessentialsx", PartyKickGUI.TAG_KEY);
        this.joinKey      = new NamespacedKey("arpgessentialsx", PartyJoinGUI.TAG_KEY);
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        if (!(event.getWhoClicked() instanceof Player player)) return;

        Component title = event.getView().title();

        // ── PartyMainGUI ──────────────────────────────────────────────────────
        if (title.equals(PartyMainGUI.TITLE)) {
            event.setCancelled(true);
            handleMainGUI(player, event.getSlot());
            return;
        }

        // ── PartyLeaderGUI ────────────────────────────────────────────────────
        if (title.equals(PartyLeaderGUI.TITLE)) {
            event.setCancelled(true);
            handleLeaderGUI(player, event.getSlot());
            return;
        }

        // ── PartyKickGUI ──────────────────────────────────────────────────────
        if (title.equals(PartyKickGUI.TITLE)) {
            event.setCancelled(true);
            handleKickGUI(player, event.getCurrentItem());
            return;
        }

        // ── PartyJoinGUI ──────────────────────────────────────────────────────
        if (title.equals(PartyJoinGUI.TITLE)) {
            event.setCancelled(true);
            handleJoinGUI(player, event.getCurrentItem());
            return;
        }

        // No party GUI matched — do NOT cancel. Player's own inventory works normally.
    }

    // ── Handlers ──────────────────────────────────────────────────────────────

    private void handleMainGUI(Player player, int slot) {
        if (slot == 11) {
            // Create Party
            if (partyManager.getPartyOf(player.getUniqueId()) != null) {
                player.closeInventory();
                player.sendMessage(ColorUtil.translate("&cYou are already in a party!"));
                return;
            }
            Party party = partyManager.createParty(player.getUniqueId(), player.getName());
            player.closeInventory();
            player.sendMessage(ColorUtil.translate(""));
            player.sendMessage(ColorUtil.translate("&a&l✦ &aParty created!"));
            player.sendMessage(ColorUtil.translate("&7Set a name with &e/arpg party name <name>"));
            player.sendMessage(ColorUtil.translate("&7Invite players with &e/arpg party invite <player>"));
            player.sendMessage(ColorUtil.translate(""));
            hudManager.show(player);
            Bukkit.getScheduler().runTaskLater(plugin, () -> PartyLeaderGUI.open(player, party), 2L);

        } else if (slot == 15) {
            // Join Party
            player.closeInventory();
            Bukkit.getScheduler().runTaskLater(plugin, () -> PartyJoinGUI.open(player, plugin), 2L);
        }
    }

    private void handleLeaderGUI(Player player, int slot) {
        Party party = partyManager.getPartyOf(player.getUniqueId());
        if (party == null || !party.isLeader(player.getUniqueId())) {
            player.closeInventory();
            return;
        }

        if (slot == 11) {
            player.closeInventory();
            player.sendMessage(ColorUtil.translate("&7Type &e/arpg party invite <player> &7to invite someone."));

        } else if (slot == 15) {
            player.closeInventory();
            Bukkit.getScheduler().runTaskLater(plugin, () -> PartyKickGUI.open(player, party), 2L);

        } else if (slot == 22) {
            player.closeInventory();
            disbandParty(player, party);
        }
    }

    private void handleKickGUI(Player player, ItemStack clicked) {
        if (clicked == null || !clicked.hasItemMeta()) return;
        ItemMeta meta = clicked.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        if (!pdc.has(kickKey, PersistentDataType.STRING)) return;

        String targetUUIDStr = pdc.get(kickKey, PersistentDataType.STRING);
        UUID targetUUID;
        try {
            targetUUID = UUID.fromString(targetUUIDStr);
        } catch (IllegalArgumentException e) {
            return;
        }

        Party party = partyManager.getPartyOf(player.getUniqueId());
        if (party == null || !party.isLeader(player.getUniqueId())) {
            player.closeInventory();
            return;
        }

        if (!party.isMember(targetUUID)) {
            player.sendMessage(ColorUtil.translate("&cThat player is no longer in your party."));
            player.closeInventory();
            return;
        }

        partyManager.leaveParty(targetUUID);
        player.closeInventory();

        Player target = Bukkit.getPlayer(targetUUID);
        String targetName = target != null ? target.getName()
                : Bukkit.getOfflinePlayer(targetUUID).getName();
        if (targetName == null) targetName = "that player";

        player.sendMessage(ColorUtil.translate("&e" + targetName + " &7has been kicked from the party."));

        if (target != null) {
            hudManager.hide(target);
            ColorUtil.sendActionBar(target, "&cYou have been kicked from the party.");
            target.sendMessage(ColorUtil.translate("&cYou were kicked from &e" + party.getName() + "&c."));
        }

        for (UUID memberUUID : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null && !member.equals(player)) {
                member.sendMessage(ColorUtil.translate("&e" + targetName + " &7was kicked from the party."));
            }
        }
    }

    private void handleJoinGUI(Player player, ItemStack clicked) {
        if (clicked == null || !clicked.hasItemMeta()) return;
        ItemMeta meta = clicked.getItemMeta();
        PersistentDataContainer pdc = meta.getPersistentDataContainer();

        if (!pdc.has(joinKey, PersistentDataType.STRING)) return;

        String leaderUUIDStr = pdc.get(joinKey, PersistentDataType.STRING);
        UUID leaderUUID;
        try {
            leaderUUID = UUID.fromString(leaderUUIDStr);
        } catch (IllegalArgumentException e) {
            return;
        }

        player.closeInventory();

        Party party = partyManager.getPartyOf(leaderUUID);
        if (party == null) {
            player.sendMessage(ColorUtil.translate("&cThat party no longer exists."));
            return;
        }
        if (party.isFull()) {
            player.sendMessage(ColorUtil.translate("&cThat party is full!"));
            return;
        }
        if (party.isMember(player.getUniqueId())) {
            player.sendMessage(ColorUtil.translate("&cYou are already in that party."));
            return;
        }

        partyManager.addRequest(player.getUniqueId(), leaderUUID);
        ColorUtil.sendActionBar(player, "&eJoin request sent to &f" + party.getName() + "&e!");

        Player leader = Bukkit.getPlayer(leaderUUID);
        if (leader != null) {
            leader.sendMessage(ColorUtil.translate(""));
            leader.sendMessage(ColorUtil.translate("&e" + player.getName() + " &7wants to join your party!"));
            leader.sendMessage(ColorUtil.translate("&7Type &e/arpg party request accept &7or &c/arpg party request decline"));
            leader.sendMessage(ColorUtil.translate(""));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void disbandParty(Player leader, Party party) {
        for (UUID memberUUID : party.getMembers()) {
            Player member = Bukkit.getPlayer(memberUUID);
            if (member != null) {
                hudManager.hide(member);
                if (!member.equals(leader)) {
                    ColorUtil.sendActionBar(member, "&cThe party has been disbanded.");
                    member.sendMessage(ColorUtil.translate("&c&l" + party.getName() + " &chas been disbanded."));
                }
            }
        }
        partyManager.disbandParty(party.getPartyId());
        leader.sendMessage(ColorUtil.translate("&7Your party has been disbanded."));
    }
}