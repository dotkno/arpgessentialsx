package com.ahren.arpgessentialsx.armors;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.armors.setbonus.*;
import org.bukkit.attribute.Attribute;

import java.util.UUID;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;

/**
 * Event listener for complex armor set bonuses that require event handling.
 * Handles:
 * - Mana restore on spell cast (Mage 4-piece)
 * - Cooldown reduction on kill (Marksman 4-piece)
 * - First hit damage bonus (Assassin 4-piece)
 * - Health regen pause on damage (Tank 4-piece)
 */
public final class SetBonusEventListener implements Listener {

    private final ARPGEssentialsX plugin;
    private final ArmorManager armorManager;

    public SetBonusEventListener(ARPGEssentialsX plugin, ArmorManager armorManager) {
        this.plugin = plugin;
        this.armorManager = armorManager;
    }

    /**
     * Handles first hit damage bonus (Assassin 4-piece).
     * Deals 20% increased damage when hitting an enemy at 100% health.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onEntityDamageByEntity(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        UUID uuid = player.getUniqueId();

        // First hit damage bonus (Assassin 4-piece)
        if (FirstHitDamageBonus.isActive(uuid)) {
            double maxHealth = target.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
            double currentHealth = target.getHealth();
            
            // Check if target is at full health
            if (currentHealth >= maxHealth * 0.99) {
                double bonus = FirstHitDamageBonus.getDamageBonus(uuid);
                double bonusDamage = event.getDamage() * bonus;
                event.setDamage(event.getDamage() + bonusDamage);
            }
        }

        // Record damage for health regen pause (Tank 4-piece)
        if (HealthRegenBonus.isActive(uuid)) {
            HealthRegenBonus.recordDamage(uuid);
        }
    }

    /**
     * Handles cooldown reduction on kill (Marksman 4-piece).
     * Reduces all ability cooldowns by 1.5 seconds when killing an enemy.
     */
    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        UUID uuid = killer.getUniqueId();

        if (CooldownReductionOnKillBonus.isActive(uuid)) {
            if (CooldownReductionOnKillBonus.canTrigger(uuid)) {
                double reduction = CooldownReductionOnKillBonus.getCooldownReduction(uuid);
                
                // Reduce all weapon skill cooldowns
                plugin.getWeaponManager().getSkillCooldownTracker().reduceAllCooldowns(uuid, reduction);
                
                CooldownReductionOnKillBonus.recordTrigger(uuid);
            }
        }
    }

    /**
     * Clears set bonus state when a player quits.
     */
    public void clearPlayer(UUID uuid) {
        ConditionalAttackSpeedBonus.clearPlayer(uuid);
        CooldownReductionOnKillBonus.clearPlayer(uuid);
        HealthRegenBonus.clearPlayer(uuid);
        FirstHitDamageBonus.clearPlayer(uuid);
        ManaCostReductionBonus.clearPlayer(uuid);
    }
}
