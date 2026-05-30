package com.ahren.arpgessentialsx.spells.effects.target;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Freezes the looked-at target (powder snow + slow combo). yml: duration (default 3.0) */
public final class FreezeTargetEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        if (!ctx.hasTarget()) return;
        int ticks = (int)(ctx.getDouble("duration", 3.0) * 20);
        ctx.getLookedAtTarget().setFreezeTicks(ticks);
        ctx.getLookedAtTarget().addPotionEffect(new PotionEffect(PotionEffectType.SLOWNESS, ticks, 3, false, true));
    }
}