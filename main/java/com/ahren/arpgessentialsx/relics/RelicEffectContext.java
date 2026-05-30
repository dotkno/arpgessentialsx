package com.ahren.arpgessentialsx.relics;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;

/**
 * Carries everything a relic effect could need at execution time.
 *
 * Mirrors SpellEffectContext exactly — same pattern, same convenience helpers.
 *
 * Fields that may be null:
 *   - lookedAtTarget: null if caster isn't looking at any entity within range
 *   - config: null if the effect section has no extra parameters
 */
public final class RelicEffectContext {

    private final ARPGEssentialsX plugin;
    private final Player caster;
    private final Location castLocation;
    private final LivingEntity lookedAtTarget;
    private final ConfigurationSection config;
    private final Relic relic;

    public RelicEffectContext(
            ARPGEssentialsX plugin,
            Player caster,
            Location castLocation,
            LivingEntity lookedAtTarget,
            ConfigurationSection config,
            Relic relic
    ) {
        this.plugin = plugin;
        this.caster = caster;
        this.castLocation = castLocation;
        this.lookedAtTarget = lookedAtTarget;
        this.config = config;
        this.relic = relic;
    }

    public ARPGEssentialsX getPlugin()          { return plugin; }
    public Player getCaster()                    { return caster; }
    public Location getCastLocation()            { return castLocation; }
    public LivingEntity getLookedAtTarget()      { return lookedAtTarget; }
    public ConfigurationSection getConfig()      { return config; }
    public Relic getRelic()                      { return relic; }

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