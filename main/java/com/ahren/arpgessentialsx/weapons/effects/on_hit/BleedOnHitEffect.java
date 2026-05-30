package com.ahren.arpgessentialsx.weapons.effects.on_hit;

import com.ahren.arpgessentialsx.weapons.WeaponEffect;
import com.ahren.arpgessentialsx.weapons.WeaponEffectContext;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.metadata.FixedMetadataValue;

/**
 * Applies a bleed (damage over time) to the hit target.
 *
 * yml params:
 *   damage_per_tick: 0.5   (default 0.5)
 *   interval: 20           (ticks between damage, default 20)
 *   duration: 4.0          (seconds, default 4.0)
 */
public final class BleedOnHitEffect implements WeaponEffect {
    @Override
    public void execute(WeaponEffectContext ctx) {
        if (!ctx.hasTarget()) return;

        LivingEntity target = ctx.getTarget();
        // Prevent duplicate application if called during a bleed tick
        if (target.hasMetadata("ARPG_BLEED_TICK")) return;

        double damage  = ctx.getDouble("damage_per_tick", 0.5);
        int interval   = ctx.getInt("interval", 20);
        int totalTicks = (int) (ctx.getDouble("duration", 4.0) * 20);
        int fires      = totalTicks / interval;

        int[] fired = {0};
        Bukkit.getScheduler().runTaskTimer(ctx.getPlugin(), task -> {
            if (fired[0]++ >= fires || target.isDead()) {
                task.cancel();
                return;
            }

            // Tag the target entity right before dealing damage to avoid infinite event feedback loops
            target.setMetadata("ARPG_BLEED_TICK", new FixedMetadataValue(ctx.getPlugin(), true));
            target.damage(damage, ctx.getAttacker());
            target.removeMetadata("ARPG_BLEED_TICK", ctx.getPlugin());

            target.getWorld().spawnParticle(Particle.DAMAGE_INDICATOR,
                    target.getLocation().add(0, 1, 0), 2, 0.2, 0.2, 0.2, 0.05);
        }, interval, interval);
    }
}