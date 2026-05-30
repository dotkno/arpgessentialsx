package com.ahren.arpgessentialsx.spells.effects.summon;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.*;

/**
 * Spawns a temporary wolf-familiar that attacks the caster's target.
 * Despawns after duration.
 * yml params:
 *   entity: WOLF      (default WOLF — any tameable mob)
 *   duration: 30.0    (seconds before despawn, default 30.0)
 */
public final class SummonFamiliarEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        String typeName = ctx.getString("entity", "WOLF").toUpperCase();
        int despawnTicks = (int)(ctx.getDouble("duration", 30.0) * 20);

        EntityType type;
        try { type = EntityType.valueOf(typeName); }
        catch (IllegalArgumentException e) { type = EntityType.WOLF; }

        Entity spawned = ctx.getCaster().getWorld().spawnEntity(ctx.getCastLocation(), type);

        // If it's a wolf, make it target the looked-at entity
        if (spawned instanceof Wolf wolf) {
            wolf.setOwner(ctx.getCaster());
            wolf.setAngry(true);
            if (ctx.hasTarget()) wolf.setTarget(ctx.getLookedAtTarget());
        } else if (spawned instanceof Mob mob && ctx.hasTarget()) {
            mob.setTarget(ctx.getLookedAtTarget());
        }

        Bukkit.getScheduler().runTaskLater(ctx.getPlugin(), () -> {
            if (!spawned.isDead()) spawned.remove();
        }, despawnTicks);
    }
}