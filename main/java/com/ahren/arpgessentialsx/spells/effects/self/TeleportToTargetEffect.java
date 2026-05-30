package com.ahren.arpgessentialsx.spells.effects.self;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.Location;

/** Blinks the caster directly to the looked-at target. yml: offset_y (default 1.0) */
public final class TeleportToTargetEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        if (!ctx.hasTarget()) return;
        double offsetY = ctx.getDouble("offset_y", 1.0);
        Location dest = ctx.getLookedAtTarget().getLocation().add(0, offsetY, 0);
        dest.setYaw(ctx.getCaster().getLocation().getYaw());
        dest.setPitch(ctx.getCaster().getLocation().getPitch());
        ctx.getCaster().teleport(dest);
    }
}