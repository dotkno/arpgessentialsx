package com.ahren.arpgessentialsx.weapons.passives.defensive;

import com.ahren.arpgessentialsx.weapons.passives.WeaponPassive;
import com.ahren.arpgessentialsx.weapons.passives.WeaponPassiveContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

/**
 * Heals the player for a percentage of damage dealt on each hit.
 *
 * yml params:
 *   percent: 10.0   (% of damage dealt returned as HP, default 10.0)
 *
 * Trigger: ON_HIT
 */
public final class PassiveLifestealPassive implements WeaponPassive {
    @Override
    public Trigger getTrigger() {
        return Trigger.ON_HIT;
    }

    @Override
    public void apply(WeaponPassiveContext ctx) {
        if (ctx.getTrigger() != WeaponPassiveContext.Trigger.ON_HIT) return;
        if (!ctx.hasEvent()) return;

        double percent = ctx.getDouble("percent", ctx.getDouble("lifesteal_percent", 10.0));
        // If percent is less than 1.0, treat it as a decimal (e.g., 0.15 = 15%)
        // If percent is 1.0 or greater, treat it as a percentage (e.g., 15.0 = 15%)
        double multiplier = percent < 1.0 ? percent : percent / 100.0;
        double heal = ctx.getEvent().getDamage() * multiplier;

        AttributeInstance maxHp = ctx.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double max = maxHp != null ? maxHp.getValue() : 20.0;
        double newHp = Math.min(ctx.getPlayer().getHealth() + heal, max);
        ctx.getPlayer().setHealth(newHp);
    }
}