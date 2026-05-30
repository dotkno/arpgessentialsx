package com.ahren.arpgessentialsx.armors.passives;

import com.ahren.arpgessentialsx.armors.Armor;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

/**
 * Context object passed to ArmorPassive.apply() methods.
 * Contains all relevant information about the trigger event.
 */
public class ArmorPassiveContext {

    private final Player player;
    private final Armor armor;
    private final ArmorPassive.Trigger trigger;

    // Event-specific data (may be null depending on trigger)
    private final EntityDamageEvent damageEvent;
    private final EntityDamageByEntityEvent damageByEntityEvent;
    private final Entity target;
    private final Entity attacker;

    private ArmorPassiveContext(Player player, Armor armor, ArmorPassive.Trigger trigger,
                                  EntityDamageEvent damageEvent,
                                  EntityDamageByEntityEvent damageByEntityEvent,
                                  Entity target, Entity attacker) {
        this.player = player;
        this.armor = armor;
        this.trigger = trigger;
        this.damageEvent = damageEvent;
        this.damageByEntityEvent = damageByEntityEvent;
        this.target = target;
        this.attacker = attacker;
    }

    public Player getPlayer() {
        return player;
    }

    public Armor getArmor() {
        return armor;
    }

    public ArmorPassive.Trigger getTrigger() {
        return trigger;
    }

    public EntityDamageEvent getDamageEvent() {
        return damageEvent;
    }

    public EntityDamageByEntityEvent getDamageByEntityEvent() {
        return damageByEntityEvent;
    }

    public Entity getTarget() {
        return target;
    }

    public Entity getAttacker() {
        return attacker;
    }

    // ── Builder methods for creating context for different triggers ───────────

    public static ArmorPassiveContext forEquip(Player player, Armor armor) {
        return new ArmorPassiveContext(player, armor, ArmorPassive.Trigger.ON_EQUIP,
                null, null, null, null);
    }

    public static ArmorPassiveContext forUnequip(Player player, Armor armor) {
        return new ArmorPassiveContext(player, armor, ArmorPassive.Trigger.ON_UNEQUIP,
                null, null, null, null);
    }

    public static ArmorPassiveContext forDamageTaken(Player player, Armor armor,
                                                       EntityDamageEvent damageEvent) {
        return new ArmorPassiveContext(player, armor, ArmorPassive.Trigger.ON_DAMAGE_TAKEN,
                damageEvent, null, null, null);
    }

    public static ArmorPassiveContext forHit(Player player, Armor armor,
                                              Entity target, EntityDamageByEntityEvent event) {
        return new ArmorPassiveContext(player, armor, ArmorPassive.Trigger.ON_HIT,
                null, event, target, null);
    }

    public static ArmorPassiveContext forKill(Player player, Armor armor, Entity target) {
        return new ArmorPassiveContext(player, armor, ArmorPassive.Trigger.ON_KILL,
                null, null, target, null);
    }

    public static ArmorPassiveContext forTick(Player player, Armor armor) {
        return new ArmorPassiveContext(player, armor, ArmorPassive.Trigger.ON_TICK,
                null, null, null, null);
    }
}
