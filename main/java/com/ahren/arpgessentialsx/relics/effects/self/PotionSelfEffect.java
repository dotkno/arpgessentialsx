package com.ahren.arpgessentialsx.relics.effects.self;

import com.ahren.arpgessentialsx.relics.RelicEffect;
import com.ahren.arpgessentialsx.relics.RelicEffectContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Applies any potion effect to the caster.
 *
 * yml params:
 *   effect: SPEED      (Bukkit PotionEffectType name)
 *   duration: 5.0      (seconds, default 5.0)
 *   amplifier: 0       (0 = level I, default 0)
 *   ambient: false     (smaller particles, default false)
 */
public final class PotionSelfEffect implements RelicEffect {
    @Override
    public void execute(RelicEffectContext ctx) {
        String effectName = ctx.getString("effect", "SPEED").toUpperCase();
        int ticks = (int)(ctx.getDouble("duration", 5.0) * 20);
        int amp = ctx.getInt("amplifier", 0);
        boolean ambient = ctx.getBoolean("ambient", false);

        PotionEffectType type = PotionEffectType.getByName(effectName);
        if (type == null) {
            ctx.getPlugin().getLogger().warning("[PotionSelfEffect] Unknown effect: " + effectName);
            return;
        }
        ctx.getCaster().addPotionEffect(new PotionEffect(type, ticks, amp, ambient, true));
    }
}