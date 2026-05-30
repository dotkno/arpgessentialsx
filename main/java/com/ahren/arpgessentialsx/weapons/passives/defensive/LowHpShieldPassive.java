package com.ahren.arpgessentialsx.weapons.passives.defensive;

import com.ahren.arpgessentialsx.weapons.passives.WeaponPassive;
import com.ahren.arpgessentialsx.weapons.passives.WeaponPassiveContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

import java.util.UUID;

/**
 * Reduces incoming damage when the player's HP drops below a threshold.
 * Acts as a passive "last stand" shield.
 *
 * yml params:
 *   threshold: 0.3          (trigger below this % of max HP, 0.0-1.0, default 0.3)
 *   shield_amount: 6.0      (damage reduction amount, default 6.0)
 *   cooldown_ticks: 600     (ticks between shield activations, default 600)
 *
 * Trigger: ON_DAMAGE_TAKEN
 */
public final class LowHpShieldPassive implements WeaponPassive {

    private final java.util.Map<UUID, Long> lastShieldTick = new java.util.HashMap<>();

    @Override
    public Trigger getTrigger() {
        return Trigger.ON_DAMAGE_TAKEN;
    }

    @Override
    public void apply(WeaponPassiveContext ctx) {
        if (ctx.getTrigger() != WeaponPassiveContext.Trigger.ON_DAMAGE_TAKEN) return;
        if (!ctx.hasEvent()) return;

        UUID uuid = ctx.getPlayer().getUniqueId();
        long now = ctx.getPlugin().getServer().getCurrentTick();
        long cooldown = ctx.getInt("cooldown_ticks", 600);

        if (lastShieldTick.containsKey(uuid) && (now - lastShieldTick.get(uuid)) < cooldown) {
            return; // Still on cooldown
        }

        double threshold = ctx.getDouble("threshold", 0.3);
        double shieldAmount = ctx.getDouble("shield_amount", 6.0);

        AttributeInstance maxHpAttr = ctx.getPlayer().getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double maxHp = maxHpAttr != null ? maxHpAttr.getValue() : 20.0;
        double hpPercent = ctx.getPlayer().getHealth() / maxHp;

        if (hpPercent < threshold) {
            double blocked = Math.min(ctx.getEvent().getDamage(), shieldAmount);
            ctx.getEvent().setDamage(Math.max(0, ctx.getEvent().getDamage() - blocked));
            lastShieldTick.put(uuid, now);
        }
    }
}