package com.ahren.arpgessentialsx.armors;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.armors.passives.ArmorPassive;
import com.ahren.arpgessentialsx.armors.passives.ArmorPassiveContext;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDamageEvent;

import java.util.Map;
import java.util.UUID;

/**
 * Listener that triggers armor passives based on damage events.
 * Handles ON_DAMAGE_TAKEN and ON_HIT triggers for armor passives.
 */
public final class ArmorPassiveListener implements Listener {

    private final ARPGEssentialsX plugin;
    private final ArmorEquipListener armorEquipListener;

    // Track last damage time for PhantomStrikePassive
    private final Map<UUID, Long> lastDamageTime;

    // Track reflection depth to prevent infinite loops
    private static final ThreadLocal<Integer> reflectionDepth = ThreadLocal.withInitial(() -> 0);
    private static final int MAX_REFLECTION_DEPTH = 3;

    public ArmorPassiveListener(ARPGEssentialsX plugin, ArmorEquipListener armorEquipListener, 
                               Map<UUID, Long> lastDamageTime) {
        this.plugin = plugin;
        this.armorEquipListener = armorEquipListener;
        this.lastDamageTime = lastDamageTime;
        
        // Initialize shared damage tracker for PhantomStrikePassive
        com.ahren.arpgessentialsx.armors.passives.offensive.PhantomStrikePassive.setDamageTracker(lastDamageTime);
    }

    /**
     * Triggers ON_DAMAGE_TAKEN armor passives when a player takes damage.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDamageTaken(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;

        Map<ArmorType, Armor> equippedArmor = armorEquipListener.getEquippedArmor(player);
        if (equippedArmor.isEmpty()) return;

        // Update damage tracking for PhantomStrikePassive
        if (event instanceof EntityDamageByEntityEvent) {
            lastDamageTime.put(player.getUniqueId(), System.currentTimeMillis());
        }

        // Trigger ON_DAMAGE_TAKEN passives for all equipped armor
        for (Armor armor : equippedArmor.values()) {
            for (ArmorPassive passive : armor.getPassives()) {
                if (passive.getTrigger() == ArmorPassive.Trigger.ON_DAMAGE_TAKEN) {
                    ArmorPassiveContext ctx = ArmorPassiveContext.forDamageTaken(player, armor, event);
                    try {
                        passive.apply(ctx);
                    } catch (Exception e) {
                        plugin.getLogger().warning("[ArmorPassiveListener] ON_DAMAGE_TAKEN passive '"
                                + passive.getClass().getSimpleName() + "' threw: " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Triggers ON_HIT armor passives when a player hits an entity.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerHit(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player player)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        Map<ArmorType, Armor> equippedArmor = armorEquipListener.getEquippedArmor(player);
        if (equippedArmor.isEmpty()) return;

        // Check reflection depth to prevent infinite loops
        int currentDepth = reflectionDepth.get();
        if (currentDepth >= MAX_REFLECTION_DEPTH) {
            return;
        }

        // Trigger ON_HIT passives for all equipped armor
        for (Armor armor : equippedArmor.values()) {
            for (ArmorPassive passive : armor.getPassives()) {
                if (passive.getTrigger() == ArmorPassive.Trigger.ON_HIT) {
                    reflectionDepth.set(currentDepth + 1);
                    try {
                        ArmorPassiveContext ctx = ArmorPassiveContext.forHit(player, armor, target, event);
                        passive.apply(ctx);
                    } catch (Exception e) {
                        plugin.getLogger().warning("[ArmorPassiveListener] ON_HIT passive '"
                                + passive.getClass().getSimpleName() + "' threw: " + e.getMessage());
                    } finally {
                        reflectionDepth.set(currentDepth);
                    }
                }
            }
        }
    }

    /**
     * Triggers ON_KILL armor passives when a player kills an entity.
     */
    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerKill(org.bukkit.event.entity.EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        Map<ArmorType, Armor> equippedArmor = armorEquipListener.getEquippedArmor(killer);
        if (equippedArmor.isEmpty()) return;

        // Trigger ON_KILL passives for all equipped armor
        for (Armor armor : equippedArmor.values()) {
            for (ArmorPassive passive : armor.getPassives()) {
                if (passive.getTrigger() == ArmorPassive.Trigger.ON_KILL) {
                    ArmorPassiveContext ctx = ArmorPassiveContext.forKill(killer, armor, event.getEntity());
                    try {
                        passive.apply(ctx);
                    } catch (Exception e) {
                        plugin.getLogger().warning("[ArmorPassiveListener] ON_KILL passive '"
                                + passive.getClass().getSimpleName() + "' threw: " + e.getMessage());
                    }
                }
            }
        }
    }

    /**
     * Clears player-specific data when they quit.
     */
    public void clearPlayer(UUID uuid) {
        lastDamageTime.remove(uuid);
    }
}
