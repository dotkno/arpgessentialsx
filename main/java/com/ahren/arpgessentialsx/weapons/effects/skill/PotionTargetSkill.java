package com.ahren.arpgessentialsx.weapons.effects.skill;

import com.ahren.arpgessentialsx.weapons.WeaponEffect;
import com.ahren.arpgessentialsx.weapons.WeaponEffectContext;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Applies a potion effect to the looked-at target. yml: effect, duration, amplifier */
public final class PotionTargetSkill implements WeaponEffect {
    @Override
    public void execute(WeaponEffectContext ctx) {
        if (!ctx.hasTarget()) return;
        String effectName = ctx.getString("effect", "SLOWNESS").toUpperCase();
        int ticks = (int)(ctx.getDouble("duration", 5.0) * 20);
        int amp   = ctx.getInt("amplifier", 0);
        PotionEffectType type = PotionEffectType.getByName(effectName);
        if (type == null) return;
        ctx.getTarget().addPotionEffect(new PotionEffect(type, ticks, amp, false, true));
    }
}