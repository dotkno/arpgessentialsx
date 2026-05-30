package com.ahren.arpgessentialsx.armors.passives.offensive;

import com.ahren.arpgessentialsx.armors.passives.ArmorPassive;
import com.ahren.arpgessentialsx.armors.passives.ArmorPassiveContext;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Passive that roots enemies in place on ranged hit.
 *
 * yml params:
 *   chance: 0.15   (15% chance)
 *   root_duration: 1.5   (seconds)
 *
 * Trigger: ON_HIT
 */
public final class PinDownPassive implements ArmorPassive {

    @Override
    public Trigger getTrigger() {
        return Trigger.ON_HIT;
    }

    @Override
    public void apply(ArmorPassiveContext ctx) {
        double chance = ctx.getArmor().getPassiveConfigs().get(0).getDouble("chance", 0.15);
        double rootDuration = ctx.getArmor().getPassiveConfigs().get(0).getDouble("root_duration", 1.5);

        if (ThreadLocalRandom.current().nextDouble() < chance && ctx.getTarget() instanceof LivingEntity) {
            LivingEntity target = (LivingEntity) ctx.getTarget();
            
            // Apply slowness X as root effect
            target.addPotionEffect(new PotionEffect(
                PotionEffectType.SLOWNESS,
                (int) (rootDuration * 20),
                10,
                false,
                true
            ));
            
            // Apply jump boost negative to prevent jumping
            target.addPotionEffect(new PotionEffect(
                PotionEffectType.JUMP_BOOST,
                (int) (rootDuration * 20),
                -10,
                false,
                true
            ));
        }
    }
}
