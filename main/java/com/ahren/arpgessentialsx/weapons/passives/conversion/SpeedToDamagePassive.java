package com.ahren.arpgessentialsx.weapons.passives.conversion;

import com.ahren.arpgessentialsx.weapons.passives.WeaponPassive;
import com.ahren.arpgessentialsx.weapons.passives.WeaponPassiveContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

/**
 * Adds bonus damage scaled by the player's movement speed attribute.
 * Rewards Assassin / Marksman builds that invest in speed.
 *
 * Vanilla base movement_speed = 0.1. Each +0.01 above base = 1 stack.
 *
 * yml params:
 *   multiplier: 10.0   (speed above base × multiplier = bonus damage, default 10.0)
 *
 * Trigger: ON_HIT
 */
public final class SpeedToDamagePassive implements WeaponPassive {
    @Override
    public Trigger getTrigger() {
        return Trigger.ON_HIT;
    }

    @Override
    public void apply(WeaponPassiveContext ctx) {
        if (ctx.getTrigger() != WeaponPassiveContext.Trigger.ON_HIT) return;
        if (!ctx.hasEvent()) return;

        double multiplier = ctx.getDouble("multiplier", ctx.getDouble("conversion_rate", 10.0));
        AttributeInstance speedAttr = ctx.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        double speed = speedAttr != null ? speedAttr.getValue() : 0.1;

        // Only bonus speed above vanilla base (0.1) counts
        double speedAboveBase = Math.max(0, speed - 0.1);
        double bonus = speedAboveBase * multiplier;
        ctx.getEvent().setDamage(ctx.getEvent().getDamage() + bonus);
    }
}