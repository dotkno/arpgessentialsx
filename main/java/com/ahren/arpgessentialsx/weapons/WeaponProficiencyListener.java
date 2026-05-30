package com.ahren.arpgessentialsx.weapons;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.classes.RPGClass;
import com.ahren.arpgessentialsx.data.PlayerData;
import com.ahren.arpgessentialsx.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityPickupItemEvent;
import org.bukkit.event.player.PlayerItemHeldEvent;
import org.bukkit.inventory.ItemStack;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Shows a one-time proficiency warning and applies the speed penalty
 * when a player holds an off-class weapon.
 *
 * Fixes over original:
 *   - Uses 1-tick delay on held event to avoid Paper slot-read timing bug
 *   - Also checks on item pickup (direct inventory click / pickup from ground)
 *   - Uses action bar instead of chat message
 *   - Clears speed penalty when switching away from any weapon (custom or not)
 */
public final class WeaponProficiencyListener implements Listener {

    private final ARPGEssentialsX plugin;
    private final WeaponItemFactory factory;
    private final WeaponCombatListener combatListener;

    /** "uuid-WEAPON_TYPE" — prevents repeated messages per session */
    private final Set<String> notifiedKeys = new HashSet<>();

    public WeaponProficiencyListener(ARPGEssentialsX plugin,
                                     WeaponItemFactory factory,
                                     WeaponCombatListener combatListener) {
        this.plugin          = plugin;
        this.factory         = factory;
        this.combatListener  = combatListener;
    }

    // ── Hotbar scroll ─────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR)
    public void onItemHeld(PlayerItemHeldEvent event) {
        Player player = event.getPlayer();

        // Always clear speed penalty when switching slots
        combatListener.applySpeedPenalty(player, 0.0);

        // 1-tick delay — Paper 1.21.x reads the new slot correctly after the event
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            checkProficiency(player);
        }, 1L);
    }

    // ── Item pickup ───────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onItemPickup(EntityPickupItemEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        // Check after pickup is processed
        Bukkit.getScheduler().runTaskLater(plugin, () -> {
            if (!player.isOnline()) return;
            checkProficiency(player);
        }, 1L);
    }

    // ── Core check ────────────────────────────────────────────────────────────

    /**
     * Checks the item currently in the player's mainhand and applies
     * proficiency effects if it's a custom weapon they're not natural with.
     */
    public void checkProficiency(Player player) {
        ItemStack held = player.getInventory().getItemInMainHand();
        if (held == null) return;

        String weaponId = factory.getWeaponId(held);
        if (weaponId == null) return;

        Weapon weapon = plugin.getWeaponManager().getWeapon(weaponId);
        if (weapon == null) return;

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        int classTag;
        
        if (data == null || !data.hasClass()) {
            // Civilian (no class) - apply heavy penalty
            classTag = 0;
        } else {
            RPGClass rpgClass = plugin.getClassManager().getClass(data.getClassId());
            if (rpgClass == null) {
                classTag = 0;
            } else {
                classTag = rpgClass.getClassTag();
            }
        }

        // Civilian (class 0) - not trained to use any weapon
        if (classTag == 0) {
            String notifyKey = player.getUniqueId() + "-CIVILIAN";
            if (notifiedKeys.add(notifyKey)) {
                ColorUtil.sendActionBar(player,
                        "&cYou're not trained enough to use this. &7(-50% damage, -50% speed)");
            }
            
            // Apply -50% speed penalty
            combatListener.applySpeedPenalty(player, -0.5);
            return;
        }

        // Catalyst — useless for non-Mages
        if (weapon.getWeaponType() == WeaponType.CATALYST && classTag != 2) {
            String notifyKey = player.getUniqueId() + "-CATALYST";
            if (notifiedKeys.add(notifyKey)) {
                ColorUtil.sendActionBar(player,
                        "&cYou have no idea how to use this. &7(Mage only)");
            }
            return;
        }

        // Off-class weapon
        if (!weapon.isNaturalFor(classTag)) {
            String notifyKey = player.getUniqueId() + "-" + weapon.getWeaponType().name();
            if (notifiedKeys.add(notifyKey)) {
                double dmgPenaltyPct = (int)((1.0 - weapon.getDamageMultiplierFor(classTag)) * 100);
                double speedPenalty  = weapon.getSpeedPenaltyFor(classTag);
                String desc = "&c-" + (int)dmgPenaltyPct + "% damage";
                if (speedPenalty < 0) desc += "&7, &cslower swing";
                ColorUtil.sendActionBar(player,
                        "&eNot proficient: " + weapon.getWeaponType().getDisplayName()
                                + " &7(" + desc + "&7)");
            }

            // Apply speed penalty
            double penalty = weapon.getSpeedPenaltyFor(classTag);
            combatListener.applySpeedPenalty(player, penalty);
        }
    }

    public void clearNotifications(UUID uuid) {
        notifiedKeys.removeIf(k -> k.startsWith(uuid.toString()));
    }
}