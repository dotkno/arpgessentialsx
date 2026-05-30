package com.ahren.arpgessentialsx.weapons.effects.on_hit;

import com.ahren.arpgessentialsx.weapons.WeaponEffect;
import com.ahren.arpgessentialsx.weapons.WeaponEffectContext;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Deals bonus flat damage on top of the weapon's base hit.
 * Different from the attribute modifier bonus — this is a conditional
 * extra hit (e.g. "deals +3 damage on every hit").
 *
 * yml params:
 *   amount: 3.0   (default 3.0)
 */
public final class DamageOnHitEffect implements WeaponEffect {
    @Override
    public void execute(WeaponEffectContext ctx) {
        if (!ctx.hasTarget()) return;
        double amount = ctx.getDouble("amount", 3.0);
        
        // Modify the event damage directly to avoid triggering recursive damage events
        EntityDamageByEntityEvent event = ctx.getEvent();
        if (event != null) {
            event.setDamage(event.getDamage() + amount);
        } else {
            // Fallback for skill mode where no event exists
            ctx.getTarget().damage(amount, ctx.getAttacker());
        }
    }
}