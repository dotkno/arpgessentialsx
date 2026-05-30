package com.ahren.arpgessentialsx.weapons.passives.offensive;

import com.ahren.arpgessentialsx.weapons.passives.WeaponPassive;
import com.ahren.arpgessentialsx.weapons.passives.WeaponPassiveContext;
import org.bukkit.entity.LivingEntity;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

/**
 * Each consecutive hit against the same target adds a stacking damage bonus.
 * The stack resets if the player switches targets or stops hitting.
 *
 * yml params:
 *   bonus_per_stack: 0.5    (flat damage added per stack, default 0.5)
 *   max_stacks:      5      (cap, default 5)
 *   reset_window:    40     (ticks of no-hit before stack resets, default 40)
 *
 * Trigger: ON_HIT
 */
public final class ConsecutiveHitPassive implements WeaponPassive {

    /** playerUUID → [currentStack, lastTargetEntityId, lastHitTick] */
    private final Map<UUID, long[]> state = new HashMap<>();

    @Override
    public Trigger getTrigger() {
        return Trigger.ON_HIT;
    }

    @Override
    public void apply(WeaponPassiveContext ctx) {
        if (ctx.getTrigger() != WeaponPassiveContext.Trigger.ON_HIT) return;
        if (!ctx.hasEvent() || !ctx.hasTarget()) return;

        UUID playerUUID  = ctx.getPlayer().getUniqueId();
        LivingEntity target = ctx.getTarget();
        int maxStacks    = ctx.getInt("max_stacks", 5);
        int resetWindow  = ctx.getInt("reset_window", 40);
        double bonus     = ctx.getDouble("bonus_per_stack", 0.5);

        long now         = ctx.getPlugin().getServer().getCurrentTick();
        long targetId    = target.getEntityId();

        long[] s = state.getOrDefault(playerUUID, new long[]{0, -1, 0});
        long stack      = s[0];
        long lastTarget = s[1];
        long lastTick   = s[2];

        // Reset if target changed or too long between hits
        if (lastTarget != targetId || (now - lastTick) > resetWindow) {
            stack = 0;
        }

        stack = Math.min(stack + 1, maxStacks);
        state.put(playerUUID, new long[]{stack, targetId, now});

        ctx.getEvent().setDamage(ctx.getEvent().getDamage() + (bonus * stack));
    }
}