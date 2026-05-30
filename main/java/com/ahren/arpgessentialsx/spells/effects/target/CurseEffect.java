package com.ahren.arpgessentialsx.spells.effects.target;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Curses the target — applies weakness so they deal less damage,
 * and wither so they take damage over time.
 *
 * yml params:
 *   duration: 6.0      (seconds, default 6.0)
 *   wither_amp: 0      (wither amplifier, default 0)
 *   weakness_amp: 1    (weakness amplifier, default 1)
 */
public final class CurseEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        if (!ctx.hasTarget()) return;
        int ticks = (int)(ctx.getDouble("duration", 6.0) * 20);
        int witherAmp = ctx.getInt("wither_amp", 0);
        int weakAmp = ctx.getInt("weakness_amp", 1);

        ctx.getLookedAtTarget().addPotionEffect(new PotionEffect(PotionEffectType.WITHER, ticks, witherAmp, false, true));
        ctx.getLookedAtTarget().addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, ticks, weakAmp, false, true));
    }
}