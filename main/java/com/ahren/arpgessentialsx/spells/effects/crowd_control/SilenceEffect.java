package com.ahren.arpgessentialsx.spells.effects.crowd_control;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

/**
 * Prevents the target player from casting spells for a duration.
 * Works by setting a flag in SpellCastManager.
 *
 * yml params:
 *   duration: 5.0   (seconds, default 5.0)
 */
public final class SilenceEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        if (!ctx.hasTarget()) return;
        if (!(ctx.getLookedAtTarget() instanceof Player target)) return;

        int ticks = (int)(ctx.getDouble("duration", 5.0) * 20);
        ctx.getPlugin().getSpellCastManager().setSilenced(target.getUniqueId(), true);

        Bukkit.getScheduler().runTaskLater(ctx.getPlugin(), () ->
                        ctx.getPlugin().getSpellCastManager().setSilenced(target.getUniqueId(), false),
                ticks);
    }
}