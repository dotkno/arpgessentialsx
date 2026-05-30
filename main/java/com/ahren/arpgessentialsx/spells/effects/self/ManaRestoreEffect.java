package com.ahren.arpgessentialsx.spells.effects.self;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;

/** Instantly restores exp levels (mana). yml: amount (levels, default 5) */
public final class ManaRestoreEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        int amount = ctx.getInt("amount", 5);
        ctx.getCaster().giveExpLevels(amount);
    }
}