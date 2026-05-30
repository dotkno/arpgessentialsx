package com.ahren.arpgessentialsx.spells;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.data.PlayerData;
import com.ahren.arpgessentialsx.weapons.CatalystMultiplier;
import com.ahren.arpgessentialsx.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitRunnable;
import org.bukkit.util.RayTraceResult;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

import static com.ahren.arpgessentialsx.util.ColorUtil.sendActionBar;

public final class SpellCastManager {

    private final ARPGEssentialsX plugin;
    private final SpellBookFactory bookFactory;

    private final Map<String, BukkitRunnable> activeChargeTasks = new HashMap<>();
    private final Map<String, Boolean> fullyChargedSpells = new ConcurrentHashMap<>();
    private final Map<String, Boolean> silencedPlayers = new ConcurrentHashMap<>();
    private final Map<String, BukkitRunnable> cooldownTasks = new HashMap<>();

    private final Map<UUID, Integer> preCastLevels = new HashMap<>();
    private final Map<UUID, BukkitRunnable> regenTasks = new HashMap<>();

    private static final double TARGET_RANGE = 20.0;

    public SpellCastManager(ARPGEssentialsX plugin, SpellBookFactory bookFactory) {
        this.plugin      = plugin;
        this.bookFactory = bookFactory;
    }

    private String getKey(UUID uuid, String spellId) {
        return uuid + "-" + spellId;
    }

    public boolean isCharging(UUID uuid, String spellId) {
        String key = getKey(uuid, spellId);
        return activeChargeTasks.containsKey(key) || fullyChargedSpells.containsKey(key);
    }

    public void setSilenced(UUID uuid, boolean silenced) {
        if (silenced) silencedPlayers.put(uuid.toString(), true);
        else silencedPlayers.remove(uuid.toString());
    }

    public boolean isSilenced(UUID uuid) {
        return silencedPlayers.containsKey(uuid.toString());
    }

    // ── Charge Phase ──────────────────────────────────────────────────────────

    public void startCharge(Player player, Spell spell) {
        UUID uuid    = player.getUniqueId();
        String spellId = spell.getId();
        String key   = getKey(uuid, spellId);

        if (isCharging(uuid, spellId) || cooldownTasks.containsKey(key)) return;

        PlayerData data = plugin.getPlayerDataManager().getPlayerData(uuid);
        if (data == null || !data.hasClass() || !data.getClassId().equalsIgnoreCase("mage")) {
            sendActionBar(player, "&cOnly Mages can cast spells!");
            return;
        }

        if (isSilenced(uuid)) {
            sendActionBar(player, "&cYou are silenced!");
            return;
        }

        int manaCost = (int) spell.getManaCost();
        
        // Apply mana cost reduction bonus (Mage 4-piece set bonus)
        if (com.ahren.arpgessentialsx.armors.setbonus.ManaCostReductionBonus.isActive(uuid)) {
            double reduction = com.ahren.arpgessentialsx.armors.setbonus.ManaCostReductionBonus.getManaCostReduction(uuid);
            manaCost = (int) Math.max(1, manaCost * (1.0 - reduction));
        }
        
        if (player.getLevel() < manaCost) {
            sendActionBar(player, "&cNot enough mana! Need &f" + manaCost + " &clevels.");
            return;
        }

        // FIXED: Changed to getOffhandCatalystMultiplier to read the amplifier from the offhand slot
        CatalystMultiplier catalyst = plugin.getWeaponManager()
                .getItemFactory().getOffhandCatalystMultiplier(player);

        // Apply catalyst charge time reduction (values < 1.0 = faster)
        double chargeSeconds = spell.getChargeTime() * catalyst.getChargeTimeMultiplier();

        sendActionBar(player, "&7Charging " + spell.getDisplayName() + "...");

        BukkitRunnable chargeTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) { cleanupSpell(uuid, spellId); return; }
                fullyChargedSpells.put(key, true);
                activeChargeTasks.remove(key);
                player.playSound(player.getLocation(), Sound.BLOCK_BEACON_ACTIVATE, 1.0f, 2.0f);
                sendActionBar(player, "&a&l✦ &f" + spell.getDisplayName() + " &ais fully charged!");

