package com.ahren.arpgessentialsx.spells.effects.terrain;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.FallingBlock;

import java.util.ArrayList;
import java.util.List;

/**
 * Places blocks in a radius around the caster, optionally removing them after a duration.
 *
 * yml params:
 *   block: SNOW_BLOCK   (default SNOW_BLOCK)
 *   radius: 3           (default 3)
 *   duration: 5.0       (seconds, 0 = permanent, default 5.0)
 */
public final class AoeBlockEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        Material mat = Material.matchMaterial(ctx.getString("block", "SNOW_BLOCK"));
        if (mat == null) mat = Material.SNOW_BLOCK;
        int radius = ctx.getInt("radius", 3);
        double duration = ctx.getDouble("duration", 5.0);

        Location center = ctx.getCaster().getLocation();
        List<Location> placed = new ArrayList<>();

        for (int x = -radius; x <= radius; x++)
            for (int z = -radius; z <= radius; z++) {
                Location bl = center.clone().add(x, 0, z);
                if (bl.getBlock().getType().isAir()) {
                    bl.getBlock().setType(mat);
                    placed.add(bl);
                }
            }

        if (duration > 0) {
            final Material finalMat = mat;
            Bukkit.getScheduler().runTaskLater(ctx.getPlugin(), () ->
                            placed.forEach(l -> { if (l.getBlock().getType() == finalMat) l.getBlock().setType(Material.AIR); }),
                    (long)(duration * 20));
        }
    }
}