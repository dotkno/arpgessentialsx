package com.ahren.arpgessentialsx.relics.effects.self;

import com.ahren.arpgessentialsx.relics.RelicEffect;
import com.ahren.arpgessentialsx.relics.RelicEffectContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Grants a speed boost that scales with how much health the caster is missing.
 * The lower the HP, the stronger the speed buff — perfect panic/escape tool.
 *
 * Formula:
 *   missingPercent = (maxHp - currentHp) / maxHp
 *   amplifier = floor(missingPercent * max_amplifier)
 *
 * yml params:
 *   duration: 4.0        (seconds, default 4.0)
 *   max_amplifier: 3     (speed level at 0 HP, default 3 = Speed IV)
 *   min_amplifier: 0     (speed level at full HP, default 0 = Speed I)
 */
public final class SpeedFromMissingHpEffect implements RelicEffect {
    @Override
    public void execute(RelicEffectContext ctx) {
        int ticks = (int)(ctx.getDouble("duration", 4.0) * 20);
        int maxAmp = ctx.getInt("max_amplifier", 3);
        int minAmp = ctx.getInt("min_amplifier", 0);

        AttributeInstance maxHpAttr = ctx.getCaster().getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double maxHp = maxHpAttr != null ? maxHpAttr.getValue() : 20.0;
        double currentHp = ctx.getCaster().getHealth();

        double missingPercent = Math.max(0, (maxHp - currentHp) / maxHp);
        int amplifier = (int)(minAmp + (missingPercent * (maxAmp - minAmp)));
        amplifier = Math.min(amplifier, maxAmp);

        ctx.getCaster().addPotionEffect(
                new PotionEffect(PotionEffectType.SPEED, ticks, amplifier, false, true));
    }
}