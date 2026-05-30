package com.ahren.arpgessentialsx.party.listeners;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.party.Party;
import com.ahren.arpgessentialsx.util.ColorUtil;
import com.ahren.arpgessentialsx.util.TargetFilter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerExpChangeEvent;

import java.util.List;
import java.util.UUID;

/**
 * Handles friendly fire interception, XP dispersion mechanics,
 * and allied pet tracking loops.
 */
public final class PartyGameplayListeners implements Listener {

    private final ARPGEssentialsX plugin;

    public PartyGameplayListeners(ARPGEssentialsX plugin) {
        this.plugin = plugin;
    }

    // ── Friendly Fire & Pet Protection Interceptor ───────────────────────────

    @EventHandler(priority = EventPriority.LOWEST)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Entity victim = event.getEntity();
        Player attacker = resolveAttacker(event);

        if (attacker == null || attacker.equals(victim)) return;

        // Use our universal filter layout matrix directly
        if (!TargetFilter.shouldApplyEffect(attacker, victim, false)) {
            event.setCancelled(true);
            victim.setVelocity(victim.getVelocity().multiply(0)); // Erase velocity bumps immediately

            if (victim instanceof Player) {
                ColorUtil.sendActionBar(attacker, "&7You cannot damage your party member.");
            } else {
                ColorUtil.sendActionBar(attacker, "&7You cannot damage an allied companion.");
            }
        }
    }

    // ── XP Sharing Routine ───────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerExpChange(PlayerExpChangeEvent event) {
        Player player = event.getPlayer();
        int gained = event.getAmount();
        if (gained <= 0) return;

        Party party = plugin.getPartyManager().getPartyOf(player.getUniqueId());
        if (party == null) return;

        List<UUID> onlineMembers = party.getMembers().stream()
                .filter(uuid -> Bukkit.getPlayer(uuid) != null)
                .toList();

        int memberCount = onlineMembers.size();
        if (memberCount <= 1) return;

        int share = gained / memberCount;
        if (share <= 0) return;

        for (UUID uuid : onlineMembers) {
            if (uuid.equals(player.getUniqueId())) continue;
            Player member = Bukkit.getPlayer(uuid);
            if (member != null) member.giveExp(share);
        }

        event.setAmount(share);
    }

    // ── Combat Extraction Helpers ───────────────────────────────────────────

    private Player resolveAttacker(EntityDamageByEntityEvent event) {
        if (event.getDamager() instanceof Player p) return p;
        if (event.getDamager() instanceof org.bukkit.entity.Projectile proj
                && proj.getShooter() instanceof Player p) return p;
        return null;
    }
}