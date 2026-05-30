package com.ahren.arpgessentialsx.armors.passives.utility;

import com.ahren.arpgessentialsx.armors.passives.ArmorPassive;
import com.ahren.arpgessentialsx.armors.passives.ArmorPassiveContext;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Passive that grants haste and movement speed on ranged kills.
 *
 * yml params:
 *   duration: 3.0   (seconds)
 *   movement_speed_bonus: 0.10   (10% movement speed)
 *   clear_slow_effects: true
 *
 * Trigger: ON_KILL
 */
public final class SnipersFocusPassive implements ArmorPassive {

    private NamespacedKey cachedKey;

    @Override
    public Trigger getTrigger() {
        return Trigger.ON_KILL;
    }

    @Override
    public void apply(ArmorPassiveContext ctx) {
        double duration = ctx.getArmor().getPassiveConfigs().get(0).getDouble("duration", 3.0);
        double movementSpeedBonus = ctx.getArmor().getPassiveConfigs().get(0).getDouble("movement_speed_bonus", 0.10);
        boolean clearSlowEffects = ctx.getArmor().getPassiveConfigs().get(0).getBoolean("clear_slow_effects", true);

        // Apply Haste II
        ctx.getPlayer().addPotionEffect(new PotionEffect(
            PotionEffectType.HASTE,
            (int) (duration * 20),
            1,
            false,
            true
        ));

        // Apply movement speed bonus
        if (cachedKey == null) {
            cachedKey = new NamespacedKey("arpgessentialsx", "sniper_focus_ms_" + ctx.getArmor().getId());
        }
        double baseSpeed = ctx.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getBaseValue();
        ctx.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).addModifier(new AttributeModifier(
            cachedKey,
            baseSpeed * movementSpeedBonus,
            AttributeModifier.Operation.ADD_NUMBER
        ));

        // Remove modifier after duration
        new org.bukkit.scheduler.BukkitRunnable() {
            @Override
            public void run() {
                ctx.getPlayer().getAttribute(Attribute.GENERIC_MOVEMENT_SPEED).getModifiers()
                    .removeIf(modifier -> modifier.getKey().equals(cachedKey));
            }
        }.runTaskLater(com.ahren.arpgessentialsx.ARPGEssentialsX.getPlugin(com.ahren.arpgessentialsx.ARPGEssentialsX.class), (long) (duration * 20));

        // Clear slow effects if specified
        if (clearSlowEffects) {
            ctx.getPlayer().removePotionEffect(PotionEffectType.SLOWNESS);
        }
    }
}
