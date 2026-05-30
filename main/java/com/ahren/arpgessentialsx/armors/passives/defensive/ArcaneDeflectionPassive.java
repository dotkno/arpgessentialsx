package com.ahren.arpgessentialsx.armors.passives.defensive;

import com.ahren.arpgessentialsx.armors.passives.ArmorPassive;
import com.ahren.arpgessentialsx.armors.passives.ArmorPassiveContext;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Passive that knocks back, slows, and potentially silences attackers on melee hit.
 *
 * yml params:
 *   chance: 0.30   (30% chance)
 *   knockback_strength: 0.5
 *   slow_duration: 2.0   (seconds)
 *   slow_amplifier: 1
 *   silence_duration: 1.5   (seconds, if applicable)
 *
 * Trigger: ON_DAMAGE_TAKEN
 */
public final class ArcaneDeflectionPassive implements ArmorPassive {

    @Override
    public Trigger getTrigger() {
        return Trigger.ON_DAMAGE_TAKEN;
    }

    @Override
    public void apply(ArmorPassiveContext ctx) {
        double chance = ctx.getArmor().getPassiveConfigs().get(0).getDouble("chance", 0.30);
        double knockbackStrength = ctx.getArmor().getPassiveConfigs().get(0).getDouble("knockback_strength", 0.5);
        double slowDuration = ctx.getArmor().getPassiveConfigs().get(0).getDouble("slow_duration", 2.0);
        int slowAmplifier = ctx.getArmor().getPassiveConfigs().get(0).getInt("slow_amplifier", 1);
        double silenceDuration = ctx.getArmor().getPassiveConfigs().get(0).getDouble("silence_duration", 1.5);

        if (ThreadLocalRandom.current().nextDouble() < chance && ctx.getDamageEvent() instanceof org.bukkit.event.entity.EntityDamageByEntityEvent) {
            org.bukkit.event.entity.EntityDamageByEntityEvent event = (org.bukkit.event.entity.EntityDamageByEntityEvent) ctx.getDamageEvent();
            Entity attacker = event.getDamager();

            if (attacker instanceof LivingEntity) {
                LivingEntity livingAttacker = (LivingEntity) attacker;

                // Knock back
                org.bukkit.util.Vector direction = ctx.getPlayer().getLocation().subtract(attacker.getLocation()).toVector().normalize();
                livingAttacker.setVelocity(direction.multiply(knockbackStrength));

                // Apply slow
                livingAttacker.addPotionEffect(new PotionEffect(
                    PotionEffectType.SLOWNESS,
                    (int) (slowDuration * 20),
                    slowAmplifier,
                    false,
                    true
                ));

                // Apply silence (using blindness as placeholder or custom implementation)
                if (silenceDuration > 0) {
                    livingAttacker.addPotionEffect(new PotionEffect(
                        PotionEffectType.BLINDNESS,
                        (int) (silenceDuration * 20),
                        0,
                        false,
                        true
                    ));
                }
            }
        }
    }
}