                // Auto-expire after 10 seconds if not cast
                Bukkit.getScheduler().runTaskLater(plugin, () -> {
                    if (fullyChargedSpells.containsKey(key)) {
                        sendActionBar(player, "&7Spell charge expired.");
                        cleanupSpell(uuid, spellId);
                    }
                }, 200L);
            }
        };

        activeChargeTasks.put(key, chargeTask);
        chargeTask.runTaskLater(plugin, Math.max(1L, (long) (chargeSeconds * 20)));
    }

    // ── Second Click ──────────────────────────────────────────────────────────

    public void handleSecondClick(Player player, Spell spell) {
        UUID uuid    = player.getUniqueId();
        String spellId = spell.getId();
        String key   = getKey(uuid, spellId);

        if (fullyChargedSpells.containsKey(key)) {
            executeSpell(player, spell);
            cleanupSpell(uuid, spellId);
        } else if (activeChargeTasks.containsKey(key)) {
            BukkitRunnable task = activeChargeTasks.remove(key);
            if (task != null) task.cancel();
            sendActionBar(player, "&7Spell cancelled.");
        }
    }

    // ── Execution ─────────────────────────────────────────────────────────────

    private void executeSpell(Player player, Spell spell) {
        UUID uuid = player.getUniqueId();
        preCastLevels.put(uuid, player.getLevel());
        
        int manaCost = (int) spell.getManaCost();
        
        // Apply mana cost reduction bonus (Mage 4-piece set bonus)
        if (com.ahren.arpgessentialsx.armors.setbonus.ManaCostReductionBonus.isActive(uuid)) {
            double reduction = com.ahren.arpgessentialsx.armors.setbonus.ManaCostReductionBonus.getManaCostReduction(uuid);
            manaCost = (int) Math.max(1, manaCost * (1.0 - reduction));
        }
        
        player.giveExpLevels(-manaCost);

        ItemStack book = player.getInventory().getItemInMainHand();
        bookFactory.consumeUse(book, spell);

        LivingEntity target = getTargetEntity(player);

        // FIXED: Changed to getOffhandCatalystMultiplier so the offhand catalyst scales the actual spell output
        CatalystMultiplier catalyst = plugin.getWeaponManager()
                .getItemFactory().getOffhandCatalystMultiplier(player);

        List<ConfigurationSection> effectConfigs = spell.getEffectConfigs();
        List<SpellEffect> effects = spell.getEffects();

        // One shared context for the entire cast — preserves lastLaunchedProjectile
        // across effects, and carries the catalyst multiplier cast-wide.
        SpellEffectContext sharedCtx = new SpellEffectContext(
                plugin,
                player,
                player.getEyeLocation(),
                target,
                effectConfigs.isEmpty() ? null : effectConfigs.get(0),
                spell,
                catalyst,
                1.0  // healingMultiplier — extend here when talent system is added
        );

        // Add spell damage amplification stacks (Mage 4-piece set bonus)
        com.ahren.arpgessentialsx.armors.setbonus.SpellDamageAmplificationBonus.onSpellCast(player, 
                effectConfigs.isEmpty() ? null : effectConfigs.get(0));

        for (int i = 0; i < effects.size(); i++) {
            ConfigurationSection effectConfig = i < effectConfigs.size() ? effectConfigs.get(i) : null;
            sharedCtx.swapConfig(effectConfig);

            try {
                effects.get(i).execute(sharedCtx);
            } catch (Exception e) {
                plugin.getLogger().warning("[SpellCastManager] Effect " + i
                        + " of spell '" + spell.getId() + "' threw: " + e.getMessage());
                e.printStackTrace();
            }
        }

        sendActionBar(player, "&fYou cast " + spell.getDisplayName() + "!");

        // Apply catalyst cooldown reduction
        double cooldownSeconds = spell.getCooldown() * catalyst.getCooldownMultiplier();
        startCooldown(player, spell, cooldownSeconds);
        startManaRegen(player);
    }

    // ── Target Resolution ─────────────────────────────────────────────────────

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

    // ── Cooldown ──────────────────────────────────────────────────────────────

    private void startCooldown(Player player, Spell spell, double seconds) {
        UUID uuid  = player.getUniqueId();
        String key = getKey(uuid, spell.getId());

        BukkitRunnable cdTask = new BukkitRunnable() {
            @Override
            public void run() {
                cooldownTasks.remove(key);
                if (player.isOnline())
                    sendActionBar(player, "&a" + spell.getDisplayName() + " &ais ready!");
            }
        };
        cooldownTasks.put(key, cdTask);
        cdTask.runTaskLater(plugin, Math.max(1L, (long) (seconds * 20)));
    }

    // ── Mana Regen ────────────────────────────────────────────────────────────

    private void startManaRegen(Player player) {
        UUID uuid = player.getUniqueId();
        BukkitRunnable old = regenTasks.remove(uuid);
        if (old != null) old.cancel();

        BukkitRunnable regenTask = new BukkitRunnable() {
            @Override
            public void run() {
                if (!player.isOnline()) { regenTasks.remove(uuid); cancel(); return; }
                int preCast = preCastLevels.getOrDefault(uuid, player.getLevel());
                if (player.getLevel() >= preCast) {
                    preCastLevels.remove(uuid);
                    regenTasks.remove(uuid);
                    cancel();
                    return;
                }
                player.giveExpLevels(1);
            }
        };
        regenTasks.put(uuid, regenTask);
        regenTask.runTaskTimer(plugin, 60L, 40L);
    }

    // ── Cleanup ───────────────────────────────────────────────────────────────

    private void cleanupSpell(UUID uuid, String spellId) {
        String key = getKey(uuid, spellId);
        BukkitRunnable task = activeChargeTasks.remove(key);
        if (task != null) task.cancel();
        fullyChargedSpells.remove(key);
    }

    public void cleanupPlayer(UUID uuid) {
        silencedPlayers.remove(uuid.toString());
        preCastLevels.remove(uuid);
        BukkitRunnable regen = regenTasks.remove(uuid);
        if (regen != null) regen.cancel();
    }
}