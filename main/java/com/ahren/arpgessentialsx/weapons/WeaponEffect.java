package com.ahren.arpgessentialsx.weapons;

/**
 * The contract every weapon effect must fulfill.
 *
 * Used for both:
 *   - effects_on_hit: fires every time the weapon lands a hit
 *   - skill effects:  fires when the player activates the weapon skill
 *
 * Same pattern as SpellEffect and RelicEffect.
 * Add a new effect: implement this, register in WeaponEffectRegistry, use in weapons.yml.
 */
public interface WeaponEffect {
    void execute(WeaponEffectContext ctx);
}