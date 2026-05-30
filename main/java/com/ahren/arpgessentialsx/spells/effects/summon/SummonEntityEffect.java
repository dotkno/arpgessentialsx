package com.ahren.arpgessentialsx.spells.effects.summon;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.entity.EntityType;

/**
 * Spawns a mob at the cast location.
 * yml params:
 *   entity: ZOMBIE    (any EntityType name, default ZOMBIE)
 *   count: 1          (default 1)
 */
public final class SummonEntityEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        String typeName = ctx.getString("entity", "ZOMBIE").toUpperCase();
        int count = ctx.getInt("count", 1);

        EntityType type;
        try {
            type = EntityType.valueOf(typeName);
        } catch (IllegalArgumentException e) {
            ctx.getPlugin().getLogger().warning("[SummonEntityEffect] Unknown entity: " + typeName);
            return;
        }

        for (int i = 0; i < count; i++) {
            ctx.getCaster().getWorld().spawnEntity(ctx.getCastLocation(), type);
        }
    }
}