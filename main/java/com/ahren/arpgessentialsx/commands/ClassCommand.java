package com.ahren.arpgessentialsx.commands;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.util.ColorUtil;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

/**
 * Handles /class commands.
 * /class pick - Open class selection GUI
 * /class reset [player] - Reset class (OP only)
 */
public final class ClassCommand implements CommandExecutor {

    private final ARPGEssentialsX plugin;

    public ClassCommand(ARPGEssentialsX plugin) {
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(ColorUtil.translate("&cUsage: /class pick | /class reset [player]"));
            return true;
        }

        switch (args[0].toLowerCase()) {
            case "pick" -> handlePick(sender);
            case "reset" -> handleReset(sender, args);
            default -> sender.sendMessage(ColorUtil.translate("&cUsage: /class pick | /class reset [player]"));
        }

        return true;
    }

    private void handlePick(CommandSender sender) {
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

    private void handleReset(CommandSender sender, String[] args) {
        if (!sender.isOp() && !sender.hasPermission("arpg.admin")) {
            sender.sendMessage(ColorUtil.translate("&cYou don't have permission to use this command."));
            return;
        }

        if (args.length >= 2) {
            // Reset a target player
            Player target = plugin.getServer().getPlayer(args[1]);
            if (target == null) {
                sender.sendMessage(ColorUtil.translate("&cPlayer '" + args[1] + "' is not online."));
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
}
