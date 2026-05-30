package com.ahren.arpgessentialsx.spells.effects.damage;

import com.ahren.arpgessentialsx.armors.setbonus.SpellDamageAmplificationBonus;
import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;

/**
 * Deals flat damage to whoever the caster is looking at.
 *
 * yml params:
 *   damage: 6.0    (default 6.0)
 */
public final class SingleTargetDamageEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        if (!ctx.hasTarget()) return;
        double damage = ctx.getDouble("damage", 6.0);
        
        // Apply spell damage amplification bonus (Mage 4-piece set bonus)
        double multiplier = SpellDamageAmplificationBonus.getDamageMultiplier(ctx.getCaster(), ctx.getConfig());
        damage *= multiplier;
        
        ctx.getLookedAtTarget().damage(damage, ctx.getCaster());
        
        // Consume stacks after use
        SpellDamageAmplificationBonus.consumeStacks(ctx.getCaster(), ctx.getConfig());
    }
}