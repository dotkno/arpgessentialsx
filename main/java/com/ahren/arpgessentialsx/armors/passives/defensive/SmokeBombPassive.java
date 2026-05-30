package com.ahren.arpgessentialsx.armors.passives.defensive;

import com.ahren.arpgessentialsx.armors.passives.ArmorPassive;
import com.ahren.arpgessentialsx.armors.passives.ArmorPassiveContext;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.concurrent.ThreadLocalRandom;

/**
 * Passive that grants invisibility and movement speed on damage taken.
 *
 * yml params:
 *   chance: 0.20   (20% chance)
 *   invisibility_duration: 2.0   (seconds)
 *   movement_speed_bonus: 0.30   (30% movement speed)
 *
 * Trigger: ON_DAMAGE_TAKEN
 */
public final class SmokeBombPassive implements ArmorPassive {

    @Override
    public Trigger getTrigger() {
        return Trigger.ON_DAMAGE_TAKEN;
    }

    @Override
    public void apply(ArmorPassiveContext ctx) {
        double chance = ctx.getArmor().getPassiveConfigs().get(0).getDouble("chance", 0.20);
        double invisibilityDuration = ctx.getArmor().getPassiveConfigs().get(0).getDouble("invisibility_duration", 2.0);
        double movementSpeedBonus = ctx.getArmor().getPassiveConfigs().get(0).getDouble("movement_speed_bonus", 0.30);

        if (ThreadLocalRandom.current().nextDouble() < chance) {
            // Apply invisibility
            ctx.getPlayer().addPotionEffect(new PotionEffect(
                PotionEffectType.INVISIBILITY,
                (int) (invisibilityDuration * 20),
                0,
                false,
                true
            ));

            // Apply movement speed bonus
            NamespacedKey key = new NamespacedKey("arpgessentialsx", "smoke_bomb_ms_" + ctx.getArmor().getId());
            double baseSpeed = ctx.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
            ctx.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(new AttributeModifier(
                key,
                baseSpeed * movementSpeedBonus,
                AttributeModifier.Operation.ADD_NUMBER
            ));

            // Remove modifier after duration
            new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    ctx.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getModifiers()
                        .removeIf(modifier -> modifier.getKey().equals(key));
                }
            }.runTaskLater(com.ahren.arpgessentialsx.ARPGEssentialsX.getPlugin(com.ahren.arpgessentialsx.ARPGEssentialsX.class), (long) (invisibilityDuration * 20));
        }
    }
}
