package com.ahren.arpgessentialsx.armors.passives;

import com.ahren.arpgessentialsx.armors.passives.defensive.ArcaneDeflectionPassive;
import com.ahren.arpgessentialsx.armors.passives.defensive.FireImmunityPassive;
import com.ahren.arpgessentialsx.armors.passives.defensive.ManaShieldPassive;
import com.ahren.arpgessentialsx.armors.passives.defensive.SmokeBombPassive;
import com.ahren.arpgessentialsx.armors.passives.defensive.ThornsPassive;
import com.ahren.arpgessentialsx.armors.passives.defensive.UnyieldingFortressPassive;
import com.ahren.arpgessentialsx.armors.passives.offensive.BloodlettingPassive;
import com.ahren.arpgessentialsx.armors.passives.offensive.CleavingStrikesPassive;
import com.ahren.arpgessentialsx.armors.passives.offensive.PhantomStrikePassive;
import com.ahren.arpgessentialsx.armors.passives.offensive.PinDownPassive;
import com.ahren.arpgessentialsx.armors.passives.utility.PhantomCloakPassive;
import com.ahren.arpgessentialsx.armors.passives.utility.SnipersFocusPassive;
import com.ahren.arpgessentialsx.armors.passives.utility.SpeedBoostPassive;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.Player;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

/**
 * Maps passive type strings (from armors.yml) to ArmorPassive instances.
 *
 * Adding a new passive:
 *   1. Create a class in the appropriate sub-package implementing ArmorPassive
 *   2. Add one register() line here
 *   3. Use the key in armors.yml under passives: — done
 */
public final class ArmorPassiveRegistry {

    private final Map<String, ArmorPassive> registry = new HashMap<>();
    private final Logger log;

    public ArmorPassiveRegistry(Logger log) {
        this.log = log;
        registerAll();
    }

    private void registerAll() {
        // ── Utility ───────────────────────────────────────────────────────────
        register("speed_boost", new SpeedBoostPassive());
        register("snipers_focus", new SnipersFocusPassive());
        register("phantom_cloak", new PhantomCloakPassive());

        // ── Defensive ─────────────────────────────────────────────────────────
        register("fire_immunity", new FireImmunityPassive());
        register("arcane_deflection", new ArcaneDeflectionPassive());
        register("mana_shield", new ManaShieldPassive());
        register("smoke_bomb", new SmokeBombPassive());
        register("thorns", new ThornsPassive());
        register("unyielding_fortress", new UnyieldingFortressPassive());

        // ── Offensive ─────────────────────────────────────────────────────────
        register("cleaving_strikes", new CleavingStrikesPassive());
        register("bloodletting", new BloodlettingPassive());
        register("pin_down", new PinDownPassive());
        register("phantom_strike", new PhantomStrikePassive());

        log.info("[ArmorPassiveRegistry] Registered " + registry.size() + " passive type(s).");
    }

    private void register(String key, ArmorPassive passive) {
        registry.put(key.toLowerCase(), passive);
    }

    public ArmorPassive get(String type) {
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
            log.warning("[ArmorPassiveRegistry] Passive has no 'type' field");
            return;
        }

        ArmorPassive passive = get(type);
        if (passive == null) {
            log.warning("[ArmorPassiveRegistry] Unknown passive type: " + type);
            return;
        }

        try {
            passive.onEquip(player, config);
        } catch (Exception e) {
            log.warning("[ArmorPassiveRegistry] Failed to apply passive '" + type + "': " + e.getMessage());
        }
    }

    /**
     * Removes a passive from configuration (state-tracking).
     */
    public void removePassive(Player player, ConfigurationSection config) {
        String type = config.getString("type", "");
        if (type.isEmpty()) return;

        ArmorPassive passive = get(type);
        if (passive == null) return;

        try {
            passive.onUnequip(player, config);
        } catch (Exception e) {
            log.warning("[ArmorPassiveRegistry] Failed to remove passive '" + type + "': " + e.getMessage());
        }
    }
}
