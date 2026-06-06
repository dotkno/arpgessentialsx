package com.ahren.arpgessentialsx.commands;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.gui.AdminMenuGUI;
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
import org.jetbrains.annotations.NotNull;

import java.util.UUID;

/**
 * Handles all /arpg subcommands.
 *
 * Subcommand tree:
 * ─────────────────────────────────────────────────────────────
 *  /arpg                              → help menu
 *  /arpg class pick                   → open class GUI
 *  /arpg class reset [player]         → reset class (OP only)
 * ─────────────────────────────────────────────────────────────
 *  /arpg party                        → open party main GUI
 *  /arpg party name <name>            → rename party (leader only)
 *  /arpg party invite <player>        → invite player (leader only)
 *  /arpg party invite <accept|decline>→ respond to an invite
 *  /arpg party request <accept|decline> → respond to a join request (leader only)
 *  /arpg party leave                  → leave current party (non-leaders)
 * ─────────────────────────────────────────────────────────────
 *  /arpg admin                        → open admin item menu (OP only)
 * ─────────────────────────────────────────────────────────────
 *
 * Note: "invite" doubles as both the send-invite and accept/decline command.
 * The handler checks whether the sender has a pending invite to decide which path to take.
 */
public final class ArpgCommand implements CommandExecutor {

    private final ARPGEssentialsX plugin;

    public ArpgCommand(ARPGEssentialsX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command,
                             @NotNull String label, @NotNull String[] args) {

        if (args.length == 0) {
            sendHelp(sender);
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "class"             -> handleClass(sender, args);
            case "party"             -> handleParty(sender, args);
            case "stats"             -> handleStats(sender);
            case "admin"             -> handleAdmin(sender, args);
            case "generatepack"      -> handleGeneratePack(sender);
            case "generatebedrock"   -> handleGenerateBedrockPack(sender);
            case "generateall"       -> handleGenerateAllPacks(sender);
            default                  -> sendHelp(sender);
        }
        return true;
    }

    // ══════════════════════════════════════════════════════════════════════════
    // /arpg class ...
    // ══════════════════════════════════════════════════════════════════════════

    private void handleClass(CommandSender sender, String[] args) {
        if (args.length == 1) { sendHelp(sender); return; }

        switch (args[1].toLowerCase()) {

            case "pick" -> {
                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ColorUtil.translate("&cOnly players can use this command."));
                    return;
                }
                var data = plugin.getPlayerDataManager().getOrCreatePlayerData(player.getUniqueId());
                if (data.hasClass()) {
                    player.sendMessage(ColorUtil.translate(
                            "&cYou already have a class! Ask an admin to reset it if you want to change."));
                    return;
                }
                plugin.applyClassToPlayer(player);
            }

            case "reset" -> {
                if (!sender.isOp() && !sender.hasPermission("arpg.admin")) {
                    sender.sendMessage(ColorUtil.translate("&cYou don't have permission to use this command."));
                    return;
                }

                if (args.length == 3) {
                    // Reset a target player
                    Player target = Bukkit.getPlayer(args[2]);
                    if (target == null) {
                        sender.sendMessage(ColorUtil.translate("&cPlayer '" + args[2] + "' is not online."));
                        return;
                    }
                    plugin.getPlayerDataManager().setPlayerClass(target.getUniqueId(), null);
                    plugin.applyClassToPlayer(target);
                    sender.sendMessage(ColorUtil.translate("&eReset " + target.getName() + "'s class."));
                    target.sendMessage(ColorUtil.translate("&eYour class has been reset by an admin. Choose wisely!"));
                    return;
                }

                if (!(sender instanceof Player player)) {
                    sender.sendMessage(ColorUtil.translate("&cConsole cannot reset its own class."));
                    return;
                }
                plugin.getPlayerDataManager().setPlayerClass(player.getUniqueId(), null);
                plugin.applyClassToPlayer(player);
                player.sendMessage(ColorUtil.translate("&eYour class has been reset. Choose wisely!"));
            }

