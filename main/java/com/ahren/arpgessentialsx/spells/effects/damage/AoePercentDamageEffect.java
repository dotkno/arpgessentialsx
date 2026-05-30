package com.ahren.arpgessentialsx.spells.effects.damage;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import com.ahren.arpgessentialsx.util.TargetFilter;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * Deals damage equal to X% of each target's current health in a radius.
 * Respects party membership - party members and their tamed pets are not affected.
 *
 * yml params:
 *   radius: 5.0      (default 5.0)
 *   percent: 0.20    (20% of current HP, default 0.20)
 */
public final class AoePercentDamageEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        double radius = ctx.getDouble("radius", 5.0);
        double percent = ctx.getDouble("percent", 0.20);

        // Percent damage is a negative effect
        boolean isPositive = false;

        for (Entity e : ctx.getCaster().getNearbyEntities(radius, radius, radius)) {
            if (e instanceof LivingEntity le && !e.equals(ctx.getCaster())) {
                // Use TargetFilter to respect party membership and tamed pets
                if (TargetFilter.shouldApplyEffect(ctx.getCaster(), e, isPositive)) {
                    le.damage(le.getHealth() * percent, ctx.getCaster());
                }
            }
        }
    }
}