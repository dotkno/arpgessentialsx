package com.ahren.arpgessentialsx.weapons.effects.skill;

import com.ahren.arpgessentialsx.weapons.WeaponEffect;
import com.ahren.arpgessentialsx.weapons.WeaponEffectContext;
import org.bukkit.util.Vector;

/**
 * Dashes the attacker forward in their look direction.
 *
 * yml params:
 *   power: 1.5   (default 1.5)
 */
public final class DashSkill implements WeaponEffect {
    @Override
    public void execute(WeaponEffectContext ctx) {
        double power = ctx.getDouble("power", 1.5);
        Vector vel = ctx.getAttacker().getEyeLocation()
                .getDirection().setY(0).normalize().multiply(power);
        ctx.getAttacker().setVelocity(vel);
    }
}