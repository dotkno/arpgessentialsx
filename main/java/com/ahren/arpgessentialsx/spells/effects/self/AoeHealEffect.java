package com.ahren.arpgessentialsx.spells.effects.self;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import com.ahren.arpgessentialsx.util.TargetFilter;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * Heals the caster and nearby allies.
 * Respects party membership - party members and their tamed pets are healed.
 *
 * yml params:
 *   radius: 6.0    (default 6.0) — scaled by catalyst radius multiplier
 *   amount: 4.0    (half-hearts per target, default 4.0)
 *                  — scaled by catalyst damage multiplier (spell power)
 *                  — further scaled by ctx.getHealingMultiplier()
 */
public final class AoeHealEffect implements SpellEffect {

    @Override
    public void execute(SpellEffectContext ctx) {
        double radius = ctx.catalyst().scaleRadius(ctx.getDouble("radius", 6.0));
        double amount = ctx.catalyst().scaleDamage(ctx.getDouble("amount", 4.0))
                * ctx.getHealingMultiplier();

        Player caster = ctx.getCaster();

        // Healing is always a positive effect
        boolean isPositive = true;

        // Always heal the caster
        healLivingEntity(caster, amount);

        for (Entity e : caster.getNearbyEntities(radius, radius, radius)) {
            if (e.equals(caster)) continue;

            // Use TargetFilter to respect party membership and tamed pets
            if (e instanceof LivingEntity le && TargetFilter.shouldApplyEffect(caster, e, isPositive)) {
                healLivingEntity(le, amount);
            }
        }
    }

    private void healLivingEntity(LivingEntity entity, double amount) {
        AttributeInstance maxHpAttr = entity.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        if (maxHpAttr == null) return;
        double max = maxHpAttr.getValue();
        entity.setHealth(Math.min(entity.getHealth() + amount, max));
    }

}