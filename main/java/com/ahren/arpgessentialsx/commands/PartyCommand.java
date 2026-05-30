package com.ahren.arpgessentialsx.commands;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.party.Party;
import com.ahren.arpgessentialsx.party.PartyManager;
import com.ahren.arpgessentialsx.party.gui.PartyLeaderGUI;
import com.ahren.arpgessentialsx.party.gui.PartyMainGUI;
import com.ahren.arpgessentialsx.party.hud.PartyHUDManager;
import com.ahren.arpgessentialsx.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.UUID;

/**
 * Handles all /party commands directly without /arpg prefix.
 *
 * Subcommand tree:
 * ─────────────────────────────────────────────────────────────
 *  /party                        → open party main GUI
 *  /party name <name>            → rename party (leader only)
 *  /party invite <player>        → invite player (leader only)
 *  /party invite <accept|decline>→ respond to an invite
 *  /party request <accept|decline> → respond to a join request (leader only)
 *  /party leave                  → leave current party (non-leaders)
 * ─────────────────────────────────────────────────────────────
 */
public final class PartyCommand implements CommandExecutor {

    private final ARPGEssentialsX plugin;

    public PartyCommand(ARPGEssentialsX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.translate("&cOnly players can use party commands."));
            return true;
        }

        PartyManager pm = plugin.getPartyManager();
        PartyHUDManager hud = plugin.getPartyHUDManager();

        // /party  (no sub-arg) → open GUI
        if (args.length == 0) {
            Party existing = pm.getPartyOf(player.getUniqueId());
            if (existing != null && existing.isLeader(player.getUniqueId())) {
                PartyLeaderGUI.open(player, existing);
            } else if (existing != null) {
                player.sendMessage(ColorUtil.translate("&7You are in &e" + existing.getName()
                        + "&7. Only the leader can manage the party."));
                player.sendMessage(ColorUtil.translate("&7Type &e/party leave &7to leave."));
            } else {
                PartyMainGUI.open(player);
            }
            return true;
        }

        switch (args[0].toLowerCase()) {

            // ── /party name <name> ───────────────────────────────────────
            case "name" -> {
                if (args.length < 2) {
                    player.sendMessage(ColorUtil.translate("&cUsage: &e/party name <name>"));
                    return true;
                }
                Party party = pm.getPartyOf(player.getUniqueId());
                if (party == null) {
                    player.sendMessage(ColorUtil.translate("&cYou are not in a party."));
                    return true;
                }
                if (!party.isLeader(player.getUniqueId())) {
                    player.sendMessage(ColorUtil.translate("&cOnly the party leader can rename the party."));
                    return true;
                }
                // Join all remaining args as the name (allows spaces)
                String newName = String.join(" ", java.util.Arrays.copyOfRange(args, 1, args.length));
                if (newName.length() > 32) {
                    player.sendMessage(ColorUtil.translate("&cParty name cannot exceed 32 characters."));
                    return true;
                }
                party.setName(newName);
                player.sendMessage(ColorUtil.translate("&aParty renamed to &f" + newName + "&a!"));
                // Notify all members
                for (UUID uuid : party.getMembers()) {
                    Player member = Bukkit.getPlayer(uuid);
                    if (member != null && !member.equals(player)) {
                        member.sendMessage(ColorUtil.translate(
                                "&7The party has been renamed to &f" + newName + "&7."));
                    }
                }
            }

            // ── /party invite <player|accept|decline> ────────────────────
            case "invite" -> {
                if (args.length < 2) {
                    player.sendMessage(ColorUtil.translate("&cUsage: &e/party invite <player>"));
                    player.sendMessage(ColorUtil.translate("&cOr: &e/party invite <accept|decline>"));
                    return true;
                }

                String sub = args[1].toLowerCase();

                // Responding to an invite
                if (sub.equals("accept") || sub.equals("decline")) {
                    UUID leaderUUID = pm.getInviteLeader(player.getUniqueId());
                    if (leaderUUID == null) {
                        player.sendMessage(ColorUtil.translate("&cYou have no pending party invite."));
                        return true;
                    }
                    pm.removeInvite(player.getUniqueId());

                    if (sub.equals("decline")) {
                        player.sendMessage(ColorUtil.translate("&7You declined the party invite."));
                        Player leader = Bukkit.getPlayer(leaderUUID);
                        if (leader != null) {
                            leader.sendMessage(ColorUtil.translate(
                                    "&e" + player.getName() + " &7declined your party invite."));
                        }
                        return true;
                    }

                    // Accept
                    Party party = pm.getPartyOf(leaderUUID);
                    if (party == null) {
                        player.sendMessage(ColorUtil.translate("&cThat party no longer exists."));
                        return true;
                    }
                    if (party.isFull()) {
                        player.sendMessage(ColorUtil.translate("&cThat party is now full."));
                        return true;
                    }
                    if (!pm.joinParty(player.getUniqueId(), party.getPartyId())) {
                        player.sendMessage(ColorUtil.translate("&cCould not join the party."));
                        return true;
                    }
                    hud.show(player);
                    ColorUtil.sendActionBar(player,
                            "&aYou have joined &f" + party.getName() + "&a!");

                    Player leader = Bukkit.getPlayer(leaderUUID);
                    if (leader != null) {
                        ColorUtil.sendActionBar(leader,
                                "&f" + player.getName() + " &ahas joined your party!");
                    }
                    // Notify all existing members
                    for (UUID uuid : party.getMembers()) {
                        Player member = Bukkit.getPlayer(uuid);
                        if (member != null && !member.equals(player)
                                && !uuid.equals(leaderUUID)) {
                            member.sendMessage(ColorUtil.translate(
                                    "&e" + player.getName() + " &7has joined the party."));
                        }
                    }
                    return true;
                }

                // Sending an invite (leader only)
                Party party = pm.getPartyOf(player.getUniqueId());
                if (party == null) {
                    player.sendMessage(ColorUtil.translate("&cYou are not in a party."));
                    return true;
                }
                if (!party.isLeader(player.getUniqueId())) {
                    player.sendMessage(ColorUtil.translate("&cOnly the party leader can send invites."));
                    return true;
                }
                if (party.isFull()) {
                    player.sendMessage(ColorUtil.translate("&cYour party is full (&f"
                            + Party.MAX_SIZE + "&c/&f" + Party.MAX_SIZE + "&c)."));
                    return true;
                }

                Player target = Bukkit.getPlayer(args[1]);
                if (target == null) {
                    player.sendMessage(ColorUtil.translate("&cPlayer '" + args[1] + "' is not online."));
                    return true;
                }
                if (target.equals(player)) {
                    player.sendMessage(ColorUtil.translate("&cYou cannot invite yourself."));
                    return true;
                }
                if (pm.getPartyOf(target.getUniqueId()) != null) {
                    player.sendMessage(ColorUtil.translate("&c" + target.getName() + " is already in a party."));
                    return true;
                }

                pm.addInvite(target.getUniqueId(), player.getUniqueId());
                player.sendMessage(ColorUtil.translate("&7Invite sent to &e" + target.getName() + "&7."));
                target.sendMessage(ColorUtil.translate(""));
                target.sendMessage(ColorUtil.translate("&e" + player.getName()
                        + " &7invited you to their party &f(" + party.getName() + ")&7!"));
                target.sendMessage(ColorUtil.translate(
                        "&7Type &a/party invite accept &7or &c/party invite decline"));
                target.sendMessage(ColorUtil.translate(""));
            }

            // ── /party request <accept|decline> ──────────────────────────
            case "request" -> {
                if (args.length < 2) {
                    player.sendMessage(ColorUtil.translate(
                            "&cUsage: &e/party request <accept|decline>"));
                    return true;
                }

                Party party = pm.getPartyOf(player.getUniqueId());
                if (party == null || !party.isLeader(player.getUniqueId())) {
                    player.sendMessage(ColorUtil.translate(
                            "&cOnly a party leader can respond to join requests."));
                    return true;
                }

                UUID requesterUUID = pm.getPendingRequesterFor(player.getUniqueId());
                if (requesterUUID == null) {
                    player.sendMessage(ColorUtil.translate("&cYou have no pending join requests."));
                    return true;
                }

                pm.removeRequest(requesterUUID);
                Player requester = Bukkit.getPlayer(requesterUUID);
                String requesterName = requester != null ? requester.getName()
                        : Bukkit.getOfflinePlayer(requesterUUID).getName();
                if (requesterName == null) requesterName = "that player";

                if (args[1].equalsIgnoreCase("decline")) {
                    player.sendMessage(ColorUtil.translate(
                            "&7Declined &e" + requesterName + "&7's join request."));
                    if (requester != null) {
                        ColorUtil.sendActionBar(requester, "&cYour join request was declined.");
                    }
                    return true;
                }

                if (!args[1].equalsIgnoreCase("accept")) {
                    player.sendMessage(ColorUtil.translate(
                            "&cUsage: &e/party request <accept|decline>"));
                    return true;
                }

                // Accept
                if (party.isFull()) {
                    player.sendMessage(ColorUtil.translate("&cYour party is full!"));
                    if (requester != null) {
                        ColorUtil.sendActionBar(requester, "&cThe party is full.");
                    }
                    return true;
                }
                if (requester == null || !requester.isOnline()) {
                    player.sendMessage(ColorUtil.translate("&c" + requesterName + " is no longer online."));
                    return true;
                }

                pm.joinParty(requesterUUID, party.getPartyId());
                hud.show(requester);

                ColorUtil.sendActionBar(requester,
                        "&aYou have joined &f" + party.getName() + "&a!");
                ColorUtil.sendActionBar(player,
                        "&f" + requesterName + " &ahas joined your party!");

                for (UUID uuid : party.getMembers()) {
                    Player member = Bukkit.getPlayer(uuid);
                    if (member != null && !uuid.equals(requesterUUID)
                            && !uuid.equals(player.getUniqueId())) {
                        member.sendMessage(ColorUtil.translate(
                                "&e" + requesterName + " &7has joined the party."));
                    }
                }
            }

            // ── /party leave ─────────────────────────────────────────────
            case "leave" -> {
                Party party = pm.getPartyOf(player.getUniqueId());
                if (party == null) {
                    player.sendMessage(ColorUtil.translate("&cYou are not in a party."));
                    return true;
                }
                if (party.isLeader(player.getUniqueId())) {
                    player.sendMessage(ColorUtil.translate(
                            "&cYou are the leader. Use &e/party &cthen Disband to dissolve the party."));
                    return true;
                }

                String partyName = party.getName();
                pm.leaveParty(player.getUniqueId());
                hud.hide(player);

                player.sendMessage(ColorUtil.translate("&7You left &e" + partyName + "&7."));

                // Notify remaining members
                for (UUID uuid : party.getMembers()) {
                    Player member = Bukkit.getPlayer(uuid);
                    if (member != null) {
                        member.sendMessage(ColorUtil.translate(
                                "&e" + player.getName() + " &7has left the party."));
                    }
                }
            }

            default -> sendPartyHelp(player);
        }

        return true;
    }

    private void sendPartyHelp(Player player) {
        player.sendMessage(ColorUtil.translate("&8&l=== &fParty Commands &8&l==="));
        player.sendMessage(ColorUtil.translate("&7/party &8- &7Open the party menu."));
        player.sendMessage(ColorUtil.translate("&7/party name <name> &8- &7Rename your party."));
        player.sendMessage(ColorUtil.translate("&7/party invite <player> &8- &7Invite a player."));
        player.sendMessage(ColorUtil.translate("&7/party invite <accept|decline> &8- &7Respond to invite."));
        player.sendMessage(ColorUtil.translate("&7/party request <accept|decline> &8- &7Respond to join request."));
        player.sendMessage(ColorUtil.translate("&7/party leave &8- &7Leave the party."));
    }
}
