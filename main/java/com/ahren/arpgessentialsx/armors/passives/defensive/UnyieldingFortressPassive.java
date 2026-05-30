package com.ahren.arpgessentialsx.armors.passives.defensive;

import com.ahren.arpgessentialsx.armors.passives.ArmorPassive;
import com.ahren.arpgessentialsx.armors.passives.ArmorPassiveContext;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeModifier;

import java.util.UUID;

/**
 * Passive that grants armor and toughness boost at low HP, and reflects damage.
 *
 * yml params:
 *   health_threshold: 0.50   (50% health threshold)
 *   armor_boost_percentage: 0.30   (30% armor boost)
 *   toughness_boost_percentage: 0.30   (30% toughness boost)
 *   reflect_percentage: 0.15   (15% damage reflection)
 *   cooldown: 15.0   (seconds)
 *
 * Trigger: ON_DAMAGE_TAKEN
 */
public final class UnyieldingFortressPassive implements ArmorPassive {

    private static final java.util.Map<UUID, Long> lastTriggerTime = new java.util.HashMap<>();

    @Override
    public Trigger getTrigger() {
        return Trigger.ON_DAMAGE_TAKEN;
    }

    @Override
    public void apply(ArmorPassiveContext ctx) {
        double healthThreshold = ctx.getArmor().getPassiveConfigs().get(0).getDouble("health_threshold", 0.50);
        double armorBoostPercentage = ctx.getArmor().getPassiveConfigs().get(0).getDouble("armor_boost_percentage", 0.30);
        double toughnessBoostPercentage = ctx.getArmor().getPassiveConfigs().get(0).getDouble("toughness_boost_percentage", 0.30);
        double reflectPercentage = ctx.getArmor().getPassiveConfigs().get(0).getDouble("reflect_percentage", 0.15);
        double cooldown = ctx.getArmor().getPassiveConfigs().get(0).getDouble("cooldown", 15.0);

        UUID uuid = ctx.getPlayer().getUniqueId();
        double maxHealth = ctx.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double currentHealth = ctx.getPlayer().getHealth();
        double healthPercent = currentHealth / maxHealth;

        // Check cooldown
        Long lastTrigger = lastTriggerTime.get(uuid);
        if (lastTrigger != null && (System.currentTimeMillis() - lastTrigger) < (cooldown * 1000)) {
            return;
        }

        if (healthPercent < healthThreshold) {
            // Apply armor and toughness boost
            NamespacedKey armorKey = new NamespacedKey("arpgessentialsx", "unyielding_armor_" + ctx.getArmor().getId());
            NamespacedKey toughnessKey = new NamespacedKey("arpgessentialsx", "unyielding_toughness_" + ctx.getArmor().getId());
            
            double baseArmor = ctx.getPlayer().getAttribute(Attribute.GENERIC_ARMOR).getBaseValue();
            double baseToughness = ctx.getPlayer().getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getBaseValue();
            
            ctx.getPlayer().getAttribute(Attribute.GENERIC_ARMOR).addModifier(new AttributeModifier(
                armorKey,
                baseArmor * armorBoostPercentage,
                AttributeModifier.Operation.ADD_NUMBER
            ));
            ctx.getPlayer().getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).addModifier(new AttributeModifier(
                toughnessKey,
                baseToughness * toughnessBoostPercentage,
                AttributeModifier.Operation.ADD_NUMBER
            ));

            // Remove modifiers after duration
            new org.bukkit.scheduler.BukkitRunnable() {
                @Override
                public void run() {
                    ctx.getPlayer().getAttribute(Attribute.GENERIC_ARMOR).getModifiers()
                        .removeIf(modifier -> modifier.getKey().equals(armorKey));
                    ctx.getPlayer().getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS).getModifiers()
                        .removeIf(modifier -> modifier.getKey().equals(toughnessKey));
                }
            }.runTaskLater(com.ahren.arpgessentialsx.ARPGEssentialsX.getPlugin(com.ahren.arpgessentialsx.ARPGEssentialsX.class), (long) (cooldown * 20));

            // Reflect damage
            if (ctx.getDamageEvent() instanceof org.bukkit.event.entity.EntityDamageByEntityEvent) {
                org.bukkit.event.entity.EntityDamageByEntityEvent event = (org.bukkit.event.entity.EntityDamageByEntityEvent) ctx.getDamageEvent();
                org.bukkit.entity.Entity attacker = event.getDamager();
                double damage = event.getDamage();
                double reflectDamage = damage * reflectPercentage;

                if (attacker instanceof org.bukkit.entity.LivingEntity) {
                    ((org.bukkit.entity.LivingEntity) attacker).damage(reflectDamage, ctx.getPlayer());
                }
            }

            lastTriggerTime.put(uuid, System.currentTimeMillis());
        }
    }

    public static void clearPlayer(UUID uuid) {
        lastTriggerTime.remove(uuid);
    }
}
