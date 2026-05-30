package com.ahren.arpgessentialsx.armors.passives.offensive;

import com.ahren.arpgessentialsx.armors.passives.ArmorPassive;
import com.ahren.arpgessentialsx.armors.passives.ArmorPassiveContext;
import org.bukkit.entity.Entity;

import java.util.Map;
import java.util.UUID;

/**
 * Passive that deals true damage if you haven't taken damage recently.
 *
 * yml params:
 *   no_damage_threshold: 5.0   (seconds)
 *   true_damage_percentage: 0.25   (25% of damage as true damage)
 *
 * Trigger: ON_HIT
 */
public final class PhantomStrikePassive implements ArmorPassive {

    // Shared damage tracker - will be set by ArmorPassiveListener
    private static Map<UUID, Long> lastDamageTime;

    public static void setDamageTracker(Map<UUID, Long> tracker) {
        lastDamageTime = tracker;
    }

    @Override
    public Trigger getTrigger() {
        return Trigger.ON_HIT;
    }

    @Override
    public void apply(ArmorPassiveContext ctx) {
        if (lastDamageTime == null) return;

        double noDamageThreshold = ctx.getArmor().getPassiveConfigs().get(0).getDouble("no_damage_threshold", 5.0);
        double trueDamagePercentage = ctx.getArmor().getPassiveConfigs().get(0).getDouble("true_damage_percentage", 0.25);

        UUID uuid = ctx.getPlayer().getUniqueId();
        Long lastDamage = lastDamageTime.get(uuid);
        
        if (lastDamage == null || (System.currentTimeMillis() - lastDamage) > (noDamageThreshold * 1000)) {
            // Deal true damage
            if (ctx.getTarget() != null && ctx.getDamageByEntityEvent() != null) {
                double baseDamage = ctx.getDamageByEntityEvent().getDamage();
                double trueDamage = baseDamage * trueDamagePercentage;
                
                // Apply true damage (bypasses armor)
                if (ctx.getTarget() instanceof org.bukkit.entity.LivingEntity) {
                    ((org.bukkit.entity.LivingEntity) ctx.getTarget()).damage(trueDamage, ctx.getPlayer());
                }
                
                // Reset timer
                lastDamageTime.put(uuid, System.currentTimeMillis());
            }
        }
    }
}
