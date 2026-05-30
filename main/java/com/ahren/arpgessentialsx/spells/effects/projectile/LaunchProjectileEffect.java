package com.ahren.arpgessentialsx.spells.effects.projectile;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/**
 * Launches a single projectile in the caster's look direction.
 * Also stores the launched projectile on the context so that
 * projectile_particle_trail can follow it.
 *
 * yml params:
 *   projectile: FIREBALL | SMALL_FIREBALL | SNOWBALL | ARROW |
 *               TRIDENT | SHULKER_BULLET | DRAGON_FIREBALL
 *   speed: 1.5          (default 1.5)
 *   yield: 2.0          (explosion yield for fireballs, default 2.0)
 */
public final class LaunchProjectileEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        String type = ctx.getString("projectile", "FIREBALL").toUpperCase();
        double speed = ctx.getDouble("speed", 1.5);
        Vector dir = ctx.getCaster().getEyeLocation().getDirection().multiply(speed);

        Projectile proj = switch (type) {
            case "SMALL_FIREBALL"  -> ctx.getCaster().launchProjectile(SmallFireball.class);
            case "SNOWBALL"        -> ctx.getCaster().launchProjectile(Snowball.class);
            case "ARROW"           -> ctx.getCaster().launchProjectile(Arrow.class);
            case "TRIDENT"         -> ctx.getCaster().launchProjectile(Trident.class);
            case "SHULKER_BULLET"  -> ctx.getCaster().launchProjectile(ShulkerBullet.class);
            case "DRAGON_FIREBALL" -> ctx.getCaster().launchProjectile(DragonFireball.class);
            default                -> ctx.getCaster().launchProjectile(Fireball.class);
        };

        proj.setVelocity(dir);

        if (proj instanceof Explosive exp) {
            exp.setYield((float) ctx.getDouble("yield", 2.0f));
        }

        // Share with trail effects
        ctx.setLastLaunchedProjectile(proj);
    }
}