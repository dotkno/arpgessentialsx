package com.ahren.arpgessentialsx.armors.setbonus;

import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Registry for armor set bonus types.
 * Similar to WeaponEffectRegistry but for set bonuses.
 */
public class ArmorSetBonusRegistry {

    private final Logger logger;
    private final Map<String, ArmorSetBonus> bonuses = new HashMap<>();

    public ArmorSetBonusRegistry(Logger logger) {
        this.logger = logger;
        registerDefaults();
    }

    private void registerDefaults() {
        register("stat_boost", new StatBoostBonus());
        register("damage_reduction", new DamageReductionBonus());
        register("percentage_stat_boost", new PercentageStatBoostBonus());
        register("conditional_attack_speed", new ConditionalAttackSpeedBonus());
        register("cooldown_reduction_on_kill", new CooldownReductionOnKillBonus());
        register("first_hit_damage", new FirstHitDamageBonus());
        register("health_regen", new HealthRegenBonus());
        
        // Genshin-inspired set bonuses
        register("momentum_stacks", new MomentumStacksBonus());
        register("spell_damage_amplification", new SpellDamageAmplificationBonus());
        register("magic_vulnerability", new MagicVulnerabilityBonus());
        register("charged_shot", new ChargedShotBonus());
        register("crit_rate_on_charged", new CritRateOnChargedBonus());
        register("mana_cost_reduction", new ManaCostReductionBonus());
        register("damage_to_full_health", new DamageToFullHealthBonus());
        register("shadow_stacks", new ShadowStacksBonus());
        register("backstab_ignore_armor", new BackstabIgnoreArmorBonus());
        register("fortification_stacks", new FortificationStacksBonus());
        register("taunt_and_shield", new TauntAndShieldBonus());
    }

    public void register(String type, ArmorSetBonus bonus) {
        bonuses.put(type.toLowerCase(), bonus);
        logger.info("[ArmorSetBonusRegistry] Registered set bonus type: " + type);
    }

    public ArmorSetBonus get(String type) {
        return bonuses.get(type.toLowerCase());
    }

    /**
     * Applies a set bonus from configuration.
     */
    public void applyBonus(Player player, ConfigurationSection config, int pieces, String setName) {
        String type = config.getString("type", "");
        if (type.isEmpty()) {
            logger.warning("[ArmorSetBonusRegistry] Set bonus has no 'type' field");
            return;
        }

        ArmorSetBonus bonus = get(type);
        if (bonus == null) {
            logger.warning("[ArmorSetBonusRegistry] Unknown set bonus type: " + type);
            return;
        }

        try {
            bonus.apply(player, config, pieces, setName);
        } catch (Exception e) {
            logger.warning("[ArmorSetBonusRegistry] Failed to apply set bonus '" + type + "': " + e.getMessage());
        }
    }

    /**
     * Removes a set bonus from configuration.
     */
    public void removeBonus(Player player, ConfigurationSection config, int pieces, String setName) {
        String type = config.getString("type", "");
        if (type.isEmpty()) return;

        ArmorSetBonus bonus = get(type);
        if (bonus == null) return;

        try {
            bonus.remove(player, config, pieces, setName);
        } catch (Exception e) {
            logger.warning("[ArmorSetBonusRegistry] Failed to remove set bonus '" + type + "': " + e.getMessage());
        }
    }
}
