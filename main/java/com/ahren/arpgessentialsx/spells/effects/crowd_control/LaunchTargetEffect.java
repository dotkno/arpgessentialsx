package com.ahren.arpgessentialsx.spells.effects.crowd_control;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import com.ahren.arpgessentialsx.util.TargetFilter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.util.Vector;

/**
 * Launches targets away from the caster or straight up.
 * Respects party membership - party members and their tamed pets are not affected.
 *
 * yml params:
 *   direction: away   (away | up | toward, default away)
 *   power: 2.0        (launch strength, default 2.0)
 *   radius: 4.0       (affects all in radius, default 4.0)
 */
public final class LaunchTargetEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        double power = ctx.getDouble("power", 2.0);
        double radius = ctx.getDouble("radius", 4.0);
        String direction = ctx.getString("direction", "away");

        // Knockback is a negative effect
        boolean isPositive = false;

        for (Entity e : ctx.getCaster().getNearbyEntities(radius, radius, radius)) {
            if (!(e instanceof LivingEntity) || e.equals(ctx.getCaster())) continue;

            // Use TargetFilter to respect party membership and tamed pets
            if (!TargetFilter.shouldApplyEffect(ctx.getCaster(), e, isPositive)) continue;

            Vector vel = switch (direction.toLowerCase()) {
                case "up"    -> new Vector(0, power, 0);
                case "toward" -> ctx.getCaster().getLocation().toVector()
                        .subtract(e.getLocation().toVector()).normalize().multiply(power);
                default      -> e.getLocation().toVector()
                        .subtract(ctx.getCaster().getLocation().toVector()).normalize().multiply(power);
            };
            e.setVelocity(vel);
        }
    }
}