package com.ahren.arpgessentialsx.weapons;

import com.ahren.arpgessentialsx.weapons.effects.on_hit.*;
import com.ahren.arpgessentialsx.weapons.effects.skill.*;
import com.ahren.arpgessentialsx.weapons.effects.visual.PlaySoundEffect;
import com.ahren.arpgessentialsx.weapons.effects.visual.ParticleBurstEffect;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Maps weapon effect type strings (from weapons.yml) to WeaponEffect instances.
 *
 * Both on_hit and skill effects share the same registry — they use the same
 * interface and context. The distinction is purely about WHEN they fire
 * (on hit vs on skill activation), not HOW they're defined.
 *
 * Adding a new effect:
 *   1. Create a class in effects/on_hit/ or effects/skill/ implementing WeaponEffect
 *   2. Add one register() line here
 *   3. Use the key in weapons.yml — done
 */
public final class WeaponEffectRegistry {

    private final Map<String, WeaponEffect> registry = new HashMap<>();
    private final Logger log;

    public WeaponEffectRegistry(Logger log) {
        this.log = log;
        registerAll();
    }

    private void registerAll() {
        // ── On-hit effects ────────────────────────────────────────────────────
        register("potion",          new PotionOnHitEffect());
        register("damage",          new DamageOnHitEffect());
        register("knockback",       new KnockbackOnHitEffect());
        register("lifesteal",       new LifestealOnHitEffect());
        register("bleed",           new BleedOnHitEffect());
        register("ignite",          new IgniteOnHitEffect());

        // ── Skill effects ─────────────────────────────────────────────────────
        register("aoe_damage",      new AoeDamageSkill());
        register("aoe_knockback",   new AoeKnockbackSkill());
        register("dash",            new DashSkill());
        register("leap",            new LeapSkill());
        register("cleave",          new CleaveSkill());
        register("potion_self",     new PotionSelfSkill());
        register("potion_target",   new PotionTargetSkill());
        register("smash",           new SmashSkill());
        register("parry",           new ParrySkill());
        register("mark_target",     new MarkTargetSkill());

        // ── Shared visual effects (usable in both on_hit and skill) ───────────
        register("play_sound",      new PlaySoundEffect());
        register("particle_burst",  new ParticleBurstEffect());

        log.info("[WeaponEffectRegistry] Registered " + registry.size() + " weapon effect type(s).");
    }

    private void register(String key, WeaponEffect effect) {
        registry.put(key.toLowerCase(), effect);
    }

    public WeaponEffect get(String type) {
        return registry.get(type.toLowerCase());
    }

    public boolean has(String type) {
        return registry.containsKey(type.toLowerCase());
    }
}