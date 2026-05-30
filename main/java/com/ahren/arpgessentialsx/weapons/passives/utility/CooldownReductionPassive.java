package com.ahren.arpgessentialsx.weapons.passives.utility;

import com.ahren.arpgessentialsx.weapons.passives.WeaponPassive;
import com.ahren.arpgessentialsx.weapons.passives.WeaponPassiveContext;

/**
 * Reduces the weapon's skill cooldown by a flat amount on each kill.
 * Uses SkillCooldownTracker to modify the active cooldown task directly.
 *
 * yml params:
 *   reduction: 3.0   (seconds to cut from skill cooldown per kill, default 3.0)
 *
 * Trigger: ON_KILL
 */
public final class CooldownReductionPassive implements WeaponPassive {
    @Override
    public Trigger getTrigger() {
        return Trigger.ON_KILL;
    }

    @Override
    public void apply(WeaponPassiveContext ctx) {
        if (ctx.getTrigger() != WeaponPassiveContext.Trigger.ON_KILL) return;
        if (ctx.getCooldownTracker() == null) return;

        double reduction = ctx.getDouble("reduction", 3.0);
        ctx.getCooldownTracker().reduceCooldown(
                ctx.getPlayer().getUniqueId(),
                ctx.getWeapon().getId(),
                reduction
        );
    }
}