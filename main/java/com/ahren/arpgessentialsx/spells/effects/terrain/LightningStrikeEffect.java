package com.ahren.arpgessentialsx.spells.effects.terrain;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.Location;

/**
 * Strikes lightning at the target or caster's look direction.
 *
 * yml params:
 *   target: look_direction  (look_direction | target_entity | caster, default look_direction)
 *   damage: true            (whether lightning deals damage, default true)
 */
public final class LightningStrikeEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        String targetMode = ctx.getString("target", "look_direction");
        boolean damage = ctx.getBoolean("damage", true);

        Location strikeAt = switch (targetMode.toLowerCase()) {
            case "target_entity" -> ctx.hasTarget() ? ctx.getLookedAtTarget().getLocation() : ctx.getCastLocation();
            case "caster"        -> ctx.getCaster().getLocation();
            default              -> {
                // look_direction: find where caster is looking up to 20 blocks
                var block = ctx.getCaster().getTargetBlockExact(20);
                yield block != null ? block.getLocation() : ctx.getCastLocation();
            }
        };

        if (damage) ctx.getCaster().getWorld().strikeLightning(strikeAt);
        else ctx.getCaster().getWorld().strikeLightningEffect(strikeAt);
    }
}