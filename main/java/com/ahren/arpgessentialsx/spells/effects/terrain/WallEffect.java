package com.ahren.arpgessentialsx.spells.effects.terrain;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.util.Vector;

import java.util.ArrayList;
import java.util.List;

/**
 * Places a wall of blocks directly in front of the caster.
 *
 * yml params:
 *   block: STONE     (default STONE)
 *   width: 3         (blocks wide, default 3)
 *   height: 3        (blocks tall, default 3)
 *   distance: 2      (blocks in front of caster, default 2)
 *   duration: 10.0   (seconds, 0 = permanent, default 10.0)
 */
public final class WallEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        Material mat = Material.matchMaterial(ctx.getString("block", "STONE"));
        if (mat == null) mat = Material.STONE;
        int width = ctx.getInt("width", 3);
        int wallHeight = ctx.getInt("height", 3);
        int distance = ctx.getInt("distance", 2);
        double duration = ctx.getDouble("duration", 10.0);

        Location base = ctx.getCaster().getLocation();
        Vector forward = base.getDirection().setY(0).normalize();
        Vector right = new Vector(-forward.getZ(), 0, forward.getX());

        List<Location> placed = new ArrayList<>();
        Location wallCenter = base.clone().add(forward.clone().multiply(distance));

        for (int w = -(width / 2); w <= width / 2; w++) {
            for (int h = 0; h < wallHeight; h++) {
                Location bl = wallCenter.clone().add(right.clone().multiply(w)).add(0, h, 0);
                if (bl.getBlock().getType().isAir()) {
                    bl.getBlock().setType(mat);
                    placed.add(bl);
                }
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