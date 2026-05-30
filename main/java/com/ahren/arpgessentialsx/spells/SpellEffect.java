package com.ahren.arpgessentialsx.spells;

/**
 * The contract every spell effect must fulfill.
 *
 * Each effect class represents ONE thing a spell can do —
 * deal damage, launch a projectile, apply a potion, etc.
 *
 * To add a brand new effect type:
 *   1. Create a class in the effects/ subpackage that implements this interface
 *   2. Register it in SpellEffectRegistry with a string key
 *   3. Use that string key in spells.yml under "effects: - type: your_key"
 *
 * That's it. No other Java files need to change.
 */
public interface SpellEffect {

    /**
     * Executes this effect.
     *
     * @param ctx Everything the effect needs — caster, target, location, config params.
     *            Never null. Individual fields inside ctx may be null (e.g., lookedAtTarget
     *            if no entity was in line of sight), so effects should null-check what they use.
     */
    void execute(SpellEffectContext ctx);
}