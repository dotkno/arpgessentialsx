package com.ahren.arpgessentialsx.armors.passives.offensive;

import com.ahren.arpgessentialsx.armors.passives.ArmorPassive;
import com.ahren.arpgessentialsx.armors.passives.ArmorPassiveContext;
import com.ahren.arpgessentialsx.util.TargetFilter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Passive that gives basic attacks a chance to deal AoE damage.
 * Respects party membership - party members and their tamed pets are not affected.
 *
 * yml params:
 *   chance: 0.25   (25% chance)
 *   aoe_damage_percentage: 0.40   (40% of damage as AoE)
 *   aoe_radius: 3.0   (blocks)
 *
 * Trigger: ON_HIT
 */
public final class CleavingStrikesPassive implements ArmorPassive {

    @Override
    public Trigger getTrigger() {
        return Trigger.ON_HIT;
    }

    @Override
    public void apply(ArmorPassiveContext ctx) {
        double chance = ctx.getArmor().getPassiveConfigs().get(0).getDouble("chance", 0.25);
        double aoeDamagePercentage = ctx.getArmor().getPassiveConfigs().get(0).getDouble("aoe_damage_percentage", 0.40);
        double aoeRadius = ctx.getArmor().getPassiveConfigs().get(0).getDouble("aoe_radius", 3.0);

        if (ThreadLocalRandom.current().nextDouble() < chance && ctx.getTarget() != null) {
            Entity target = ctx.getTarget();
            double baseDamage = ctx.getDamageByEntityEvent() != null ? ctx.getDamageByEntityEvent().getDamage() : 0;
            double aoeDamage = baseDamage * aoeDamagePercentage;

            // Deal AoE damage to nearby entities
            // AoE damage is a negative effect
            boolean isPositive = false;
            
            target.getWorld().getNearbyEntities(target.getLocation(), aoeRadius, aoeRadius, aoeRadius).forEach(entity -> {
                if (entity instanceof LivingEntity && entity != target && entity != ctx.getPlayer()) {
                    // Use TargetFilter to respect party membership and tamed pets
                    if (TargetFilter.shouldApplyEffect(ctx.getPlayer(), entity, isPositive)) {
                        ((LivingEntity) entity).damage(aoeDamage, ctx.getPlayer());
                    }
                }
            });
        }
    }
}
