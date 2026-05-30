package com.ahren.arpgessentialsx.spells.effects.self;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.util.Vector;

/**
 * Launches the caster in a direction.
 *
 * yml params:
 *   direction: forward   (forward | up | backward, default forward)
 *   power: 2.5           (default 2.5)
 */
public final class LaunchSelfEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        double power = ctx.getDouble("power", 2.5);
        String direction = ctx.getString("direction", "forward");

        Vector vel = switch (direction.toLowerCase()) {
            case "up"       -> new Vector(0, power, 0);
            case "backward" -> ctx.getCaster().getEyeLocation().getDirection().multiply(-power);
            default         -> ctx.getCaster().getEyeLocation().getDirection().multiply(power);
        };
        ctx.getCaster().setVelocity(vel);
    }
}