            default -> sendHelp(sender);
        }
    }

    // ══════════════════════════════════════════════════════════════════════════
    // /arpg party ...
    // ══════════════════════════════════════════════════════════════════════════

    private void handleParty(CommandSender sender, String[] args) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.translate("&cOnly players can use party commands."));
            return;
        }

        PartyManager pm = plugin.getPartyManager();
        PartyHUDManager hud = plugin.getPartyHUDManager();

        // /arpg party  (no sub-arg) → open GUI
        if (args.length == 1) {
            Party existing = pm.getPartyOf(player.getUniqueId());
            if (existing != null && existing.isLeader(player.getUniqueId())) {
                PartyLeaderGUI.open(player, existing);
            } else if (existing != null) {
                player.sendMessage(ColorUtil.translate("&7You are in &e" + existing.getName()
                        + "&7. Only the leader can manage the party."));
                player.sendMessage(ColorUtil.translate("&7Type &e/arpg party leave &7to leave."));
            } else {
                PartyMainGUI.open(player);
            }
            return;
        }

        switch (args[1].toLowerCase()) {

            // ── /arpg party name <name> ───────────────────────────────────────
            case "name" -> {
                if (args.length < 3) {
                    player.sendMessage(ColorUtil.translate("&cUsage: &e/arpg party name <name>"));
                    return;
                }
                Party party = pm.getPartyOf(player.getUniqueId());
                if (party == null) {
                    player.sendMessage(ColorUtil.translate("&cYou are not in a party."));
                    return;
                }
                if (!party.isLeader(player.getUniqueId())) {
                    player.sendMessage(ColorUtil.translate("&cOnly the party leader can rename the party."));
                    return;
                }
                // Join all remaining args as the name (allows spaces)
                String newName = String.join(" ", java.util.Arrays.copyOfRange(args, 2, args.length));
                if (newName.length() > 32) {
                    player.sendMessage(ColorUtil.translate("&cParty name cannot exceed 32 characters."));
                    return;
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

            // ── /arpg party invite <player|accept|decline> ────────────────────
            case "invite" -> {
                if (args.length < 3) {
                    player.sendMessage(ColorUtil.translate("&cUsage: &e/arpg party invite <player>"));
                    player.sendMessage(ColorUtil.translate("&cOr: &e/arpg party invite <accept|decline>"));
                    return;
                }

                String sub = args[2].toLowerCase();

                // Responding to an invite
                if (sub.equals("accept") || sub.equals("decline")) {
                    UUID leaderUUID = pm.getInviteLeader(player.getUniqueId());
                    if (leaderUUID == null) {
                        player.sendMessage(ColorUtil.translate("&cYou have no pending party invite."));
                        return;
                    }
                    pm.removeInvite(player.getUniqueId());

                    if (sub.equals("decline")) {
                        player.sendMessage(ColorUtil.translate("&7You declined the party invite."));
                        Player leader = Bukkit.getPlayer(leaderUUID);
                        if (leader != null) {
                            leader.sendMessage(ColorUtil.translate(
                                    "&e" + player.getName() + " &7declined your party invite."));
                        }
                        return;
                    }

                    // Accept
                    Party party = pm.getPartyOf(leaderUUID);
                    if (party == null) {
                        player.sendMessage(ColorUtil.translate("&cThat party no longer exists."));
                        return;
                    }
                    if (party.isFull()) {
                        player.sendMessage(ColorUtil.translate("&cThat party is now full."));
                        return;
                    }
                    if (!pm.joinParty(player.getUniqueId(), party.getPartyId())) {
                        player.sendMessage(ColorUtil.translate("&cCould not join the party."));
                        return;
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
                    return;
                }

                // Sending an invite (leader only)
                Party party = pm.getPartyOf(player.getUniqueId());
                if (party == null) {
                    player.sendMessage(ColorUtil.translate("&cYou are not in a party."));
                    return;
                }
                if (!party.isLeader(player.getUniqueId())) {
                    player.sendMessage(ColorUtil.translate("&cOnly the party leader can send invites."));
                    return;
                }
                if (party.isFull()) {
                    player.sendMessage(ColorUtil.translate("&cYour party is full (&f"
                            + Party.MAX_SIZE + "&c/&f" + Party.MAX_SIZE + "&c)."));
                    return;
                }

                Player target = Bukkit.getPlayer(args[2]);
                if (target == null) {
                    player.sendMessage(ColorUtil.translate("&cPlayer '" + args[2] + "' is not online."));
                    return;
                }
                if (target.equals(player)) {
                    player.sendMessage(ColorUtil.translate("&cYou cannot invite yourself."));
                    return;
                }
                if (pm.getPartyOf(target.getUniqueId()) != null) {
                    player.sendMessage(ColorUtil.translate("&c" + target.getName() + " is already in a party."));
                    return;
                }

                pm.addInvite(target.getUniqueId(), player.getUniqueId());
                player.sendMessage(ColorUtil.translate("&7Invite sent to &e" + target.getName() + "&7."));
                target.sendMessage(ColorUtil.translate(""));
                target.sendMessage(ColorUtil.translate("&e" + player.getName()
                        + " &7invited you to their party &f(" + party.getName() + ")&7!"));
                target.sendMessage(ColorUtil.translate(
                        "&7Type &a/arpg party invite accept &7or &c/arpg party invite decline"));
                target.sendMessage(ColorUtil.translate(""));
            }

            // ── /arpg party request <accept|decline> ──────────────────────────
            case "request" -> {
                if (args.length < 3) {
                    player.sendMessage(ColorUtil.translate(
                            "&cUsage: &e/arpg party request <accept|decline>"));
                    return;
                }

                Party party = pm.getPartyOf(player.getUniqueId());
                if (party == null || !party.isLeader(player.getUniqueId())) {
                    player.sendMessage(ColorUtil.translate(
                            "&cOnly a party leader can respond to join requests."));
                    return;
                }

                UUID requesterUUID = pm.getPendingRequesterFor(player.getUniqueId());
                if (requesterUUID == null) {
                    player.sendMessage(ColorUtil.translate("&cYou have no pending join requests."));
                    return;
                }

                pm.removeRequest(requesterUUID);
                Player requester = Bukkit.getPlayer(requesterUUID);
                String requesterName = requester != null ? requester.getName()
                        : Bukkit.getOfflinePlayer(requesterUUID).getName();
                if (requesterName == null) requesterName = "that player";

                if (args[2].equalsIgnoreCase("decline")) {
                    player.sendMessage(ColorUtil.translate(
                            "&7Declined &e" + requesterName + "&7's join request."));
                    if (requester != null) {
                        ColorUtil.sendActionBar(requester, "&cYour join request was declined.");
                    }
                    return;
                }

                if (!args[2].equalsIgnoreCase("accept")) {
                    player.sendMessage(ColorUtil.translate(
                            "&cUsage: &e/arpg party request <accept|decline>"));
                    return;
                }

                // Accept
                if (party.isFull()) {
                    player.sendMessage(ColorUtil.translate("&cYour party is full!"));
                    if (requester != null) {
                        ColorUtil.sendActionBar(requester, "&cThe party is full.");
                    }
                    return;
                }
                if (requester == null || !requester.isOnline()) {
                    player.sendMessage(ColorUtil.translate("&c" + requesterName + " is no longer online."));
                    return;
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

            // ── /arpg party leave ─────────────────────────────────────────────
            case "leave" -> {
                Party party = pm.getPartyOf(player.getUniqueId());
                if (party == null) {
                    player.sendMessage(ColorUtil.translate("&cYou are not in a party."));
                    return;
                }
                if (party.isLeader(player.getUniqueId())) {
                    player.sendMessage(ColorUtil.translate(
                            "&cYou are the leader. Use &e/arpg party &cthen Disband to dissolve the party."));
                    return;
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
    }

    // ══════════════════════════════════════════════════════════════════════════
    // /arpg stats ...
    // ══════════════════════════════════════════════════════════════════════════

    private void handleStats(CommandSender sender) {
        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.translate("&cOnly players can use this command."));
            return;
        }
        plugin.getStatsHUDManager().toggle(player);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // /arpg admin ...
    // ══════════════════════════════════════════════════════════════════════════

    private void handleAdmin(CommandSender sender, String[] args) {
        if (!sender.isOp() && !sender.hasPermission("arpg.admin")) {
            sender.sendMessage(ColorUtil.translate("&cYou don't have permission to use this command."));
            return;
        }

        if (!(sender instanceof Player player)) {
            sender.sendMessage(ColorUtil.translate("&cOnly players can use this command."));
            return;
        }

        plugin.getAdminMenuGUI().open(player);
    }

    // ══════════════════════════════════════════════════════════════════════════
    // /arpg generatepack
    // ══════════════════════════════════════════════════════════════════════════

    private void handleGeneratePack(CommandSender sender) {
        if (!sender.isOp() && !sender.hasPermission("arpg.admin")) {
            sender.sendMessage(ColorUtil.translate("&cYou don't have permission to use this command."));
            return;
        }

        sender.sendMessage(ColorUtil.translate("&eGenerating Java resource pack..."));
        plugin.getResourcePackManager().generateResourcePack();
        sender.sendMessage(ColorUtil.translate("&aJava resource pack generated successfully!"));
        sender.sendMessage(ColorUtil.translate("&7Location: " + plugin.getResourcePackManager().getResourcePackFile().getAbsolutePath()));
        sender.sendMessage(ColorUtil.translate("&7Place your texture files in: " + plugin.getResourcePackManager().getTexturesFolder().getAbsolutePath()));
    }

    private void handleGenerateBedrockPack(CommandSender sender) {
        if (!sender.isOp() && !sender.hasPermission("arpg.admin")) {
            sender.sendMessage(ColorUtil.translate("&cYou don't have permission to use this command."));
            return;
        }

        sender.sendMessage(ColorUtil.translate("&eGenerating Bedrock resource pack..."));
        plugin.getResourcePackManager().generateBedrockResourcePack();
        sender.sendMessage(ColorUtil.translate("&aBedrock resource pack generated successfully!"));
        sender.sendMessage(ColorUtil.translate("&7Location: " + plugin.getResourcePackManager().getBedrockResourcePackFile().getAbsolutePath()));
        sender.sendMessage(ColorUtil.translate("&7Place your .geo.json files in: " + plugin.getResourcePackManager().getBedrockModelsFolder().getAbsolutePath()));
    }

    private void handleGenerateAllPacks(CommandSender sender) {
        if (!sender.isOp() && !sender.hasPermission("arpg.admin")) {
            sender.sendMessage(ColorUtil.translate("&cYou don't have permission to use this command."));
            return;
        }

        sender.sendMessage(ColorUtil.translate("&eGenerating both Java and Bedrock resource packs..."));
        plugin.getResourcePackManager().generateAllResourcePacks();
        sender.sendMessage(ColorUtil.translate("&aBoth resource packs generated successfully!"));
        sender.sendMessage(ColorUtil.translate("&7Java pack: " + plugin.getResourcePackManager().getResourcePackFile().getAbsolutePath()));
        sender.sendMessage(ColorUtil.translate("&7Bedrock pack: " + plugin.getResourcePackManager().getBedrockResourcePackFile().getAbsolutePath()));
    }

    // ══════════════════════════════════════════════════════════════════════════
    // Help menus
    // ══════════════════════════════════════════════════════════════════════════

    private void sendHelp(CommandSender sender) {
        sender.sendMessage(ColorUtil.translate("&8&l=== &fARPGEssentialsX &8&l==="));
        sender.sendMessage(ColorUtil.translate("&7/arpg class pick &8- &7Open the class selection menu."));
        sender.sendMessage(ColorUtil.translate("&7/arpg class reset &8- &7Reset your class."));
        sender.sendMessage(ColorUtil.translate("&7/arpg class reset <player> &8- &7Reset another player's class."));
        sender.sendMessage(ColorUtil.translate("&7/arpg party &8- &7Open the party menu."));
        sender.sendMessage(ColorUtil.translate("&7/arpg party invite <player> &8- &7Invite a player (leader)."));
        sender.sendMessage(ColorUtil.translate("&7/arpg party invite <accept|decline> &8- &7Respond to an invite."));
        sender.sendMessage(ColorUtil.translate("&7/arpg party request <accept|decline> &8- &7Respond to join request (leader)."));
        sender.sendMessage(ColorUtil.translate("&7/arpg party name <name> &8- &7Rename your party (leader)."));
        sender.sendMessage(ColorUtil.translate("&7/arpg party leave &8- &7Leave your current party."));
        sender.sendMessage(ColorUtil.translate("&7/arpg stats &8- &7Toggle character stats HUD."));
        if (sender.isOp() || sender.hasPermission("arpg.admin")) {
            sender.sendMessage(ColorUtil.translate("&7/arpg admin &8- &7Open the admin item menu."));
            sender.sendMessage(ColorUtil.translate("&7/arpg generatepack &8- &7Generate the Java resource pack."));
            sender.sendMessage(ColorUtil.translate("&7/arpg generatebedrock &8- &7Generate the Bedrock resource pack."));
            sender.sendMessage(ColorUtil.translate("&7/arpg generateall &8- &7Generate both Java and Bedrock packs."));
        }
    }

    private void sendPartyHelp(Player player) {
        player.sendMessage(ColorUtil.translate("&8&l=== &fParty Commands &8&l==="));
        player.sendMessage(ColorUtil.translate("&7/arpg party &8- &7Open the party menu."));
        player.sendMessage(ColorUtil.translate("&7/arpg party name <name> &8- &7Rename your party."));
        player.sendMessage(ColorUtil.translate("&7/arpg party invite <player> &8- &7Invite a player."));
        player.sendMessage(ColorUtil.translate("&7/arpg party invite <accept|decline> &8- &7Respond to invite."));
        player.sendMessage(ColorUtil.translate("&7/arpg party request <accept|decline> &8- &7Respond to join request."));
        player.sendMessage(ColorUtil.translate("&7/arpg party leave &8- &7Leave the party."));
    }
}