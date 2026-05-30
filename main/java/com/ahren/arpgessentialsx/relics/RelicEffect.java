package com.ahren.arpgessentialsx.relics;

/**
 * The contract every relic effect must fulfill.
 *
 * Identical pattern to SpellEffect — one interface, many implementations.
 * Each effect class does ONE thing. Mix and match in relics.yml.
 *
 * To add a new effect type:
 *   1. Create a class in effects/ that implements this interface
 *   2. Register it in RelicEffectRegistry with a string key
 *   3. Use that key in relics.yml under "effects: - type: your_key"
 */
public interface RelicEffect {
    void execute(RelicEffectContext ctx);
}