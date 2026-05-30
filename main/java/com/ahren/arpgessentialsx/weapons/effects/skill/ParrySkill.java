package com.ahren.arpgessentialsx.weapons.effects.skill;

import com.ahren.arpgessentialsx.weapons.WeaponEffect;
import com.ahren.arpgessentialsx.weapons.WeaponEffectContext;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Parry stance — grants brief damage resistance.
 * Best used for Tank/Fighter defensive weapons.
 *
 * yml params:
 *   duration: 2.0      (seconds, default 2.0)
 *   amplifier: 4       (resistance level, default 4 = Resistance V)
 */
public final class ParrySkill implements WeaponEffect {
    @Override
    public void execute(WeaponEffectContext ctx) {
        int ticks = (int)(ctx.getDouble("duration", 2.0) * 20);
        int amp   = ctx.getInt("amplifier", 4);
        ctx.getAttacker().addPotionEffect(
                new PotionEffect(PotionEffectType.RESISTANCE, ticks, amp, false, false));
    }
}