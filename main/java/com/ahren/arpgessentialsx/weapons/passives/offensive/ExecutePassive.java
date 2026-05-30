package com.ahren.arpgessentialsx.weapons.passives.offensive;

import com.ahren.arpgessentialsx.weapons.passives.WeaponPassive;
import com.ahren.arpgessentialsx.weapons.passives.WeaponPassiveContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.LivingEntity;

/**
 * Instantly kills the target if their HP is below a threshold percentage.
 * Sets damage to target's current health to guarantee the kill in one hit.
 *
 * yml params:
 *   threshold_percent: 15.0   (execute below this % of max HP, default 15.0)
 *
 * Trigger: ON_HIT
 */
public final class ExecutePassive implements WeaponPassive {
    @Override
    public Trigger getTrigger() {
        return Trigger.ON_HIT;
    }

    @Override
    public void apply(WeaponPassiveContext ctx) {
        if (ctx.getTrigger() != WeaponPassiveContext.Trigger.ON_HIT) return;
        if (!ctx.hasEvent() || !ctx.hasTarget()) return;

        LivingEntity target = ctx.getTarget();
        double threshold = ctx.getDouble("threshold_percent", 15.0);

        AttributeInstance maxHpAttr = target.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double maxHp = maxHpAttr != null ? maxHpAttr.getValue() : 20.0;
        double hpPercent = (target.getHealth() / maxHp) * 100.0;

        if (hpPercent < threshold) {
            // Deal enough damage to guarantee death
            ctx.getEvent().setDamage(target.getHealth() + 1.0);
        }
    }
}