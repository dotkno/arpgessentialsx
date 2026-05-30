package com.ahren.arpgessentialsx.spells.effects.damage;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import com.ahren.arpgessentialsx.util.TargetFilter;
import org.bukkit.Bukkit;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Applies damage over time to targets.
 * Respects party membership - party members and their tamed pets are not affected.
 *
 * yml params:
 *   damage_per_tick: 1.0  (damage each tick, default 1.0)
 *   interval: 20          (ticks between each damage, default 20 = 1 second)
 *   duration: 5.0         (total seconds, default 5.0)
 *   target: looked        (looked | aoe, default looked)
 *   radius: 4.0           (used when target: aoe, default 4.0)
 */
public final class DotEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        double damage = ctx.getDouble("damage_per_tick", 1.0);
        int interval = ctx.getInt("interval", 20);
        int totalTicks = (int) (ctx.getDouble("duration", 5.0) * 20);
        int totalTasks = totalTicks / interval;
        String targetMode = ctx.getString("target", "looked");

        List<LivingEntity> targets = new ArrayList<>();
        // Damage over time is a negative effect
        boolean isPositive = false;
        
        if (targetMode.equalsIgnoreCase("aoe")) {
            double radius = ctx.getDouble("radius", 4.0);
            for (Entity e : ctx.getCaster().getNearbyEntities(radius, radius, radius)) {
                if (e instanceof LivingEntity le && !e.equals(ctx.getCaster())) {
                    // Use TargetFilter to respect party membership and tamed pets
                    if (TargetFilter.shouldApplyEffect(ctx.getCaster(), e, isPositive)) {
                        targets.add(le);
                    }
                }
            }
        } else if (ctx.hasTarget()) {
            // Use TargetFilter to respect party membership and tamed pets
            if (TargetFilter.shouldApplyEffect(ctx.getCaster(), ctx.getLookedAtTarget(), isPositive)) {
                targets.add(ctx.getLookedAtTarget());
            }
        }

        int[] fired = {0};
        Bukkit.getScheduler().runTaskTimer(ctx.getPlugin(), task -> {
            if (fired[0]++ >= totalTasks) { task.cancel(); return; }
            targets.removeIf(LivingEntity::isDead);
            for (LivingEntity le : targets) {
                le.damage(damage, ctx.getCaster());
            }
        }, interval, interval);
    }
}