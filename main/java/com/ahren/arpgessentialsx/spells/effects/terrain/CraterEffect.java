package com.ahren.arpgessentialsx.spells.effects.terrain;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.Location;
import org.bukkit.Material;

/** Removes blocks in a sphere radius around the caster. yml: radius */
public final class CraterEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        int radius = ctx.getInt("radius", 3);
        Location center = ctx.getCaster().getLocation();
        for (int x = -radius; x <= radius; x++)
            for (int y = -radius; y <= radius; y++)
                for (int z = -radius; z <= radius; z++)
                    if (x*x + y*y + z*z <= radius*radius) {
                        Location bl = center.clone().add(x, y, z);
                        if (!bl.getBlock().getType().isAir() && bl.getBlock().getType() != Material.BEDROCK)
                            bl.getBlock().setType(Material.AIR);
                    }
    }
}