package com.ahren.arpgessentialsx.spells.effects.damage;

import com.ahren.arpgessentialsx.armors.setbonus.SpellDamageAmplificationBonus;
import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import com.ahren.arpgessentialsx.util.TargetFilter;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * Deals flat damage to all living entities within a radius of the caster.
 * Respects party membership - party members and their tamed pets are not affected.
 *
 * yml params:
 *   radius: 5.0    (default 5.0) — scaled by catalyst radius multiplier
 *   damage: 4.0    (default 4.0) — scaled by catalyst damage multiplier
 */
public final class AoeDamageEffect implements SpellEffect {

    @Override
    public void execute(SpellEffectContext ctx) {
        double radius = ctx.catalyst().scaleRadius(ctx.getDouble("radius", 5.0));
        double damage = ctx.catalyst().scaleDamage(ctx.getDouble("damage", 4.0));

        // Apply spell damage amplification bonus (Mage 4-piece set bonus)
        double multiplier = SpellDamageAmplificationBonus.getDamageMultiplier(ctx.getCaster(), ctx.getConfig());
        damage *= multiplier;

        Player caster = ctx.getCaster();

        // Damage is always a negative effect
        boolean isPositive = false;

        for (Entity e : caster.getNearbyEntities(radius, radius, radius)) {
            if (!(e instanceof LivingEntity le)) continue;
            if (e.equals(caster)) continue;

            // Use TargetFilter to respect party membership and tamed pets
            if (TargetFilter.shouldApplyEffect(caster, e, isPositive)) {
                le.damage(damage, caster);
            }
        }
        
        // Consume stacks after use
        SpellDamageAmplificationBonus.consumeStacks(caster, ctx.getConfig());
    }
}