package com.ahren.arpgessentialsx.stats;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import org.bukkit.scheduler.BukkitRunnable;

public final class StatsHUDTask extends BukkitRunnable {

    private final ARPGEssentialsX plugin;
    private final StatsHUDManager statsHUDManager;

    public StatsHUDTask(ARPGEssentialsX plugin, StatsHUDManager statsHUDManager) {
        this.plugin = plugin;
        this.statsHUDManager = statsHUDManager;
    }

    @Override
    public void run() {
        statsHUDManager.tickAll();
    }
}
