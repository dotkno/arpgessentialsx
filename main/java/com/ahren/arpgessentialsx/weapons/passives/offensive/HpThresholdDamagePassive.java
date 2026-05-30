package com.ahren.arpgessentialsx.weapons.passives.offensive;

import com.ahren.arpgessentialsx.weapons.passives.WeaponPassive;
import com.ahren.arpgessentialsx.weapons.passives.WeaponPassiveContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;

/**
 * Deals bonus damage when the target's HP is below or above a threshold percentage.
 *
 * yml params:
 *   threshold: 0.5          (trigger at this % of max HP, 0.0-1.0, default 0.5)
 *   above: false            (true = bonus when above threshold, false = below, default false)
 *   bonus_multiplier: 1.3   (damage multiplier, default 1.3)
 *
 * Trigger: ON_HIT
 */
public final class HpThresholdDamagePassive implements WeaponPassive {
    @Override
    public Trigger getTrigger() {
        return Trigger.ON_HIT;
    }

    @Override
    public void apply(WeaponPassiveContext ctx) {
        if (ctx.getTrigger() != WeaponPassiveContext.Trigger.ON_HIT) return;
        if (!ctx.hasEvent() || !ctx.hasTarget()) return;

        LivingEntity target = ctx.getTarget();
        double threshold = ctx.getDouble("threshold", 0.5);
        boolean above = ctx.getBoolean("above", false);
        double multiplier = ctx.getDouble("bonus_multiplier", ctx.getDouble("multiplier", 1.3));

        AttributeInstance maxHpAttr = target.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double maxHp = maxHpAttr != null ? maxHpAttr.getValue() : 20.0;
        double hpPercent = target.getHealth() / maxHp;

        boolean shouldTrigger = above ? (hpPercent > threshold) : (hpPercent < threshold);

        if (shouldTrigger) {
            double base = ctx.getEvent().getDamage();
            ctx.getEvent().setDamage(base * multiplier);
        }
    }
}