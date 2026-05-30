package com.ahren.arpgessentialsx.weapons.passives;

import com.ahren.arpgessentialsx.weapons.passives.offensive.*;
import com.ahren.arpgessentialsx.weapons.passives.defensive.*;
import com.ahren.arpgessentialsx.weapons.passives.conversion.*;
import com.ahren.arpgessentialsx.weapons.passives.utility.*;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Maps passive type strings (from weapons.yml) to WeaponPassive instances.
 *
 * Adding a new passive:
 *   1. Create a class in the appropriate sub-package implementing WeaponPassive
 *   2. Add one register() line here
 *   3. Use the key in weapons.yml under passives: — done
 */
public final class WeaponPassiveRegistry {

    private final Map<String, WeaponPassive> registry = new HashMap<>();
    private final Logger log;

    public WeaponPassiveRegistry(Logger log) {
        this.log = log;
        registerAll();
    }

    private void registerAll() {
        // ── Offensive ─────────────────────────────────────────────────────────
        register("flat_damage",           new FlatDamagePassive());
        register("percent_damage",        new PercentDamagePassive());
        register("hp_threshold_damage",   new HpThresholdDamagePassive());
        register("execute",               new ExecutePassive());
        register("consecutive_hit",       new ConsecutiveHitPassive());
        register("debuff_exploiter",      new DebuffExploiterPassive());
        register("surrounded_bonus",      new SurroundedBonusPassive());
        register("ambush",                new AmbushPassive());

        // ── Defensive ─────────────────────────────────────────────────────────
        register("lifesteal",             new PassiveLifestealPassive());
        register("kill_heal",             new KillHealPassive());
        register("low_hp_shield",         new LowHpShieldPassive());

        // ── Stat conversion ───────────────────────────────────────────────────
        register("hp_to_damage",          new HpToDamagePassive());
        register("armor_to_damage",       new ArmorToDamagePassive());
        register("speed_to_damage",       new SpeedToDamagePassive());

        // ── Utility ───────────────────────────────────────────────────────────
        register("cooldown_reduction",    new CooldownReductionPassive());
        register("mana_on_kill",          new ManaOnKillPassive());
        register("random_buff",           new RandomBuffPassive());

        log.info("[WeaponPassiveRegistry] Registered " + registry.size() + " passive type(s).");
    }

    private void register(String key, WeaponPassive passive) {
        registry.put(key.toLowerCase(), passive);
    }

    public WeaponPassive get(String type) {
        return registry.get(type == null ? "" : type.toLowerCase());
    }

    public boolean has(String type) {
        return type != null && registry.containsKey(type.toLowerCase());
    }

    /**
     * Applies a passive from configuration (state-tracking).
     */
    public void applyPassive(Player player, ConfigurationSection config) {
        String type = config.getString("type", "");
        if (type.isEmpty()) {
            log.warning("[WeaponPassiveRegistry] Passive has no 'type' field");
            return;
        }

        WeaponPassive passive = get(type);
        if (passive == null) {
            log.warning("[WeaponPassiveRegistry] Unknown passive type: " + type);
            return;
        }

        try {
            passive.onEquip(player, config);
        } catch (Exception e) {
            log.warning("[WeaponPassiveRegistry] Failed to apply passive '" + type + "': " + e.getMessage());
        }
    }

    /**
     * Removes a passive from configuration (state-tracking).
     */
    public void removePassive(Player player, ConfigurationSection config) {
        String type = config.getString("type", "");
        if (type.isEmpty()) return;

        WeaponPassive passive = get(type);
        if (passive == null) return;

        try {
            passive.onUnequip(player, config);
        } catch (Exception e) {
            log.warning("[WeaponPassiveRegistry] Failed to remove passive '" + type + "': " + e.getMessage());
        }
    }
}