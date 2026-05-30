package com.ahren.arpgessentialsx.weapons.effects.on_hit;

import com.ahren.arpgessentialsx.weapons.WeaponEffect;
import com.ahren.arpgessentialsx.weapons.WeaponEffectContext;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;

/**
 * Heals the attacker for a percentage of damage dealt.
 * Uses percent of damage so it self-balances — stronger hits heal more,
 * but it never becomes overwhelming.
 *
 * yml params:
 *   percent: 0.15   (15% of damage dealt, default 0.15)
 */
public final class LifestealOnHitEffect implements WeaponEffect {
    @Override
    public void execute(WeaponEffectContext ctx) {
        double percent = ctx.getDouble("percent", 0.15);
        double heal = ctx.getDamage() * percent;
        if (heal <= 0) return;

        AttributeInstance maxHp = ctx.getAttacker().getAttribute(Attribute.GENERIC_MAX_HEALTH);
        double max = maxHp != null ? maxHp.getValue() : 20.0;
        ctx.getAttacker().setHealth(Math.min(ctx.getAttacker().getHealth() + heal, max));
    }
}