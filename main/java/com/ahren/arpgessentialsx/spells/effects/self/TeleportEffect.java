package com.ahren.arpgessentialsx.spells.effects.self;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.Location;
import org.bukkit.block.Block;

/**
 * Blinks the caster forward in their look direction.
 *
 * yml params:
 *   distance: 10.0   (max blink distance in blocks, default 10.0)
 */
public final class TeleportEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        double distance = ctx.getDouble("distance", 10.0);
        Block target = ctx.getCaster().getTargetBlockExact((int) distance);
        Location dest;

        if (target != null) {
            // Teleport just before the block
            dest = target.getLocation().add(0.5, 0, 0.5);
            dest.setYaw(ctx.getCaster().getLocation().getYaw());
            dest.setPitch(ctx.getCaster().getLocation().getPitch());
        } else {
            // Nothing in the way — blink the full distance
            dest = ctx.getCaster().getEyeLocation()
                    .add(ctx.getCaster().getEyeLocation().getDirection().multiply(distance));
            dest.setYaw(ctx.getCaster().getLocation().getYaw());
            dest.setPitch(ctx.getCaster().getLocation().getPitch());
        }

        ctx.getCaster().teleport(dest);
    }
}