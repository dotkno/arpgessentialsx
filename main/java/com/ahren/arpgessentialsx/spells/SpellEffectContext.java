package com.ahren.arpgessentialsx.spells;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.weapons.CatalystMultiplier;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;

/**
 * Carries everything a spell effect could need at execution time.
 *
 * One instance is created per spell cast and SHARED across all effects in the
 * chain. SpellCastManager calls swapConfig() before each effect so that
 * ctx.getString/getInt/getDouble/getBoolean read from the right effect's config
 * section, while fields like lastLaunchedProjectile persist across the whole chain.
 *
 * NEW fields:
 *   - catalystMultiplier: read from the Mage's offhand catalyst (NONE if bare-handed)
 *   - healingMultiplier:  global healing amplifier (future: talent tree, set bonuses)
 *
 * Usage in spell effects:
 *   double damage = ctx.catalyst().scaleDamage(ctx.getDouble("damage", 5.0));
 *   double radius = ctx.catalyst().scaleRadius(ctx.getDouble("radius", 3.0));
 *   int    ticks  = (int)(ctx.catalyst().scaleDuration(ctx.getDouble("duration", 5.0)) * 20);
 */
public final class SpellEffectContext {

    private final ARPGEssentialsX plugin;
    private final Player caster;
    private final Location castLocation;
    private final LivingEntity lookedAtTarget;
    private final Spell spell;

    /** Swapped per-effect by SpellCastManager before each execute() call. */
    private ConfigurationSection config;

    /** Multipliers from the equipped catalyst. Never null — falls back to CatalystMultiplier.NONE. */
    private final CatalystMultiplier catalystMultiplier;

    /**
     * Global healing multiplier. 1.0 = no bonus.
     * Placeholder for future talent / set-bonus systems.
     */
    private final double healingMultiplier;

    /** Set by launch effects so trail effects can follow the projectile. */
    private Projectile lastLaunchedProjectile = null;

    /**
     * Full constructor — used by SpellCastManager which reads the catalyst from offhand.
     */
    public SpellEffectContext(
            ARPGEssentialsX plugin,
            Player caster,
            Location castLocation,
            LivingEntity lookedAtTarget,
            ConfigurationSection config,
            Spell spell,
            CatalystMultiplier catalystMultiplier,
            double healingMultiplier
    ) {
        this.plugin             = plugin;
        this.caster             = caster;
        this.castLocation       = castLocation;
        this.lookedAtTarget     = lookedAtTarget;
        this.config             = config;
        this.spell              = spell;
        this.catalystMultiplier = catalystMultiplier != null ? catalystMultiplier : CatalystMultiplier.NONE;
        this.healingMultiplier  = healingMultiplier;
    }

    /**
     * Convenience constructor — no catalyst, no healing bonus.
     * Keeps existing call sites that don't need catalyst scaling compiling without changes.
     */
    public SpellEffectContext(
            ARPGEssentialsX plugin,
            Player caster,
            Location castLocation,
            LivingEntity lookedAtTarget,
            ConfigurationSection config,
            Spell spell
    ) {
        this(plugin, caster, castLocation, lookedAtTarget, config, spell, CatalystMultiplier.NONE, 1.0);
    }

    /**
     * Replaces the active config section with the one for the next effect.
     * Called by SpellCastManager before each effect's execute().
     * Catalyst and healing multipliers are cast-wide — they don't swap.
     */
    public void swapConfig(ConfigurationSection newConfig) {
        this.config = newConfig;
    }

    // ── Accessors ─────────────────────────────────────────────────────────────

    public ARPGEssentialsX getPlugin()      { return plugin; }
    public Player getCaster()               { return caster; }
    public Location getCastLocation()       { return castLocation; }
    public LivingEntity getLookedAtTarget() { return lookedAtTarget; }
    public ConfigurationSection getConfig() { return config; }
    public Spell getSpell()                 { return spell; }

    /** The catalyst multiplier active for this cast. Never null. */
    public CatalystMultiplier catalyst()    { return catalystMultiplier; }

    /** Healing multiplier for this cast (1.0 = no bonus). */
    public double getHealingMultiplier()    { return healingMultiplier; }

    public void setLastLaunchedProjectile(Projectile p) { this.lastLaunchedProjectile = p; }
    public Projectile getLastLaunchedProjectile()       { return lastLaunchedProjectile; }

    // ── Config convenience helpers ────────────────────────────────────────────

    public boolean hasTarget() {
        return lookedAtTarget != null && !lookedAtTarget.isDead();
    }

    public double getDouble(String key, double def) {
        return config != null ? config.getDouble(key, def) : def;
    }

    public int getInt(String key, int def) {
        return config != null ? config.getInt(key, def) : def;
    }

    public String getString(String key, String def) {
        return config != null ? config.getString(key, def) : def;
    }

    public boolean getBoolean(String key, boolean def) {
        return config != null ? config.getBoolean(key, def) : def;
    }
}