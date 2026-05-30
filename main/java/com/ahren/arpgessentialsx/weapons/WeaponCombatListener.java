package com.ahren.arpgessentialsx.weapons;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.classes.RPGClass;
import com.ahren.arpgessentialsx.data.PlayerData;
import com.ahren.arpgessentialsx.util.ColorUtil;
import com.ahren.arpgessentialsx.util.TargetFilter;
import com.ahren.arpgessentialsx.weapons.passives.WeaponPassive;
import com.ahren.arpgessentialsx.weapons.passives.WeaponPassiveContext;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.attribute.AttributeModifier;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.entity.EntityShootBowEvent;
import org.bukkit.inventory.EquipmentSlotGroup;
import org.bukkit.inventory.ItemStack;

import java.util.ArrayList;
import java.util.List;

public final class WeaponCombatListener implements Listener {

    private final ARPGEssentialsX plugin;
    private final WeaponItemFactory factory;
    private final SkillCooldownTracker cooldownTracker;

    private static final String SPEED_PENALTY_KEY = "arpg_off_class_speed";

    public WeaponCombatListener(ARPGEssentialsX plugin, WeaponItemFactory factory,
                                SkillCooldownTracker cooldownTracker) {
        this.plugin          = plugin;
        this.factory         = factory;
        this.cooldownTracker = cooldownTracker;
    }

    // ── ON_HIT ────────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.NORMAL, ignoreCancelled = true)
    public void onPlayerAttack(EntityDamageByEntityEvent event) {
        if (!(event.getDamager() instanceof Player attacker)) return;
        if (!(event.getEntity() instanceof LivingEntity target)) return;

        // ── PARTY FILTER INTERCEPT OVERRIDE ──
        // Ensure no offensive weapon passives, bleeding stats, or custom damage flows target allies or pets
        if (!TargetFilter.shouldApplyEffect(attacker, target, false)) {
            event.setCancelled(true);
            target.setVelocity(target.getVelocity().multiply(0)); // Kill residual momentum ticks
            return;
        }

        ItemStack held = attacker.getInventory().getItemInMainHand();
        String weaponId = factory.getWeaponId(held);
        if (weaponId == null) return;

        Weapon weapon = plugin.getWeaponManager().getWeapon(weaponId);
        if (weapon == null) return;

        int classTag = resolveClassTag(attacker);

        // Civilian (class 0) - not trained to use any weapon
        if (classTag == 0) {
            event.setDamage(event.getDamage() * 0.5); // -50% damage
        }

        // Catalyst: 0 damage for non-Mages
        if (weapon.getWeaponType() == WeaponType.CATALYST && classTag != 2) {
            event.setDamage(0);
            event.setCancelled(true);
            ColorUtil.sendActionBar(attacker, "&cYou have no idea how to use this.");
            return;
        }

        // Off-class proficiency damage penalty (excluding civilian which is handled above)
        if (classTag != 0 && !weapon.isNaturalFor(classTag)) {
            event.setDamage(event.getDamage() * weapon.getDamageMultiplierFor(classTag));
        }

        // On-hit effects
        // Swing cooldown gate — only fire effects and passives on a fully charged swing.
        if (attacker.getAttackCooldown() < 1.0f) return;
        double currentDamage = event.getDamage();
        List<WeaponEffect> effects = weapon.getOnHitEffects();
        List<org.bukkit.configuration.ConfigurationSection> configs = weapon.getOnHitEffectConfigs();

        for (int i = 0; i < effects.size(); i++) {
            org.bukkit.configuration.ConfigurationSection cfg =
                    i < configs.size() ? configs.get(i) : null;
            WeaponEffectContext ctx = new WeaponEffectContext(
                    plugin, attacker, target, attacker.getLocation(), cfg, weapon, currentDamage, event);
            try {
                effects.get(i).execute(ctx);
            } catch (Exception e) {
                plugin.getLogger().warning("[WeaponCombatListener] on_hit effect " + i
                        + " of '" + weaponId + "' threw: " + e.getMessage());
            }
        }

        firePassives(weapon, attacker, target, event, WeaponPassiveContext.Trigger.ON_HIT);

        // Reduce custom durability on hit
        if (factory.hasCustomDurability(held)) {
            boolean broke = factory.reduceDurability(held, 1);
            if (broke) {
                attacker.getInventory().setItemInMainHand(null);
                attacker.sendMessage("§cYour weapon broke!");
            }
        }
    }

