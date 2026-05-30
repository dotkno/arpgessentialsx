package com.ahren.arpgessentialsx.armors.passives.utility;

import com.ahren.arpgessentialsx.armors.passives.ArmorPassive;
import com.ahren.arpgessentialsx.armors.passives.ArmorPassiveContext;

/**
 * Passive that reduces aggro/threat generation.
 *
 * yml params:
 *   aggro_reduction_percentage: 0.30   (30% less aggro)
 *
 * Trigger: ON_EQUIP
 */
public final class PhantomCloakPassive implements ArmorPassive {

    @Override
    public Trigger getTrigger() {
        return Trigger.ON_EQUIP;
    }

    @Override
    public void apply(ArmorPassiveContext ctx) {
        // Aggro reduction would be implemented here
        // This requires integration with a threat/aggro system
        // For now, this is a placeholder
    }
}
