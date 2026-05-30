package com.ahren.arpgessentialsx.spells.effects.terrain;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Drops falling blocks from above onto the target location.
 *
 * yml params:
 *   block: GRAVEL     (default GRAVEL)
 *   count: 5          (number of blocks, default 5)
 *   height: 10        (blocks above target to spawn from, default 10)
 *   spread: 2.0       (random spread radius, default 2.0)
 */
public final class SummonFallingBlockEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        Material mat = Material.matchMaterial(ctx.getString("block", "GRAVEL"));
        if (mat == null) mat = Material.GRAVEL;
        int count = ctx.getInt("count", 5);
        int height = ctx.getInt("height", 10);
        double spread = ctx.getDouble("spread", 2.0);

        Location target = ctx.hasTarget()
                ? ctx.getLookedAtTarget().getLocation()
                : ctx.getCaster().getTargetBlockExact(20) != null
                  ? ctx.getCaster().getTargetBlockExact(20).getLocation()
                  : ctx.getCaster().getLocation();

        for (int i = 0; i < count; i++) {
            double rx = (ThreadLocalRandom.current().nextDouble() - 0.5) * spread * 2;
            double rz = (ThreadLocalRandom.current().nextDouble() - 0.5) * spread * 2;
            Location spawn = target.clone().add(rx, height, rz);
            ctx.getCaster().getWorld().spawnFallingBlock(spawn, mat.createBlockData());
        }
    }
}