    // ── ON_KILL ───────────────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.MONITOR, ignoreCancelled = true)
    public void onEntityDeath(EntityDeathEvent event) {
        Player killer = event.getEntity().getKiller();
        if (killer == null) return;

        ItemStack held = killer.getInventory().getItemInMainHand();
        String weaponId = factory.getWeaponId(held);
        if (weaponId == null) return;

        Weapon weapon = plugin.getWeaponManager().getWeapon(weaponId);
        if (weapon == null) return;

        firePassives(weapon, killer, null, null, WeaponPassiveContext.Trigger.ON_KILL);
    }

    // ── ARROW VELOCITY SCALING ───────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onArrowShoot(EntityShootBowEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        if (!(event.getProjectile() instanceof org.bukkit.entity.Projectile projectile)) return;

        ItemStack held = player.getInventory().getItemInMainHand();
        String weaponId = factory.getWeaponId(held);
        if (weaponId == null) return;

        Weapon weapon = plugin.getWeaponManager().getWeapon(weaponId);
        if (weapon == null) return;

        // Only apply velocity scaling to bows
        if (weapon.getWeaponType() != WeaponType.BOW) return;

        // Scale arrow velocity based on star tier
        double velocityMultiplier = switch (weapon.getStars()) {
            case 1 -> 1.0;  // Normal velocity
            case 2 -> 1.1;  // +10% velocity
            case 3 -> 1.2;  // +20% velocity
            case 4 -> 1.3;  // +30% velocity
            case 5 -> 1.4;  // +40% velocity
            default -> 1.0;
        };

        if (velocityMultiplier != 1.0) {
            projectile.setVelocity(projectile.getVelocity().multiply(velocityMultiplier));
        }
    }

    // ── ON_DAMAGE_TAKEN ───────────────────────────────────────────────────────

    @EventHandler(priority = EventPriority.HIGH, ignoreCancelled = true)
    public void onPlayerDamageTaken(EntityDamageByEntityEvent event) {
        if (!(event.getEntity() instanceof Player victim)) return;

        ItemStack held = victim.getInventory().getItemInMainHand();
        String weaponId = factory.getWeaponId(held);
        if (weaponId == null) return;

        Weapon weapon = plugin.getWeaponManager().getWeapon(weaponId);
        if (weapon == null) return;

        firePassives(weapon, victim, null, event, WeaponPassiveContext.Trigger.ON_DAMAGE_TAKEN);
    }

    // ── Passive dispatch ──────────────────────────────────────────────────────

    private void firePassives(Weapon weapon, Player player, LivingEntity target,
                              EntityDamageByEntityEvent event,
                              WeaponPassiveContext.Trigger trigger) {

        List<WeaponPassive> passives = weapon.getPassives();
        List<org.bukkit.configuration.ConfigurationSection> configs = weapon.getPassiveConfigs();

        for (int i = 0; i < passives.size(); i++) {
            // Safety evaluation bypass block
            if (target != null && !TargetFilter.shouldApplyEffect(player, target, false)) {
                continue; // Do not distribute weapon tracking procs onto friends
            }

            org.bukkit.configuration.ConfigurationSection cfg =
                    i < configs.size() ? configs.get(i) : null;

            WeaponPassiveContext ctx = new WeaponPassiveContext(
                    plugin, player, target, event, cfg, weapon, cooldownTracker, trigger);
            try {
                passives.get(i).apply(ctx);
            } catch (Exception e) {
                plugin.getLogger().warning("[WeaponCombatListener] passive " + i
                        + " of '" + weapon.getId() + "' (" + trigger + ") threw: "
                        + e.getMessage());
            }
        }
    }

    // ── Speed penalty ─────────────────────────────────────────────────────────

    public void applySpeedPenalty(Player player, double penaltyValue) {
        AttributeInstance inst = player.getAttribute(Attribute.GENERIC_ATTACK_SPEED);
        if (inst == null) return;

        NamespacedKey key = new NamespacedKey(plugin, SPEED_PENALTY_KEY);

        List<AttributeModifier> toRemove = new ArrayList<>();
        for (AttributeModifier mod : inst.getModifiers()) {
            if (mod.getKey().equals(key)) toRemove.add(mod);
        }
        toRemove.forEach(inst::removeModifier);

        if (penaltyValue != 0.0) {
            // Apply as a direct reduction to make it very noticeable
            // penaltyValue is the percentage (e.g., -0.5 means reduce by 50% of base value)
            double baseValue = inst.getBaseValue();
            double reduction = baseValue * Math.abs(penaltyValue);
            inst.addModifier(new AttributeModifier(
                    key, -reduction,
                    AttributeModifier.Operation.ADD_NUMBER,
                    EquipmentSlotGroup.MAINHAND));
        }
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int resolveClassTag(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data == null || !data.hasClass()) return 0;
        RPGClass rpgClass = plugin.getClassManager().getClass(data.getClassId());
        return rpgClass != null ? rpgClass.getClassTag() : 0;
    }
}