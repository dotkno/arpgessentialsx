package com.ahren.arpgessentialsx.relics;

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
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;

import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles relic activation on right-click.
 *
 * Flow:
 *   1. Detect right-click with a relic item
 *   2. Class restriction check
 *   3. Cooldown check
 *   4. Dispatch all effects simultaneously via registry
 *   5. Consume one use (destroy item at 0)
 *   6. Start cooldown timer
 */
public final class RelicCastListener implements Listener {

    private final ARPGEssentialsX plugin;
    private final RelicItemFactory factory;

    /** Cooldown tracking: "uuid-relicId" → active cooldown task */
    private final Map<String, BukkitRunnable> activeCooldowns = new ConcurrentHashMap<>();

    /** Target ray trace range in blocks */
    private static final double TARGET_RANGE = 20.0;

    public RelicCastListener(ARPGEssentialsX plugin, RelicItemFactory factory) {
        this.plugin = plugin;
        this.factory = factory;
    }

    @EventHandler(priority = EventPriority.NORMAL)
    public void onRelicUse(PlayerInteractEvent event) {
        // ── Offhand double-fire guard ─────────────────────────────────────────
        // Bukkit fires this event for both hands. We only want main-hand.
        if (event.getHand() != EquipmentSlot.HAND) return;

        // Don't check cancelled - our own listeners cancel events and we need to process anyway
        // if (event.isCancelled()) return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item.getItemMeta() == null) return;
        if (!item.getItemMeta().getPersistentDataContainer()
                .has(factory.getRelicIdKey(), PersistentDataType.STRING)) return;

        event.setCancelled(true);

        String relicId = item.getItemMeta().getPersistentDataContainer()
                .get(factory.getRelicIdKey(), PersistentDataType.STRING);
        Relic relic = plugin.getRelicManager().getRelic(relicId);
        if (relic == null) return;

        UUID uuid = player.getUniqueId();
        String cooldownKey = uuid + "-" + relicId;

        // ── Class restriction ────────────────────────────────────────────────
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        if (data == null || !data.hasClass()) {
            ColorUtil.sendActionBar(player, "&cYou must have a class to use relics!");
            return;
        }

        RPGClass rpgClass = plugin.getClassManager().getClass(data.getClassId());
        if (rpgClass == null || rpgClass.getClassTag() != relic.getClassTag()) {
            ColorUtil.sendActionBar(player, "&cOnly " + classNameFor(relic.getClassTag()) + "s can use this!");
            return;
        }

        // ── Cooldown check ───────────────────────────────────────────────────
        if (activeCooldowns.containsKey(cooldownKey)) {
            ColorUtil.sendActionBar(player, "&c" + relic.getDisplayName() + " &cis on cooldown!");
            return;
        }

        // ── Resolve look-at target ───────────────────────────────────────────
        LivingEntity target = getTargetEntity(player);

        // ── Dispatch all effects simultaneously ──────────────────────────────
        List<RelicEffect> effects = relic.getEffects();
        List<org.bukkit.configuration.ConfigurationSection> effectConfigs = relic.getEffectConfigs();

        for (int i = 0; i < effects.size(); i++) {
            org.bukkit.configuration.ConfigurationSection cfg =
                    i < effectConfigs.size() ? effectConfigs.get(i) : null;

            RelicEffectContext ctx = new RelicEffectContext(
                    plugin,
                    player,
                    player.getEyeLocation(),
                    target,
                    cfg,
                    relic
            );

            try {
                effects.get(i).execute(ctx);
            } catch (Exception e) {
                plugin.getLogger().warning("[RelicCastListener] Effect " + i
                        + " of relic '" + relicId + "' threw an exception: " + e.getMessage());
                e.printStackTrace();
            }
        }

        // ── Consume one use ──────────────────────────────────────────────────
        boolean destroyed = factory.consumeUse(item, relic, player);
        if (destroyed) return; // Item gone — don't start cooldown on a destroyed item

        // ── Start cooldown ───────────────────────────────────────────────────
        startCooldown(player, cooldownKey, relic);
    }

    // ── Helpers ───────────────────────────────────────────────────────────────

    private void startCooldown(Player player, String cooldownKey, Relic relic) {
        // Start boss bar cooldown
        plugin.getBossBarCooldownManager().startCooldown(player, relic.getDisplayName(), relic.getCooldown());

        BukkitRunnable task = new BukkitRunnable() {
            @Override
            public void run() {
                activeCooldowns.remove(cooldownKey);
                if (player.isOnline()) {
                    ColorUtil.sendActionBar(player, "&a" + relic.getDisplayName() + " &ais ready!");
                }
            }
        };
        activeCooldowns.put(cooldownKey, task);
        task.runTaskLater(plugin, (long)(relic.getCooldown() * 20));
    }

    private LivingEntity getTargetEntity(Player player) {
        RayTraceResult result = player.getWorld().rayTraceEntities(
                player.getEyeLocation(),
                player.getEyeLocation().getDirection(),
                TARGET_RANGE,
                entity -> entity instanceof LivingEntity && !entity.equals(player)
        );
        if (result != null && result.getHitEntity() instanceof LivingEntity living) {
            return living;
        }
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