package com.ahren.arpgessentialsx.listeners;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.armors.setbonus.ManaCostReductionBonus;
import com.ahren.arpgessentialsx.spells.Spell;
import com.ahren.arpgessentialsx.spells.SpellBookFactory;
import com.ahren.arpgessentialsx.spells.SpellCastManager;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.NamespacedKey;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;

import java.util.UUID;

/**
 * Listens for right-click events to start or release spell charges.
 *
 * FIX — offhand double-fire bug:
 *   Bukkit fires PlayerInteractEvent TWICE when a player right-clicks:
 *   once for the main hand (HAND) and once for the off hand (OFF_HAND).
 *   Without the hand check below, every right-click would trigger startCharge
 *   or handleSecondClick twice — instantly cancelling the charge it just started.
 *
 *   Fix: guard with event.getHand() == EquipmentSlot.HAND so only the main-hand
 *   event is processed. The off-hand event is silently ignored.
 */
public final class SpellCastListener implements Listener {

    private final ARPGEssentialsX plugin;
    private final SpellCastManager castManager;
    private final NamespacedKey spellIdKey;

    public SpellCastListener(ARPGEssentialsX plugin, SpellCastManager castManager,
                             SpellBookFactory bookFactory) {
        this.plugin      = plugin;
        this.castManager = castManager;
        this.spellIdKey  = bookFactory.getSpellIdKey();
    }

    @EventHandler(priority = EventPriority.HIGH)
    public void onPlayerInteract(PlayerInteractEvent event) {
        // ── Offhand double-fire guard ─────────────────────────────────────────
        // Bukkit fires this event for both hands. We only want main-hand.
        if (event.getHand() != EquipmentSlot.HAND) return;

        // Don't check cancelled - our own listeners cancel events and we need to process anyway
        // if (event.isCancelled()) return;

        if (event.getAction() != Action.RIGHT_CLICK_AIR
                && event.getAction() != Action.RIGHT_CLICK_BLOCK) return;

        Player player = event.getPlayer();
        ItemStack item = player.getInventory().getItemInMainHand();

        if (item == null || !item.hasItemMeta()) return;
        PersistentDataContainer pdc = item.getItemMeta().getPersistentDataContainer();
        if (!pdc.has(spellIdKey, PersistentDataType.STRING)) return;

        // This is a spell book — cancel the interact so it doesn't open doors,
        // place blocks, or consume food while charging.
        event.setCancelled(true);

        String spellId = pdc.get(spellIdKey, PersistentDataType.STRING);
        Spell spell = plugin.getSpellManager().getSpell(spellId);
        if (spell == null) return;

        UUID uuid = player.getUniqueId();

        if (castManager.isCharging(uuid, spellId)) {
            castManager.handleSecondClick(player, spell);
        } else {
            castManager.startCharge(player, spell);
        }
    }
}