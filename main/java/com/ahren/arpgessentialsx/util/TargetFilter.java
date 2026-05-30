package com.ahren.arpgessentialsx.util;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import org.bukkit.entity.AnimalTamer;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Tameable;
import java.util.UUID;

public final class TargetFilter {

    /**
     * Determines if an entity should be affected by an active system effect based on source party status.
     *
     * @param source     The player executing the ability (caster, attacker, user).
     * @param target     The entity that may potentially be affected.
     * @param isPositive True if the effect is a buff/heal/support, False if it is damage/knockback/debuff.
     * @return true if the execution mechanics should proceed on this target; false if it should skip it.
     */
    public static boolean shouldApplyEffect(Player source, Entity target, boolean isPositive) {
        // Self-targeting evaluation rules
        if (target.equals(source)) {
            return isPositive; // Positive mechanics hit yourself; negative ones get dropped.
        }

        boolean isAlly = false;

        // Check 1: Target is an allied player
        if (target instanceof Player targetPlayer) {
            isAlly = ARPGEssentialsX.getInstance().getPartyManager()
                    .inSameParty(source.getUniqueId(), targetPlayer.getUniqueId());
        }
        // Check 2: Target is an allied tamed pet (Wolf, Cat, Horse, Parrot, etc.)
        else if (target instanceof Tameable tameable) {
            if (tameable.isTamed() && tameable.getOwner() != null) {
                AnimalTamer owner = tameable.getOwner();
                UUID ownerUUID = owner.getUniqueId();

                // Check if the pet belongs directly to the source caster
                if (ownerUUID.equals(source.getUniqueId())) {
                    isAlly = true;
                } else {
                    // Check if the pet owner is online and in the caster's party
                    isAlly = ARPGEssentialsX.getInstance().getPartyManager()
                            .inSameParty(source.getUniqueId(), ownerUUID);
                }
            }
        }

        // Resolution Matrix
        if (isAlly) {
            return isPositive;  // Allies ONLY receive helpful properties.
        } else {
            return !isPositive; // Enemies/neutral entities ONLY receive harmful properties.
        }
    }
}