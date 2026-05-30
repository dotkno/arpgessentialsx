package com.ahren.arpgessentialsx.weapons.passives.offensive;

import com.ahren.arpgessentialsx.weapons.passives.WeaponPassive;
import com.ahren.arpgessentialsx.weapons.passives.WeaponPassiveContext;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * Deals extra damage for each additional enemy within radius of the player.
 * Rewards fighting multiple opponents at once.
 *
 * yml params:
 *   radius:          5.0    (detection radius in blocks, default 5.0)
 *   bonus_per_enemy: 0.5    (flat damage per nearby enemy beyond the current target, default 0.5)
 *   max_bonus:       3.0    (damage cap from this passive, default 3.0)
 *
 * Trigger: ON_HIT
 */
public final class SurroundedBonusPassive implements WeaponPassive {
    @Override
    public Trigger getTrigger() {
        return Trigger.ON_HIT;
    }

    @Override
    public void apply(WeaponPassiveContext ctx) {
        if (ctx.getTrigger() != WeaponPassiveContext.Trigger.ON_HIT) return;
        if (!ctx.hasEvent()) return;

        double radius      = ctx.getDouble("radius", 5.0);
        double bonusPerEnemy = ctx.getDouble("bonus_per_enemy", 0.5);
        double maxBonus    = ctx.getDouble("max_bonus", 3.0);
        int enemyCount = ctx.getInt("enemy_count", -1);

        long nearbyEnemies = ctx.getPlayer().getWorld()
                .getNearbyEntities(ctx.getPlayer().getLocation(), radius, radius, radius)
                .stream()
                .filter(e -> e instanceof LivingEntity
                        && !(e instanceof Player)
                        && !e.equals(ctx.getTarget()))
                .count();

        // If enemy_count is specified, only trigger if we have at least that many enemies
        if (enemyCount > 0 && nearbyEnemies < enemyCount) {
            return;
        }

        double bonus = Math.min(nearbyEnemies * bonusPerEnemy, maxBonus);
        if (bonus > 0) {
            ctx.getEvent().setDamage(ctx.getEvent().getDamage() + bonus);
        }
    }
}