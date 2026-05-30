package com.ahren.arpgessentialsx.relics.effects.visual;

import com.ahren.arpgessentialsx.relics.RelicEffect;
import com.ahren.arpgessentialsx.relics.RelicEffectContext;
import org.bukkit.Material;
import org.bukkit.Particle;

/**
 * Spawns a burst of particles at the caster's location.
 *
 * yml params:
 *   particle: SQUID_INK   (any Bukkit Particle name, default SQUID_INK)
 *   count: 40             (default 40)
 *   radius: 1.5           (spread, default 1.5)
 *   speed: 0.05           (default 0.05)
 *   material: IRON_INGOT  (required for ITEM_CRACK and BLOCK particles)
 */
public final class ParticleBurstEffect implements RelicEffect {
    @Override
    public void execute(RelicEffectContext ctx) {
        String particleName = ctx.getString("particle", "SQUID_INK").toUpperCase();
        int count = ctx.getInt("count", 40);
        double radius = ctx.getDouble("radius", 1.5);
        double speed = ctx.getDouble("speed", 0.05);
        String materialName = ctx.getString("material", null);

        Particle particle;
        try {
            particle = Particle.valueOf(particleName);
        } catch (IllegalArgumentException e) {
            ctx.getPlugin().getLogger().warning("[ParticleBurstEffect] Unknown particle: " + particleName);
            return;
        }

        // Handle particles that require material data
        if (materialName != null && (particle == Particle.ITEM || particle == Particle.BLOCK)) {
            Material mat = Material.matchMaterial(materialName);
            if (mat != null) {
                ctx.getCaster().getWorld().spawnParticle(
                        particle,
                        ctx.getCastLocation(),
                        count,
                        radius, radius, radius,
                        speed,
                        mat.createBlockData()
                );
                return;
            } else {
                ctx.getPlugin().getLogger().warning("[ParticleBurstEffect] Unknown material: " + materialName);
                return;
            }
        }

        // Standard particle spawn
        ctx.getCaster().getWorld().spawnParticle(
                particle,
                ctx.getCastLocation(),
                count,
                radius, radius, radius,
                speed
        );
    }
}