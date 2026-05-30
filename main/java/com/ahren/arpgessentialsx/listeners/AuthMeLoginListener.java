package com.ahren.arpgessentialsx.listeners;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import fr.xephi.authme.events.LoginEvent;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;

/**
 * Listens for AuthMe's LoginEvent.
 *
 * This event fires ONLY after a player has successfully typed their password.
 * At this point, they are fully authenticated and it is safe to apply
 * RPG class attributes.
 *
 * This class is ONLY registered if AuthMe is present (see ARPGEssentialsX.onEnable).
 * If AuthMe is not installed, this class is never loaded — so the import
 * above won't cause any errors even though we used <optional>true</optional>.
 */
public final class AuthMeLoginListener implements Listener {

    private final ARPGEssentialsX plugin;

    public AuthMeLoginListener(ARPGEssentialsX plugin) {
        this.plugin = plugin;
    }

    @EventHandler
    public void onAuthMeLogin(LoginEvent event) {
        Player player = event.getPlayer();

        // Player is now fully logged in. Safe to apply their class.
        plugin.applyClassToPlayer(player);
        
        // Restore set bonuses based on currently equipped armor
        plugin.getArmorEquipListener().getSetBonusManager().restorePlayerSetBonuses(player);
    }
}