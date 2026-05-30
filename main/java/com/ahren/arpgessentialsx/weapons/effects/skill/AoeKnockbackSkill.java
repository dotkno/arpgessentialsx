package com.ahren.arpgessentialsx.weapons.effects.skill;

import com.ahren.arpgessentialsx.weapons.WeaponEffect;
import com.ahren.arpgessentialsx.weapons.WeaponEffectContext;
import com.ahren.arpgessentialsx.util.TargetFilter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.util.Vector;

/**
 * Knocks all nearby entities away from the attacker.
 * Respects party membership - party members and their tamed pets are not affected.
 *
 * yml params:
 *   radius: 3.0      (default 3.0)
 *   strength: 1.5    (default 1.5)
 *   vertical: 0.3    (default 0.3)
 */
public final class AoeKnockbackSkill implements WeaponEffect {
    @Override
    public void execute(WeaponEffectContext ctx) {
        double radius   = ctx.getDouble("radius", 3.0);
        double strength = ctx.getDouble("strength", 1.5);
        double vertical = ctx.getDouble("vertical", 0.3);

        // Knockback is a negative effect
        boolean isPositive = false;

        for (Entity e : ctx.getAttacker().getNearbyEntities(radius, radius, radius)) {
            if (!(e instanceof LivingEntity) || e.equals(ctx.getAttacker())) continue;
            
            // Use TargetFilter to respect party membership and tamed pets
            if (TargetFilter.shouldApplyEffect(ctx.getAttacker(), e, isPositive)) {
                Vector dir = e.getLocation().toVector()
                        .subtract(ctx.getAttacker().getLocation().toVector())
                        .normalize().multiply(strength);
                dir.setY(vertical);
                e.setVelocity(dir);
            }
        }
    }
}