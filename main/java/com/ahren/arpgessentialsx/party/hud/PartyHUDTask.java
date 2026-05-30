package com.ahren.arpgessentialsx.party.hud;

import org.bukkit.scheduler.BukkitRunnable;

/**
 * Ticks the party HUD every 2 server ticks (≈100ms).
 *
 * Why 2 ticks instead of event-driven?
 *   Event-driven would require listening to EntityDamageEvent, FoodLevelChangeEvent,
 *   PotionEffectAddEvent, ArmorEquipEvent, and more — all at HIGH priority —
 *   and dispatching a scoreboard update after each one. That's more total work
 *   per second than a simple 2-tick poll, especially under combat load.
 *   2 ticks is imperceptible to players and causes near-zero CPU overhead.
 */
public final class PartyHUDTask extends BukkitRunnable {

    private final PartyHUDManager hudManager;

    public PartyHUDTask(PartyHUDManager hudManager) {
        this.hudManager = hudManager;
    }

    @Override
    public void run() {
        hudManager.tickAll();
    }
}