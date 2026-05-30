package com.ahren.arpgessentialsx.weapons.passives.utility;

import com.ahren.arpgessentialsx.weapons.passives.WeaponPassive;
import com.ahren.arpgessentialsx.weapons.passives.WeaponPassiveContext;

/**
 * Restores a flat amount of mana (XP levels) to the player on each kill.
 * Synergises with Mage spell costs.
 *
 * yml params:
 *   amount: 2   (XP levels restored per kill, default 2)
 *
 * Trigger: ON_KILL
 */
public final class ManaOnKillPassive implements WeaponPassive {
    @Override
    public Trigger getTrigger() {
        return Trigger.ON_KILL;
    }

    @Override
    public void apply(WeaponPassiveContext ctx) {
        if (ctx.getTrigger() != WeaponPassiveContext.Trigger.ON_KILL) return;
        int amount = ctx.getInt("amount", 2);
        ctx.getPlayer().giveExpLevels(amount);
    }
}