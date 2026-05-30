package com.ahren.arpgessentialsx.spells.effects.damage;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Deals damage to a target then jumps to nearby entities up to X times.
 * Each jump deals slightly less damage than the last.
 *
 * yml params:
 *   jumps: 4              (max chain jumps, default 4)
 *   damage_per_jump: 3.0  (damage on first hit, default 3.0)
 *   jump_radius: 6.0      (search radius for next target, default 6.0)
 *   falloff: 0.75         (damage multiplied by this each jump, default 0.75)
 */
public final class ChainLightningEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        if (!ctx.hasTarget()) return;

        int maxJumps = ctx.getInt("jumps", 4);
        double damage = ctx.getDouble("damage_per_jump", 3.0);
        double radius = ctx.getDouble("jump_radius", 6.0);
        double falloff = ctx.getDouble("falloff", 0.75);

        List<LivingEntity> hit = new ArrayList<>();
        hit.add(ctx.getCaster()); // don't chain back to caster

        LivingEntity current = ctx.getLookedAtTarget();

        for (int i = 0; i < maxJumps && current != null; i++) {
            current.damage(damage, ctx.getCaster());
            current.getWorld().strikeLightningEffect(current.getLocation());
            hit.add(current);
            damage *= falloff;

            // Find next nearest unhit entity
            LivingEntity next = null;
            double nearest = Double.MAX_VALUE;
            for (Entity e : current.getNearbyEntities(radius, radius, radius)) {
                if (!(e instanceof LivingEntity le)) continue;
                if (hit.contains(le)) continue;
                double dist = e.getLocation().distanceSquared(current.getLocation());
                if (dist < nearest) { nearest = dist; next = le; }
            }
            current = next;
        }
    }
}