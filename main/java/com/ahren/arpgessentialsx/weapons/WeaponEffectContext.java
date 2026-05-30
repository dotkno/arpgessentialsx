package com.ahren.arpgessentialsx.weapons;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import org.bukkit.Location;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Carries everything a weapon effect could need at execution time.
 *
 * Two usage modes:
 *
 * ON-HIT mode (effects_on_hit):
 *   - attacker = the player who swung
 *   - target   = the entity that was hit (never null in this mode)
 *   - damage   = the raw damage of the hit (for lifesteal calculations)
 *   - event    = the EntityDamageByEntityEvent (null in skill mode)
 *
 * SKILL mode (skill effects):
 *   - attacker = the player who activated the skill
 *   - target   = whoever they're looking at (may be null)
 *   - damage   = 0 (not a hit)
 *   - event    = null
 */
public final class WeaponEffectContext {

    private final ARPGEssentialsX plugin;
    private final Player attacker;
    private final LivingEntity target;
    private final Location castLocation;
    private final ConfigurationSection config;
    private final Weapon weapon;
    private final double damage;
    private final EntityDamageByEntityEvent event;

    public WeaponEffectContext(
            ARPGEssentialsX plugin,
            Player attacker,
            LivingEntity target,
            Location castLocation,
            ConfigurationSection config,
            Weapon weapon,
            double damage
    ) {
        this.plugin        = plugin;
        this.attacker      = attacker;
        this.target        = target;
        this.castLocation  = castLocation;
        this.config        = config;
        this.weapon        = weapon;
        this.damage        = damage;
        this.event         = null;
    }

    public WeaponEffectContext(
            ARPGEssentialsX plugin,
            Player attacker,
            LivingEntity target,
            Location castLocation,
            ConfigurationSection config,
            Weapon weapon,
            double damage,
            EntityDamageByEntityEvent event
    ) {
        this.plugin        = plugin;
        this.attacker      = attacker;
        this.target        = target;
        this.castLocation  = castLocation;
        this.config        = config;
        this.weapon        = weapon;
        this.damage        = damage;
        this.event         = event;
    }

    public ARPGEssentialsX getPlugin()         { return plugin; }
    public Player getAttacker()                { return attacker; }
    public LivingEntity getTarget()            { return target; }
    public Location getCastLocation()          { return castLocation; }
    public ConfigurationSection getConfig()    { return config; }
    public Weapon getWeapon()                  { return weapon; }
    public double getDamage()                  { return damage; }
    public EntityDamageByEntityEvent getEvent() { return event; }

    public boolean hasTarget() {
        return target != null && !target.isDead();
    }

    // ── Config convenience helpers ────────────────────────────────────────────

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