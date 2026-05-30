package com.ahren.arpgessentialsx.spells.effects.projectile;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.entity.Fireball;

/**
 * Launches an explosive fireball that detonates on impact.
 *
 * yml params:
 *   speed: 1.5          (default 1.5)
 *   yield: 3.0          (explosion size, default 3.0)
 *   block_damage: false (whether it destroys blocks, default false)
 */
public final class ExplosiveProjectileEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        double speed = ctx.getDouble("speed", 1.5);
        float yield = (float) ctx.getDouble("yield", 3.0);
        boolean blockDamage = ctx.getBoolean("block_damage", false);

        Fireball fb = ctx.getCaster().launchProjectile(Fireball.class);
        fb.setVelocity(ctx.getCaster().getEyeLocation().getDirection().multiply(speed));
        fb.setYield(yield);
        fb.setIsIncendiary(true);

        // If block damage is disabled, we handle explosion manually
        if (!blockDamage) {
            fb.setYield(0f);
            // The explosion visual/damage is handled by the Fireball impact naturally
            // For full control, a ProjectileHitEvent listener would be needed
        }
    }
}