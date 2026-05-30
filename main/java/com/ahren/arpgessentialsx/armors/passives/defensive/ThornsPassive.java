package com.ahren.arpgessentialsx.armors.passives.defensive;

import com.ahren.arpgessentialsx.armors.passives.ArmorPassive;
import com.ahren.arpgessentialsx.armors.passives.ArmorPassiveContext;
import org.bukkit.entity.Entity;

/**
 * Passive that reflects damage back to attackers.
 *
 * yml params:
 *   reflect_percentage: 0.10   (10% of damage reflected)
 *
 * Trigger: ON_DAMAGE_TAKEN
 */
public final class ThornsPassive implements ArmorPassive {

    @Override
    public Trigger getTrigger() {
        return Trigger.ON_DAMAGE_TAKEN;
    }

    @Override
    public void apply(ArmorPassiveContext ctx) {
        double reflectPercentage = ctx.getArmor().getPassiveConfigs().get(0).getDouble("reflect_percentage", 0.10);

        if (ctx.getDamageEvent() instanceof org.bukkit.event.entity.EntityDamageByEntityEvent) {
            org.bukkit.event.entity.EntityDamageByEntityEvent event = (org.bukkit.event.entity.EntityDamageByEntityEvent) ctx.getDamageEvent();
            Entity attacker = event.getDamager();
            double damage = event.getDamage();
            double reflectDamage = damage * reflectPercentage;

            if (attacker instanceof org.bukkit.entity.LivingEntity) {
                ((org.bukkit.entity.LivingEntity) attacker).damage(reflectDamage, ctx.getPlayer());
            }
        }
    }
}
