// ── MultiProjectileEffect.java ───────────────────────────────────────────────
package com.ahren.arpgessentialsx.spells.effects.projectile;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.entity.*;
import org.bukkit.util.Vector;

/**
 * Fires X projectiles in a horizontal spread pattern.
 *
 * yml params:
 *   projectile: FIREBALL  (default FIREBALL)
 *   count: 3              (default 3)
 *   spread: 15.0          (degrees between each projectile, default 15.0)
 *   speed: 1.5            (default 1.5)
 */
public final class MultiProjectileEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        int count = ctx.getInt("count", 3);
        double spread = ctx.getDouble("spread", 15.0);
        double speed = ctx.getDouble("speed", 1.5);
        String type = ctx.getString("projectile", "FIREBALL").toUpperCase();

        Vector base = ctx.getCaster().getEyeLocation().getDirection().normalize();

        // Spread evenly: e.g. count=3, spread=15 → -15, 0, +15 degrees
        double startAngle = -((count - 1) / 2.0) * spread;

        for (int i = 0; i < count; i++) {
            double angle = Math.toRadians(startAngle + i * spread);
            Vector dir = rotateAroundY(base.clone(), angle).multiply(speed);

            Projectile proj = switch (type) {
                case "SNOWBALL" -> ctx.getCaster().launchProjectile(Snowball.class);
                case "ARROW"    -> ctx.getCaster().launchProjectile(Arrow.class);
                default         -> ctx.getCaster().launchProjectile(Fireball.class);
            };
            proj.setVelocity(dir);
        }
    }

    private Vector rotateAroundY(Vector v, double angle) {
        double cos = Math.cos(angle);
        double sin = Math.sin(angle);
        return new Vector(
                v.getX() * cos + v.getZ() * sin,
                v.getY(),
                -v.getX() * sin + v.getZ() * cos
        );
    }
}

// ── HomingProjectileEffect.java ──────────────────────────────────────────────
// File: effects/projectile/HomingProjectileEffect.java
// package com.ahren.arpgessentialsx.spells.effects.projectile;
// (Each of these needs its own file in IntelliJ — see note below)