package com.ahren.arpgessentialsx.weapons;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.classes.RPGClass;
import com.ahren.arpgessentialsx.data.PlayerData;
import com.ahren.arpgessentialsx.util.ColorUtil;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.RayTraceResult;

import java.util.List;
import java.util.UUID;

/**
 * Right-click → activate weapon skill.
 * Bows and Polearms require Shift + Right-click.
 *
 * Uses SkillCooldownTracker (shared with CooldownReductionPassive)
 * so passive cooldown reduction actually works.
 */
public final class WeaponSkillListener implements Listener {

    private final ARPGEssentialsX plugin;
    private final WeaponItemFactory factory;
    private final SkillCooldownTracker cooldownTracker;

    private static final double TARGET_RANGE = 20.0;

    public WeaponSkillListener(ARPGEssentialsX plugin,
                               WeaponItemFactory factory,
                               SkillCooldownTracker cooldownTracker) {
        this.plugin          = plugin;
        this.factory         = factory;
        this.cooldownTracker = cooldownTracker;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onSkillActivate(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;

        // Don't check cancelled - our own listeners cancel events and we need to process anyway
        // if (event.isCancelled()) return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();

        ItemStack held = player.getInventory().getItemInMainHand();
        String weaponId = factory.getWeaponId(held);
        if (weaponId == null) return;

        Weapon weapon = plugin.getWeaponManager().getWeapon(weaponId);
        if (weapon == null || !weapon.hasSkill()) return;

        // Bows and Polearms require Shift + Right-click
        if (weapon.getWeaponType() == WeaponType.BOW || weapon.getWeaponType() == WeaponType.POLEARM) {
            if (!player.isSneaking()) return;
        }

        event.setCancelled(true);

        UUID uuid = player.getUniqueId();

        // Resolve player class
        int classTag = resolveClassTag(player);

        // Check skill class tag restriction
        int skillClassTag = weapon.getSkillClassTag();
        if (skillClassTag > 0 && classTag != skillClassTag) {
            ColorUtil.sendActionBar(player,
                    "&cOnly " + classNameFor(skillClassTag) + "s can activate this skill.");
            return;
        }

        // Cooldown check via shared tracker
        if (cooldownTracker.isOnCooldown(uuid, weaponId)) {
            ColorUtil.sendActionBar(player,
                    "&c" + ColorUtil.translate(weapon.getSkillName()) + " &cis on cooldown!");
            return;
        }

        // Resolve look-at target
        LivingEntity target = getTargetEntity(player);

        // Dispatch all skill effects
        List<WeaponEffect> effects = weapon.getSkillEffects();
        List<org.bukkit.configuration.ConfigurationSection> configs = weapon.getSkillEffectConfigs();

        for (int i = 0; i < effects.size(); i++) {
            org.bukkit.configuration.ConfigurationSection cfg =
                    i < configs.size() ? configs.get(i) : null;

            WeaponEffectContext ctx = new WeaponEffectContext(
                    plugin, player, target,
                    player.getEyeLocation(), cfg, weapon, 0.0);
            try {
                effects.get(i).execute(ctx);
            } catch (Exception e) {
                plugin.getLogger().warning("[WeaponSkillListener] Skill effect " + i
                        + " of '" + weaponId + "' threw: " + e.getMessage());
            }
        }

        // Fire ON_EQUIP passives... actually ON_HIT makes no sense here.
        // Skills don't fire passives — passives are combat-triggered only.

        ColorUtil.sendActionBar(player,
                "&f" + ColorUtil.translate(weapon.getSkillName()) + " &7activated!");

        // Register cooldown via shared tracker
        cooldownTracker.startCooldown(uuid, weaponId, weapon.getSkillCooldown(), weapon);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private int resolveClassTag(Player player) {
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data == null || !data.hasClass()) return 0;
        RPGClass rpgClass = plugin.getClassManager().getClass(data.getClassId());
        return rpgClass != null ? rpgClass.getClassTag() : 0;
    }

    private LivingEntity getTargetEntity(Player player) {
        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                TARGET_RANGE,
                e -> e instanceof LivingEntity && !e.equals(player));
        if (result != null && result.getHitEntity() instanceof LivingEntity living) return living;
        return null;
    }

    private String classNameFor(int tag) {
        return switch (tag) {
            case 1 -> "Fighter";
            case 2 -> "Mage";
            case 3 -> "Marksman";
            case 4 -> "Assassin";
            case 5 -> "Tank";
            default -> "the correct class";
        };
    }
}