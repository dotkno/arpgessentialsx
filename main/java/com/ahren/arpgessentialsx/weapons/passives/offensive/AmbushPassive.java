package com.ahren.arpgessentialsx.weapons.passives.offensive;

import com.ahren.arpgessentialsx.weapons.passives.WeaponPassive;
import com.ahren.arpgessentialsx.weapons.passives.WeaponPassiveContext;
import org.bukkit.Location;

/**
 * Deals bonus damage when the player attacks from behind the target
 * or while sneaking (crouching).
 *
 * "Behind" is determined by comparing the angle between the target's facing
 * direction and the vector from target to attacker. If that angle > 90°,
 * the attacker is behind the target.
 *
 * yml params:
 *   bonus_percent: 40.0   (extra damage as % of base hit, default 40.0)
 *   require_sneak: false  (if true, only triggers while player is sneaking, default false)
 *
 * Trigger: ON_HIT
 */
public final class AmbushPassive implements WeaponPassive {
    @Override
    public Trigger getTrigger() {
        return Trigger.ON_HIT;
    }

    @Override
    public void apply(WeaponPassiveContext ctx) {
        if (ctx.getTrigger() != WeaponPassiveContext.Trigger.ON_HIT) return;
        if (!ctx.hasEvent() || !ctx.hasTarget()) return;

        boolean requireSneak = ctx.getBoolean("require_sneak", false);
        if (requireSneak && !ctx.getPlayer().isSneaking()) return;

        boolean fromBehind = isFromBehind(ctx.getPlayer().getLocation(), ctx.getTarget().getLocation());
        boolean sneaking   = ctx.getPlayer().isSneaking();

        if (fromBehind || sneaking) {
            double bonus = ctx.getDouble("bonus_percent", 40.0);
            ctx.getEvent().setDamage(ctx.getEvent().getDamage() * (1.0 + bonus / 100.0));
        }
    }

    /**
     * Returns true if the attacker is behind the target (angle > 90° from target's facing).
     */
    private boolean isFromBehind(Location attackerLoc, Location targetLoc) {
        // Vector from target to attacker
        double dx = attackerLoc.getX() - targetLoc.getX();
        double dz = attackerLoc.getZ() - targetLoc.getZ();

        // Target's facing direction (yaw → unit vector)
        double yawRad = Math.toRadians(targetLoc.getYaw());
        double facingX = -Math.sin(yawRad);
        double facingZ =  Math.cos(yawRad);

        // Dot product: negative means attacker is in the rear half
        double dot = dx * facingX + dz * facingZ;
        return dot < 0;
    }
}