package com.ahren.arpgessentialsx.spells.effects.terrain;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.Sound;

import java.util.ArrayList;
import java.util.List;

/**
 * Raises a pillar of blocks under the caster.
 *
 * yml params:
 *   block: STONE      (any valid material, default STONE)
 *   height: 3         (default 3)
 *   duration: 8.0     (seconds before it disappears, default 8.0)
 */
public final class PillarEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        Material mat = Material.matchMaterial(ctx.getString("block", "STONE"));
        if (mat == null) mat = Material.STONE;
        int height = ctx.getInt("height", 3);
        int removeTicks = (int)(ctx.getDouble("duration", 8.0) * 20);

        Location loc = ctx.getCaster().getLocation().clone();
        List<Location> placed = new ArrayList<>();

        for (int i = 0; i < height; i++) {
            if (loc.getBlock().getType().isAir()) {
                loc.getBlock().setType(mat);
                placed.add(loc.clone());
            }
            loc.add(0, 1, 0);
        }

        ctx.getCaster().teleport(loc);
        ctx.getCaster().getWorld().playSound(ctx.getCaster().getLocation(), Sound.BLOCK_STONE_PLACE, 1f, 0.5f);

        final Material finalMat = mat;
        Bukkit.getScheduler().runTaskLater(ctx.getPlugin(), () -> {
            for (Location bl : placed) {
                if (bl.getBlock().getType() == finalMat) {
                    bl.getBlock().setType(Material.AIR);
                    bl.getWorld().playSound(bl, Sound.BLOCK_STONE_BREAK, 1f, 1f);
                }
            }
        }, removeTicks);
    }
}