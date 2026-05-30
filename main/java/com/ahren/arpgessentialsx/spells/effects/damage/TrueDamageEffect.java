package com.ahren.arpgessentialsx.spells.effects.damage;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import com.ahren.arpgessentialsx.util.TargetFilter;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * Deals damage that ignores armor by directly setting health.
 * Respects party membership - party members and their tamed pets are not affected.
 *
 * yml params:
 *   damage: 5.0     (default 5.0)
 *   target: looked  (looked = looked-at entity, aoe = all in radius, default looked)
 *   radius: 4.0     (used only when target: aoe, default 4.0)
 */
public final class TrueDamageEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        double damage = ctx.getDouble("damage", 5.0);
        String targetMode = ctx.getString("target", "looked");

        // True damage is always a negative effect
        boolean isPositive = false;

        if (targetMode.equalsIgnoreCase("aoe")) {
            double radius = ctx.getDouble("radius", 4.0);
            for (Entity e : ctx.getCaster().getNearbyEntities(radius, radius, radius)) {
                if (e instanceof LivingEntity le && !e.equals(ctx.getCaster())) {
                    // Use TargetFilter to respect party membership and tamed pets
                    if (TargetFilter.shouldApplyEffect(ctx.getCaster(), e, isPositive)) {
                        applyTrue(le, damage);
                    }
                }
            }
        } else {
            if (ctx.hasTarget()) {
                // Use TargetFilter to respect party membership and tamed pets
                if (TargetFilter.shouldApplyEffect(ctx.getCaster(), ctx.getLookedAtTarget(), isPositive)) {
                    applyTrue(ctx.getLookedAtTarget(), damage);
                }
            }
        }
    }

    private void applyTrue(LivingEntity target, double damage) {
        double newHealth = Math.max(0, target.getHealth() - damage);
        target.setHealth(newHealth);
    }
}