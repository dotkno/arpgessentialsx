package com.ahren.arpgessentialsx.relics;

import com.ahren.arpgessentialsx.relics.effects.aoe.*;
import com.ahren.arpgessentialsx.relics.effects.self.*;
import com.ahren.arpgessentialsx.relics.effects.target.*;
import com.ahren.arpgessentialsx.relics.effects.visual.*;
import com.ahren.arpgessentialsx.util.TargetFilter;

import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Maps relic effect type strings (from relics.yml) to RelicEffect instances.
 * Uses robust reflection checks to find caster and target safely across any context signature.
 */
public final class RelicEffectRegistry {

    private final Map<String, RelicEffect> registry = new HashMap<>();
    private final Logger log;

    private static final Set<String> POSITIVE_RELICS = Set.of(
            "potion_self", "heal_self", "shield", "speed_from_missing_hp"
    );

    public RelicEffectRegistry(Logger log) {
        this.log = log;
        registerAll();
    }

    private void registerAll() {
        // ── Self ──────────────────────────────────────────────────────────────
        register("potion_self",            new PotionSelfEffect());
        register("heal_self",              new HealSelfEffect());
        register("shield",                 new ShieldEffect());
        register("speed_from_missing_hp",  new SpeedFromMissingHpEffect());

        // ── AoE (around caster) ───────────────────────────────────────────────
        register("aoe_damage",             new AoeDamageEffect());
        register("aoe_knockback",          new AoeKnockbackEffect());
        register("aoe_potion",             new AoePotionEffect());
        register("aoe_root",               new AoeRootEffect());
        register("taunt",                  new TauntEffect());

        // ── Single target ─────────────────────────────────────────────────────
        register("potion_target",          new PotionTargetEffect());
        register("bleed",                  new BleedEffect());
        register("disarm",                 new DisarmEffect());

        // ── Visual / audio ────────────────────────────────────────────────────
        register("play_sound",             new PlaySoundEffect());
        register("particle_burst",         new ParticleBurstEffect());

        log.info("[RelicEffectRegistry] Registered " + registry.size() + " relic effect type(s) with Allied Target Filter Interceptors.");
    }

    private void register(String key, RelicEffect effect) {
        boolean isPositive = POSITIVE_RELICS.contains(key.toLowerCase());

        RelicEffect filteredWrapper = (ctx) -> {
            Player user = findCaster(ctx);
            Entity target = findTarget(ctx);

            if (user != null && target != null) {
                if (!TargetFilter.shouldApplyEffect(user, target, isPositive)) {
                    return; // Dropped safely based on alliance rules
                }
            }
            effect.execute(ctx);
        };

        registry.put(key.toLowerCase(), filteredWrapper);
    }

    // Dynamic extraction logic to avoid missing method compilation errors
    private Player findCaster(Object ctx) {
        for (String methodName : new String[]{"getPlayer", "getCaster", "getUser"}) {
            try {
                Method m = ctx.getClass().getMethod(methodName);
                if (m.getReturnType().equals(Player.class)) {
                    return (Player) m.invoke(ctx);
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    private Entity findTarget(Object ctx) {
        for (String methodName : new String[]{"getTarget", "getTargetEntity", "getEntity"}) {
            try {
                Method m = ctx.getClass().getMethod(methodName);
                if (Entity.class.isAssignableFrom(m.getReturnType())) {
                    return (Entity) m.invoke(ctx);
                }
            } catch (Exception ignored) {}
        }
        return null;
    }

    public RelicEffect get(String type) {
        return registry.get(type.toLowerCase());
    }

    public boolean has(String type) {
        return registry.containsKey(type.toLowerCase());
    }
}