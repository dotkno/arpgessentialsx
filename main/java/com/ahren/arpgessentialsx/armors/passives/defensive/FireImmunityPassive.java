package com.ahren.arpgessentialsx.armors.passives.defensive;

import com.ahren.arpgessentialsx.armors.passives.ArmorPassive;
import com.ahren.arpgessentialsx.armors.passives.ArmorPassiveContext;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Grants fire immunity when the armor is equipped.
 *
 * yml params: none
 *
 * Trigger: ON_EQUIP, ON_UNEQUIP
 */
public final class FireImmunityPassive implements ArmorPassive {

    @Override
    public Trigger getTrigger() {
        return Trigger.ON_EQUIP;
    }

    @Override
    public void apply(ArmorPassiveContext ctx) {
        if (ctx.getTrigger() == Trigger.ON_EQUIP) {
            // Apply infinite fire resistance
            ctx.getPlayer().addPotionEffect(new PotionEffect(
                    PotionEffectType.FIRE_RESISTANCE,
                    Integer.MAX_VALUE,
                    0,
                    false,
                    false));
        } else if (ctx.getTrigger() == Trigger.ON_UNEQUIP) {
            // Remove fire resistance
            ctx.getPlayer().removePotionEffect(PotionEffectType.FIRE_RESISTANCE);
        }
    }
}
