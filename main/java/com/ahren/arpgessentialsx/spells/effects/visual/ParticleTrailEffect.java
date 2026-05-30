package com.ahren.arpgessentialsx.spells.effects.visual;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.Bukkit;
import org.bukkit.Particle;

/**
 * Leaves a particle trail behind the caster for a duration.
 *
 * yml params:
 *   particle: CLOUD    (default CLOUD)
 *   duration: 3.0      (seconds, default 3.0)
 *   interval: 2        (ticks between spawns, default 2)
 */
public final class ParticleTrailEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        String particleName = ctx.getString("particle", "CLOUD").toUpperCase();
        int totalTicks = (int)(ctx.getDouble("duration", 3.0) * 20);
        int interval = ctx.getInt("interval", 2);

        Particle particle;
        try { particle = Particle.valueOf(particleName); }
        catch (IllegalArgumentException e) { particle = Particle.CLOUD; }

        final Particle finalParticle = particle;
        int[] elapsed = {0};

        Bukkit.getScheduler().runTaskTimer(ctx.getPlugin(), task -> {
            elapsed[0] += interval;
            if (elapsed[0] >= totalTicks || !ctx.getCaster().isOnline()) { task.cancel(); return; }
            ctx.getCaster().getWorld().spawnParticle(
                    finalParticle, ctx.getCaster().getLocation(), 5, 0.2, 0.2, 0.2, 0.01);
        }, 0L, interval);
    }
}