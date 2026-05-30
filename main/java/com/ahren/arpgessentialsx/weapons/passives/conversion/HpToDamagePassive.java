package com.ahren.arpgessentialsx.weapons.passives.conversion;

import com.ahren.arpgessentialsx.weapons.passives.WeaponPassive;
import com.ahren.arpgessentialsx.weapons.passives.WeaponPassiveContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

/**
 * Adds bonus damage equal to (maxHP / ratio).
 * e.g. ratio 10 with 40 maxHP → +4 damage per hit.
 *
 * yml params:
 *   ratio: 10.0   (maxHP divided by this = bonus damage, default 10.0)
 *
 * Trigger: ON_HIT
 */
public final class HpToDamagePassive implements WeaponPassive {
    @Override
    public Trigger getTrigger() {
        return Trigger.ON_HIT;
    }

    @Override
    public void apply(WeaponPassiveContext ctx) {
        if (ctx.getTrigger() != WeaponPassiveContext.Trigger.ON_HIT) return;
        if (!ctx.hasEvent()) return;

        double ratio = ctx.getDouble("ratio", 10.0);
        AttributeInstance maxHpAttr = ctx.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double maxHp = maxHpAttr != null ? maxHpAttr.getValue() : 20.0;
        double bonus = maxHp / ratio;
        ctx.getEvent().setDamage(ctx.getEvent().getDamage() + bonus);
    }
}