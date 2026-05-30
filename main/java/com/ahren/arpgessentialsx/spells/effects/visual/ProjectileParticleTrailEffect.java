package com.ahren.arpgessentialsx.spells.effects.visual;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.Projectile;

/**
 * Spawns particles along the flight path of the last launched projectile.
 * Must be placed AFTER a launch effect in the spell's effects list so the
 * projectile is already set on the context when this runs.
 *
 * yml params:
 *   particle: FLAME      (any Bukkit Particle name, default FLAME)
 *   count: 5             (particles per tick, default 5)
 *   spread: 0.1          (random offset spread, default 0.1)
 *   speed: 0.01          (particle speed, default 0.01)
 *   interval: 1          (ticks between spawns, default 1)
 *   lifetime: 100        (max ticks before giving up, default 100)
 *
 * Example — lance of fire trailing behind a small fireball:
 *   - type: launch_projectile
 *     projectile: SMALL_FIREBALL
 *     speed: 3.0
 *   - type: projectile_particle_trail
 *     particle: FLAME
 *     count: 8
 *     spread: 0.15
 */
public final class ProjectileParticleTrailEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        Projectile proj = ctx.getLastLaunchedProjectile();
        if (proj == null) {
            ctx.getPlugin().getLogger().warning(
                    "[ProjectileParticleTrail] No projectile found on context — " +
                            "make sure launch_projectile comes before this effect in the list.");
            return;
        }

        String particleName = ctx.getString("particle", "FLAME").toUpperCase();
        int count    = ctx.getInt("count", 5);
        double spread = ctx.getDouble("spread", 0.1);
        double speed  = ctx.getDouble("speed", 0.01);
        int interval  = ctx.getInt("interval", 1);
        int lifetime  = ctx.getInt("lifetime", 100);

        Particle particle;
        try {
            particle = Particle.valueOf(particleName);
        } catch (IllegalArgumentException e) {
            ctx.getPlugin().getLogger().warning(
                    "[ProjectileParticleTrail] Unknown particle: " + particleName);
            return;
        }

        final Particle finalParticle = particle;
        int[] ticks = {0};

        Bukkit.getScheduler().runTaskTimer(ctx.getPlugin(), task -> {
            // Stop if projectile is gone or lifetime exceeded
            if (proj.isDead() || !proj.isValid() || ticks[0]++ >= lifetime) {
                task.cancel();
                return;
            }

            proj.getWorld().spawnParticle(
                    finalParticle,
                    proj.getLocation(),
                    count,
                    spread, spread, spread,
                    speed
            );
        }, 0L, interval);
    }
}