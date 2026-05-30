package com.ahren.arpgessentialsx.weapons.effects.skill;

import com.ahren.arpgessentialsx.weapons.WeaponEffect;
import com.ahren.arpgessentialsx.weapons.WeaponEffectContext;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Applies a potion effect to the attacker. yml: effect, duration, amplifier */
public final class PotionSelfSkill implements WeaponEffect {
    @Override
    public void execute(WeaponEffectContext ctx) {
        String effectName = ctx.getString("effect", "STRENGTH").toUpperCase();
        int ticks = (int)(ctx.getDouble("duration", 5.0) * 20);
        int amp   = ctx.getInt("amplifier", 0);
        PotionEffectType type = PotionEffectType.getByName(effectName);
        if (type == null) return;
        ctx.getAttacker().addPotionEffect(new PotionEffect(type, ticks, amp, false, true));
    }
}