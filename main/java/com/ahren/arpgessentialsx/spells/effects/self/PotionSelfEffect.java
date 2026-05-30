package com.ahren.arpgessentialsx.spells.effects.self;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Applies a potion effect to the caster.
 *
 * yml params:
 *   effect: SPEED     (Bukkit PotionEffectType name, default SPEED)
 *   duration: 5.0     (seconds, default 5.0)
 *   amplifier: 0      (0 = level I, default 0)
 */
public final class PotionSelfEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        String effectName = ctx.getString("effect", "SPEED").toUpperCase();
        int ticks = (int)(ctx.getDouble("duration", 5.0) * 20);
        int amp = ctx.getInt("amplifier", 0);

        PotionEffectType type = PotionEffectType.getByName(effectName);
        if (type == null) {
            ctx.getPlugin().getLogger().warning("[PotionSelfEffect] Unknown effect: " + effectName);
            return;
        }
        ctx.getCaster().addPotionEffect(new PotionEffect(type, ticks, amp, false, true));
    }
}