package com.ahren.arpgessentialsx.tasks;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.classes.RPGClass;
import com.ahren.arpgessentialsx.data.PlayerData;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.Map;
import java.util.UUID;

public final class PassiveTickTask extends BukkitRunnable {

    private final ARPGEssentialsX plugin;
    private final Map<UUID, Long> lastDamageTime;
    private final NamespacedKey shieldTagKey;

    public PassiveTickTask(ARPGEssentialsX plugin, Map<UUID, Long> lastDamageTime) {
        this.plugin = plugin;
        this.lastDamageTime = lastDamageTime;
        this.shieldTagKey = new NamespacedKey(plugin, "unbreakable_shield");
    }

    @Override
    public void run() {
        long now = System.currentTimeMillis();

        for (var entry : plugin.getPlayerDataManager().getPlayerDataMap().entrySet()) {
            UUID uuid = entry.getKey();
            PlayerData data = entry.getValue();

            if (!data.hasClass()) {
                // NO CLASS (CIVILIAN): Force remove unbreakable from any shield they hold
                var player = plugin.getServer().getPlayer(uuid);
                if (player != null && player.isOnline()) {
                    checkShield(player.getInventory().getItemInOffHand(), false);
                    checkShield(player.getInventory().getItemInMainHand(), false);
                }
                continue;
            }

            var player = plugin.getServer().getPlayer(uuid);
            if (player == null || !player.isOnline()) continue;

            RPGClass rpgClass = plugin.getClassManager().getClass(data.getClassId());
            if (rpgClass == null) continue;

            handleFighterRegen(player, rpgClass, uuid, now);
            handleTankShields(player, rpgClass);
        }
    }

    private void handleFighterRegen(org.bukkit.entity.Player player, RPGClass rpgClass, UUID uuid, long now) {
        double regenDelay = rpgClass.getPassive("regen_delay");
        double regenAmount = rpgClass.getPassive("regen_amount");

        if (regenDelay <= 0.0 || regenAmount <= 0.0) return;

        Long lastDamage = lastDamageTime.get(uuid);
        boolean outOfCombat = (lastDamage == null) || ((now - lastDamage) >= (regenDelay * 1000));

        if (outOfCombat) {
            AttributeInstance healthAttr = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
            if (healthAttr != null) {
                double currentHealth = player.getHealth();
                double maxHealth = healthAttr.getValue();

                if (currentHealth < maxHealth) {
                    double newHealth = Math.min(currentHealth + regenAmount, maxHealth);
                    player.setHealth(newHealth);
                }
            }
        }
    }

    private void handleTankShields(org.bukkit.entity.Player player, RPGClass rpgClass) {
        double unbreakableSetting = rpgClass.getPassive("unbreakable_shields");
        boolean isTank = unbreakableSetting > 0.0;

        checkShield(player.getInventory().getItemInOffHand(), isTank);
        checkShield(player.getInventory().getItemInMainHand(), isTank);
    }

    /**
     * NEW LOGIC:
     * If Tank holds it: Tag it AND make it unbreakable (even if it was already unbreakable, we tag it now).
     * If Non-Tank holds it: If it has our tag, strip unbreakable and remove tag.
     */
    private void checkShield(ItemStack shield, boolean isTank) {
        if (shield == null || shield.getType() != Material.SHIELD) return;

        ItemMeta meta = shield.getItemMeta();
        if (meta == null) return;

        boolean isTagged = meta.getPersistentDataContainer().has(shieldTagKey, PersistentDataType.BYTE);

        if (isTank) {
            // Tank is holding it: ensure it is tagged and unbreakable
            if (!isTagged) {
                meta.getPersistentDataContainer().set(shieldTagKey, PersistentDataType.BYTE, (byte) 1);
            }
            if (!meta.isUnbreakable()) {
                meta.setUnbreakable(true);
            }
            shield.setItemMeta(meta);
        } else {
            // Not a tank: if WE tagged it previously, strip it
            if (isTagged) {
                meta.setUnbreakable(false);
                meta.getPersistentDataContainer().remove(shieldTagKey);
                shield.setItemMeta(meta);
            }
        }
    }
}