package com.ahren.arpgessentialsx.weapons.passives.conversion;

import com.ahren.arpgessentialsx.weapons.passives.WeaponPassive;
import com.ahren.arpgessentialsx.weapons.passives.WeaponPassiveContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

/**
 * Adds bonus damage equal to (totalArmor / ratio).
 * Rewards Tank-style builds that invest in armor.
 *
 * yml params:
 *   ratio: 5.0   (armor points divided by this = bonus damage, default 5.0)
 *
 * Trigger: ON_HIT
 */
public final class ArmorToDamagePassive implements WeaponPassive {
    @Override
    public Trigger getTrigger() {
        return Trigger.ON_HIT;
    }

    @Override
    public void apply(WeaponPassiveContext ctx) {
        if (ctx.getTrigger() != WeaponPassiveContext.Trigger.ON_HIT) return;
        if (!ctx.hasEvent()) return;

        double ratio = ctx.getDouble("ratio", 5.0);
        AttributeInstance armorAttr = ctx.getPlayer().getAttribute(Attribute.GENERIC_ARMOR);
        double armor = armorAttr != null ? armorAttr.getValue() : 0.0;
        double bonus = armor / ratio;
        ctx.getEvent().setDamage(ctx.getEvent().getDamage() + bonus);
    }
}