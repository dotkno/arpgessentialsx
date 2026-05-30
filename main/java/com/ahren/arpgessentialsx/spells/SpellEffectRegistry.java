package com.ahren.arpgessentialsx.spells;

import com.ahren.arpgessentialsx.spells.effects.crowd_control.*;
import com.ahren.arpgessentialsx.spells.effects.damage.*;
import com.ahren.arpgessentialsx.spells.effects.projectile.*;
import com.ahren.arpgessentialsx.spells.effects.self.*;
import com.ahren.arpgessentialsx.spells.effects.summon.*;
import com.ahren.arpgessentialsx.spells.effects.target.*;
import com.ahren.arpgessentialsx.spells.effects.terrain.*;
import com.ahren.arpgessentialsx.spells.effects.visual.*;
import com.ahren.arpgessentialsx.util.TargetFilter;

import org.bukkit.entity.Player;
import org.bukkit.entity.Entity;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;

/**
 * Maps effect type strings (from spells.yml) to SpellEffect instances.
 * Uses robust reflection checks to find caster and target safely across any context signature.
 */
public final class SpellEffectRegistry {

    private final Map<String, SpellEffect> registry = new HashMap<>();
    private final Logger log;

    private static final Set<String> POSITIVE_SPELLS = Set.of(
            "potion_self", "shield", "heal_self", "invulnerability", "mana_restore", "cloak", "aoe_heal"
    );

    public SpellEffectRegistry(Logger log) {
        this.log = log;
        registerAll();
    }

    private void registerAll() {
        // ── Projectile ────────────────────────────────────────────────────────
        register("launch_projectile",      new LaunchProjectileEffect());
        register("multi_projectile",       new MultiProjectileEffect());
        register("homing_projectile",      new HomingProjectileEffect());
        register("bouncing_projectile",    new BouncingProjectileEffect());
        register("explosive_projectile",   new ExplosiveProjectileEffect());
        register("projectile_particle_trail", new ProjectileParticleTrailEffect());

        // ── Damage ────────────────────────────────────────────────────────────
        register("aoe_damage",             new AoeDamageEffect());
        register("aoe_percent_damage",     new AoePercentDamageEffect());
        register("single_target_damage",   new SingleTargetDamageEffect());
        register("chain_lightning",        new ChainLightningEffect());
        register("true_damage",            new TrueDamageEffect());
        register("dot",                    new DotEffect());

        // ── Crowd Control ─────────────────────────────────────────────────────
        register("freeze",                 new FreezeEffect());
        register("launch_target",          new LaunchTargetEffect());
        register("pull_target",            new PullTargetEffect());
        register("confuse",                new ConfuseEffect());
        register("blind",                  new BlindEffect());
        register("silence",                new SilenceEffect());
        register("root",                   new RootEffect());

        // ── Terrain ───────────────────────────────────────────────────────────
        register("pillar",                 new PillarEffect());
        register("crater",                 new CraterEffect());
        register("aoe_block",              new AoeBlockEffect());
        register("summon_falling_block",   new SummonFallingBlockEffect());
        register("wall",                   new WallEffect());
        register("lightning_strike",       new LightningStrikeEffect());
        register("ignite_area",            new IgniteAreaEffect());

        // ── Self ──────────────────────────────────────────────────────────────
        register("potion_self",            new PotionSelfEffect());
        register("launch_self",            new LaunchSelfEffect());
        register("teleport",               new TeleportEffect());
        register("teleport_to_target",     new TeleportToTargetEffect());
        register("shield",                 new ShieldEffect());
        register("heal_self",              new HealSelfEffect());
        register("invulnerability",        new InvulnerabilityEffect());
        register("mana_restore",           new ManaRestoreEffect());
        register("cloak",                  new CloakEffect());
        register("aoe_heal",               new AoeHealEffect());

        // ── Target ────────────────────────────────────────────────────────────
        register("potion_target",          new PotionTargetEffect());
        register("ignite",                 new IgniteEffect());
        register("freeze_target",          new FreezeTargetEffect());
        register("disarm",                 new DisarmEffect());
        register("curse",                  new CurseEffect());

        // ── Summon ────────────────────────────────────────────────────────────
        register("summon_entity",          new SummonEntityEffect());
        register("summon_familiar",        new SummonFamiliarEffect());
        register("summon_lightning_cage",  new SummonLightningCageEffect());

        // ── Visual ────────────────────────────────────────────────────────────
        register("particle_burst",         new ParticleBurstEffect());
        register("particle_trail",         new ParticleTrailEffect());
        register("play_sound",             new PlaySoundEffect());
        register("screen_shake",           new ScreenShakeEffect());

        log.info("[SpellEffectRegistry] Registered " + registry.size() + " effect type(s) with Allied Target Filter Interceptors.");
    }

    private void register(String key, SpellEffect effect) {
        boolean isPositive = POSITIVE_SPELLS.contains(key.toLowerCase());

        SpellEffect filteredWrapper = (ctx) -> {
            Player caster = findCaster(ctx);
            Entity target = findTarget(ctx);

            if (caster != null && target != null) {
                if (!TargetFilter.shouldApplyEffect(caster, target, isPositive)) {
                    return; // Intercepted safely based on alliance rules!
                }
            }
            effect.execute(ctx);
        };

        registry.put(key.toLowerCase(), filteredWrapper);
    }

    // Dynamic extraction logic to avoid missing method compilation errors
    private Player findCaster(Object ctx) {
        for (String methodName : new String[]{"getPlayer", "getCaster", "getSender"}) {
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

    public SpellEffect get(String type) {
        return registry.get(type.toLowerCase());
    }

    public boolean has(String type) {
        return registry.containsKey(type.toLowerCase());
    }
}