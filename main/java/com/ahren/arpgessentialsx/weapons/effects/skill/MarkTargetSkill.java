package com.ahren.arpgessentialsx.weapons.effects.skill;

import com.ahren.arpgessentialsx.weapons.WeaponEffect;
import com.ahren.arpgessentialsx.weapons.WeaponEffectContext;
import org.bukkit.Bukkit;
import org.bukkit.Particle;
import org.bukkit.entity.LivingEntity;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 * Marks the looked-at target — applies glowing and weakness so they
 * take more damage and are visible through walls to the attacker.
 * Great for Marksman / Assassin weapons.
 *
 * yml params:
 *   duration: 6.0        (seconds, default 6.0)
 *   weakness_amp: 1      (weakness amplifier, default 1)
 */
public final class MarkTargetSkill implements WeaponEffect {
    @Override
    public void execute(WeaponEffectContext ctx) {
        if (!ctx.hasTarget()) return;
        int ticks = (int)(ctx.getDouble("duration", 6.0) * 20);
        int weakAmp = ctx.getInt("weakness_amp", 1);

        LivingEntity target = ctx.getTarget();

        // Glowing makes target visible through walls
        target.addPotionEffect(new PotionEffect(PotionEffectType.GLOWING, ticks, 0, false, false));
        // Weakness amplifies incoming damage perception
        target.addPotionEffect(new PotionEffect(PotionEffectType.WEAKNESS, ticks, weakAmp, false, true));

        // Visual burst at target
        target.getWorld().spawnParticle(
                Particle.CRIT, target.getLocation().add(0, 1, 0),
                20, 0.3, 0.5, 0.3, 0.1);
    }
}