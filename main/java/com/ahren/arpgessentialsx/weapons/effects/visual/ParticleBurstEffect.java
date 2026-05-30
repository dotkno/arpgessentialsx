package com.ahren.arpgessentialsx.weapons.effects.visual;

import com.ahren.arpgessentialsx.weapons.WeaponEffect;
import com.ahren.arpgessentialsx.weapons.WeaponEffectContext;
import org.bukkit.Particle;

/**
 * Spawns a burst of particles at the attacker's location.
 * Usable in both on_hit and skill effect lists.
 *
 * yml params:
 *   particle: CRIT      (any Bukkit Particle name, default CRIT)
 *   count: 20           (default 20)
 *   spread: 0.3         (default 0.3)
 *   speed: 0.1          (default 0.1)
 */
public final class ParticleBurstEffect implements WeaponEffect {
    @Override
    public void execute(WeaponEffectContext ctx) {
        String particleName = ctx.getString("particle", "CRIT").toUpperCase();
        int count    = ctx.getInt("count", 20);
        double spread = ctx.getDouble("spread", 0.3);
        double speed  = ctx.getDouble("speed", 0.1);

        Particle particle;
        try {
            particle = Particle.valueOf(particleName);
        } catch (IllegalArgumentException e) {
            ctx.getPlugin().getLogger().warning("[ParticleBurstEffect] Unknown particle: " + particleName);
            return;
        }

        ctx.getAttacker().getWorld().spawnParticle(
                particle,
                ctx.getCastLocation(),
                count,
                spread, spread, spread,
                speed
        );
    }
}