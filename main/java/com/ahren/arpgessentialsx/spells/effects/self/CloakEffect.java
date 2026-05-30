package com.ahren.arpgessentialsx.spells.effects.self;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Turns the caster invisible and suppresses their armor/item visibility.
 *
 * yml params:
 *   duration: 8.0   (seconds, default 8.0)
 */
public final class CloakEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        int ticks = (int)(ctx.getDouble("duration", 8.0) * 20);
        ctx.getCaster().addPotionEffect(new PotionEffect(PotionEffectType.INVISIBILITY, ticks, 0, false, false));
    }
}