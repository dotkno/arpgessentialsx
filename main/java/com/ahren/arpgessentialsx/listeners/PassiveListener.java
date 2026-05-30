package com.ahren.arpgessentialsx.listeners;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.classes.RPGClass;
import com.ahren.arpgessentialsx.data.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Arrow;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.inventory.ItemStack;

/**
 * Handles passive damage bonuses for Assassin and Marksman.
 *
 * Assassin: Critical hits deal increased damage.
 * Marksman: Bows and crossbows deal bonus damage.
 *
 * EventPriority.HIGH: We run after most other plugins but before the damage is applied.
 * This ensures we don't conflict with damage calculation plugins.
 */
public final class PassiveListener implements Listener {

    private final ARPGEssentialsX plugin;

    public PassiveListener(ARPGEssentialsX plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamage(EntityDamageByEntityEvent event) {
        Player attacker = null;
        boolean isRanged = false;

        // Figure out who attacked and how
        if (event.getDamager() instanceof Player player) {
            attacker = player;
            isRanged = false;
        } else if (event.getDamager() instanceof Arrow arrow) {
            if (arrow.getShooter() instanceof Player player) {
                attacker = player;
                isRanged = true;
            }
        }

        // If no player caused the damage, ignore
        if (attacker == null) return;

        // Look up the attacker's class
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(attacker.getUniqueId());
        if (data == null || !data.hasClass()) return;

        RPGClass rpgClass = plugin.getClassManager().getClass(data.getClassId());
        if (rpgClass == null) return;

        // Apply Assassin crit bonus (melee only)
        if (!isRanged) {
            applyAssassinCrit(attacker, rpgClass, event);
        }

        // Apply Marksman ranged bonus
        if (isRanged) {
            applyMarksmanRanged(attacker, rpgClass, event);
        }
    }

    /**
     * Assassin: Boosts critical hit damage.
     *
     * Vanilla crit = 1.5x damage. This adds a configurable bonus to that multiplier.
     * Example: bonus 0.25 means 1.5 + 0.25 = 1.75x total crit damage.
     *
     * The event damage already includes vanilla's 1.5x, so we multiply by
     * (1.5 + bonus) / 1.5 to get the correct final number.
     */
    private void applyAssassinCrit(Player player, RPGClass rpgClass, EntityDamageByEntityEvent event) {
        double critBonus = rpgClass.getPassive("crit_multiplier_bonus");
        if (critBonus <= 0.0) return;

        // Replicate vanilla's exact crit conditions
        boolean isCrit = !player.isOnGround()
                && player.getFallDistance() > 0.0f
                && !player.isInWater()
                && !player.isInsideVehicle();

        if (isCrit) {
            double currentDamage = event.getDamage();
            double newDamage = currentDamage * (1.5 + critBonus) / 1.5;
            event.setDamage(newDamage);
        }
    }

    /**
     * Marksman: Adds flat bonus damage to bow and crossbow hits.
     */
    private void applyMarksmanRanged(Player player, RPGClass rpgClass, EntityDamageByEntityEvent event) {
        double rangedBonus = rpgClass.getPassive("ranged_damage_bonus");
        if (rangedBonus <= 0.0) return;

        ItemStack weapon = player.getInventory().getItemInMainHand();
        if (weapon.getType() == Material.BOW || weapon.getType() == Material.CROSSBOW) {
            event.setDamage(event.getDamage() + rangedBonus);
        }
    }
}