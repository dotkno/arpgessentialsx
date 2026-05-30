package com.ahren.arpgessentialsx.relics.effects.target;

import com.ahren.arpgessentialsx.relics.RelicEffect;
import com.ahren.arpgessentialsx.relics.RelicEffectContext;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;

/**
 * Applies a bleed (damage over time) to the looked-at target.
 * Shows blood particles each tick for visual feedback.
 *
 * yml params:
 *   damage_per_tick: 1.0   (default 1.0)
 *   interval: 20           (ticks between damage, default 20 = 1 second)
 *   duration: 5.0          (total seconds, default 5.0)
 */
public final class BleedEffect implements RelicEffect {
    @Override
    public void execute(RelicEffectContext ctx) {
        if (!ctx.hasTarget()) return;

        double damage = ctx.getDouble("damage_per_tick", 1.0);
        int interval = ctx.getInt("interval", 20);
        int totalTicks = (int)(ctx.getDouble("duration", 5.0) * 20);
        int totalFires = totalTicks / interval;

        LivingEntity target = ctx.getLookedAtTarget();
        int[] fired = {0};

        Bukkit.getScheduler().runTaskTimer(ctx.getPlugin(), task -> {
            if (fired[0]++ >= totalFires || target.isDead()) {
                task.cancel();
                return;
            }
            target.damage(damage, ctx.getCaster());
            // Red particle burst to show bleed
            target.getWorld().spawnParticle(
                    Particle.DAMAGE_INDICATOR,
                    target.getLocation().add(0, 1, 0),
                    3, 0.3, 0.3, 0.3, 0.1);
        }, interval, interval);
    }
}