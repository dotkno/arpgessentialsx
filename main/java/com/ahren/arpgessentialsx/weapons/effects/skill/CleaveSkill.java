package com.ahren.arpgessentialsx.weapons.effects.skill;

import com.ahren.arpgessentialsx.weapons.WeaponEffect;
import com.ahren.arpgessentialsx.weapons.WeaponEffectContext;
import com.ahren.arpgessentialsx.util.TargetFilter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

/**
 * Hits all entities within a frontal arc — like a wide sword swing.
 * Only damages enemies in front of the attacker, not behind.
 * Respects party membership - party members and their tamed pets are not affected.
 *
 * yml params:
 *   radius: 3.5      (default 3.5)
 *   damage: 4.0      (default 4.0)
 *   arc: 120.0       (degrees of frontal arc, default 120)
 */
public final class CleaveSkill implements WeaponEffect {
    @Override
    public void execute(WeaponEffectContext ctx) {
        double radius = ctx.getDouble("radius", 3.5);
        double damage = ctx.getDouble("damage", 4.0);
        double arc    = ctx.getDouble("arc", 120.0);

        Vector facing = ctx.getAttacker().getEyeLocation()
                .getDirection().setY(0).normalize();

        // Damage is a negative effect
        boolean isPositive = false;

        for (Entity e : ctx.getAttacker().getNearbyEntities(radius, radius, radius)) {
            if (!(e instanceof LivingEntity le) || e.equals(ctx.getAttacker())) continue;

            Vector toTarget = e.getLocation().toVector()
                    .subtract(ctx.getAttacker().getLocation().toVector())
                    .setY(0).normalize();

            double angle = Math.toDegrees(Math.acos(
                    Math.max(-1, Math.min(1, facing.dot(toTarget)))));

            if (angle <= arc / 2.0) {
                // Use TargetFilter to respect party membership and tamed pets
                if (TargetFilter.shouldApplyEffect(ctx.getAttacker(), e, isPositive)) {
                    le.damage(damage, ctx.getAttacker());
                }
            }
        }
    }
}