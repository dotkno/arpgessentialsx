package com.ahren.arpgessentialsx.spells.effects.visual;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.Particle;

/**
 * Spawns a burst of particles at the cast location.
 *
 * yml params:
 *   particle: FLAME    (any Bukkit Particle name, default FLAME)
 *   count: 40          (default 40)
 *   radius: 1.0        (spread radius, default 1.0)
 *   speed: 0.1         (particle speed, default 0.1)
 */
public final class ParticleBurstEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        String particleName = ctx.getString("particle", "FLAME").toUpperCase();
        int count = ctx.getInt("count", 40);
        double radius = ctx.getDouble("radius", 1.0);
        double speed = ctx.getDouble("speed", 0.1);

        Particle particle;
        try {
            particle = Particle.valueOf(particleName);
        } catch (IllegalArgumentException e) {
            ctx.getPlugin().getLogger().warning("[ParticleBurstEffect] Unknown particle: " + particleName);
            return;
        }

        ctx.getCaster().getWorld().spawnParticle(
                particle,
                ctx.getCastLocation(),
                count,
                radius, radius, radius,
                speed
        );
    }
}