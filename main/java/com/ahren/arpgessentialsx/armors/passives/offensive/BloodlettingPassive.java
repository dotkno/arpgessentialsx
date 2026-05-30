package com.ahren.arpgessentialsx.armors.passives.offensive;

import com.ahren.arpgessentialsx.armors.passives.ArmorPassive;
import com.ahren.arpgessentialsx.armors.passives.ArmorPassiveContext;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Passive that applies a bleed effect on hit.
 *
 * yml params:
 *   chance: 0.15   (15% chance)
 *   bleed_duration: 4.0   (seconds)
 *   bleed_damage_per_second: 2.0   (damage per second)
 *
 * Trigger: ON_HIT
 */
public final class BloodlettingPassive implements ArmorPassive {

    @Override
    public Trigger getTrigger() {
        return Trigger.ON_HIT;
    }

    @Override
    public void apply(ArmorPassiveContext ctx) {
        double chance = ctx.getArmor().getPassiveConfigs().get(0).getDouble("chance", 0.15);
        double bleedDuration = ctx.getArmor().getPassiveConfigs().get(0).getDouble("bleed_duration", 4.0);
        double bleedDamagePerSecond = ctx.getArmor().getPassiveConfigs().get(0).getDouble("bleed_damage_per_second", 2.0);

        if (ThreadLocalRandom.current().nextDouble() < chance && ctx.getTarget() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) ctx.getTarget();
            
            // Apply wither effect as a bleed substitute (or custom implementation if available)
            target.addPotionEffect(new PotionEffect(
                PotionEffectType.WITHER,
                (int) (bleedDuration * 20),
                0,
                false,
                true
            ));
        }
    }
}
