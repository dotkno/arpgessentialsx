package com.ahren.arpgessentialsx.weapons.effects.skill;

import com.ahren.arpgessentialsx.weapons.WeaponEffect;
import com.ahren.arpgessentialsx.weapons.WeaponEffectContext;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * Deals damage to all entities in a radius around the attacker.
 * Core of cleave, ground slam, battle shout etc.
 *
 * yml params:
 *   radius: 3.0    (default 3.0)
 *   damage: 5.0    (default 5.0)
 */
public final class AoeDamageSkill implements WeaponEffect {
    @Override
    public void execute(WeaponEffectContext ctx) {
        double radius = ctx.getDouble("radius", 3.0);
        double damage = ctx.getDouble("damage", 5.0);
        for (Entity e : ctx.getAttacker().getNearbyEntities(radius, radius, radius)) {
            if (e instanceof LivingEntity le && !e.equals(ctx.getAttacker()))
                le.damage(damage, ctx.getAttacker());
        }
    }
}