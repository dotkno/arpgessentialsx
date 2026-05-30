package com.ahren.arpgessentialsx.weapons.passives;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.weapons.SkillCooldownTracker;
import com.ahren.arpgessentialsx.weapons.Weapon;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;

/**
 * Carries everything a passive could need at trigger time.
 *
 * Trigger modes:
 *   ON_HIT          — player hit a living entity (target != null, event != null)
 *   ON_KILL         — player killed a living entity (target != null, event == null)
 *   ON_DAMAGE_TAKEN — player was hit by something (target == null, event != null)
 *   ALWAYS          — called passively (e.g. stat conversions) — target/event may be null
 *
 * Not all fields are populated in every mode. Check hasTarget() before using target.
 */
public final class WeaponPassiveContext {

    public enum Trigger { ON_HIT, ON_KILL, ON_DAMAGE_TAKEN, ALWAYS }

    private final ARPGEssentialsX plugin;
    private final Player player;
    private final LivingEntity target;          // null in ON_DAMAGE_TAKEN / ALWAYS
    private final EntityDamageByEntityEvent event; // null in ON_KILL / ALWAYS
    private final ConfigurationSection config;
    private final Weapon weapon;
    private final SkillCooldownTracker cooldownTracker;
    private final Trigger trigger;

    public WeaponPassiveContext(
            ARPGEssentialsX plugin,
            Player player,
            LivingEntity target,
            EntityDamageByEntityEvent event,
            ConfigurationSection config,
            Weapon weapon,
            SkillCooldownTracker cooldownTracker,
            Trigger trigger
    ) {
        this.plugin          = plugin;
        this.player          = player;
        this.target          = target;
        this.event           = event;
        this.config          = config;
        this.weapon          = weapon;
        this.cooldownTracker = cooldownTracker;
        this.trigger         = trigger;
    }

    public ARPGEssentialsX getPlugin()              { return plugin; }
    public Player getPlayer()                       { return player; }
    public LivingEntity getTarget()                 { return target; }
    public EntityDamageByEntityEvent getEvent()     { return event; }
    public ConfigurationSection getConfig()         { return config; }
    public Weapon getWeapon()                       { return weapon; }
    public SkillCooldownTracker getCooldownTracker(){ return cooldownTracker; }
    public Trigger getTrigger()                     { return trigger; }

    public boolean hasTarget() {
        return target != null && !target.isDead();
    }

    public boolean hasEvent() {
        return event != null;
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