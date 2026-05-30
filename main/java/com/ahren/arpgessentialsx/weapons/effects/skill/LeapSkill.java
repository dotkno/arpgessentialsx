package com.ahren.arpgessentialsx.weapons.effects.skill;

import com.ahren.arpgessentialsx.weapons.WeaponEffect;
import com.ahren.arpgessentialsx.weapons.WeaponEffectContext;
import org.bukkit.util.Vector;

/**
 * Launches the attacker upward and slightly forward — a combat leap.
 * Good for fighters who want to close distance from above.
 *
 * yml params:
 *   power: 1.2      (forward component, default 1.2)
 *   height: 0.8     (upward component, default 0.8)
 */
public final class LeapSkill implements WeaponEffect {
    @Override
    public void execute(WeaponEffectContext ctx) {
        double power  = ctx.getDouble("power", 1.2);
        double height = ctx.getDouble("height", 0.8);
        Vector forward = ctx.getAttacker().getEyeLocation()
                .getDirection().setY(0).normalize().multiply(power);
        forward.setY(height);
        ctx.getAttacker().setVelocity(forward);
    }
}