package com.ahren.arpgessentialsx.weapons.effects.skill;

import com.ahren.arpgessentialsx.weapons.WeaponEffect;
import com.ahren.arpgessentialsx.weapons.WeaponEffectContext;
import com.ahren.arpgessentialsx.util.TargetFilter;
import org.bukkit.Sound;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;

/**
 * Ground smash — damages and knocks up all nearby enemies.
 * Extra damage bonus if the attacker is airborne (fall smash).
 * Respects party membership - party members and their tamed pets are not affected.
 *
 * yml params:
 *   radius: 3.0          (default 3.0)
 *   damage: 6.0          (default 6.0)
 *   air_bonus: 0.5       (damage multiplier when airborne, default 0.5 = +50%)
 *   knockup: 0.6         (vertical launch power, default 0.6)
 */
public final class SmashSkill implements WeaponEffect {
    @Override
    public void execute(WeaponEffectContext ctx) {
        double radius   = ctx.getDouble("radius", 3.0);
        double damage   = ctx.getDouble("damage", 6.0);
        double airBonus = ctx.getDouble("air_bonus", 0.5);
        double knockup  = ctx.getDouble("knockup", 0.6);

        boolean airborne = !ctx.getAttacker().isOnGround()
                && ctx.getAttacker().getFallDistance() > 0.0f;

        double finalDamage = airborne ? damage * (1.0 + airBonus) : damage;

        // Damage and knockup are negative effects
        boolean isPositive = false;

        for (Entity e : ctx.getAttacker().getNearbyEntities(radius, radius, radius)) {
            if (!(e instanceof LivingEntity le) || e.equals(ctx.getAttacker())) continue;
            
            // Use TargetFilter to respect party membership and tamed pets
            if (TargetFilter.shouldApplyEffect(ctx.getAttacker(), e, isPositive)) {
                le.damage(finalDamage, ctx.getAttacker());
                le.setVelocity(le.getVelocity().setY(knockup));
            }
        }

        ctx.getAttacker().getWorld().playSound(
                ctx.getAttacker().getLocation(),
                Sound.ENTITY_PLAYER_ATTACK_CRIT, 1.0f, 0.5f);
    }
}