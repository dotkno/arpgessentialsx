package com.ahren.arpgessentialsx.spells.effects.projectile;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/**
 * Launches a projectile that steers toward the nearest entity each tick.
 *
 * yml params:
 *   projectile: FIREBALL  (default FIREBALL)
 *   speed: 1.2            (default 1.2)
 *   turn_speed: 0.08      (how fast it steers, 0.0–1.0, default 0.08)
 *   lifetime: 60          (ticks before it gives up, default 60)
 */
public final class HomingProjectileEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        double speed = ctx.getDouble("speed", 1.2);
        double turnSpeed = ctx.getDouble("turn_speed", 0.08);
        int lifetime = ctx.getInt("lifetime", 60);

        Projectile proj = ctx.getCaster().launchProjectile(Fireball.class);
        proj.setVelocity(ctx.getCaster().getEyeLocation().getDirection().multiply(speed));
        proj.setGravity(false);

        int[] ticksLeft = {lifetime};

        Bukkit.getScheduler().runTaskTimer(ctx.getPlugin(), task -> {
            if (proj.isDead() || !proj.isValid() || ticksLeft[0]-- <= 0) {
                task.cancel();
                return;
            }

            // Find nearest living entity that isn't the caster
            LivingEntity nearest = null;
            double nearestDist = Double.MAX_VALUE;
            for (Entity e : proj.getNearbyEntities(16, 16, 16)) {
                if (!(e instanceof LivingEntity le)) continue;
                if (e.equals(ctx.getCaster())) continue;
                double dist = e.getLocation().distanceSquared(proj.getLocation());
                if (dist < nearestDist) { nearestDist = dist; nearest = le; }
            }

            if (nearest != null) {
                Vector toTarget = nearest.getEyeLocation()
                        .subtract(proj.getLocation()).toVector().normalize().multiply(speed);
                Vector current = proj.getVelocity();
                Vector steered = current.add(toTarget.subtract(current).multiply(turnSpeed));
                proj.setVelocity(steered.normalize().multiply(speed));
            }
        }, 1L, 1L);
    }
}