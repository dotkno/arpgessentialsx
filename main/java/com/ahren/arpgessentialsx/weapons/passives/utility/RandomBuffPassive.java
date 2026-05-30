package com.ahren.arpgessentialsx.weapons.passives.utility;

import com.ahren.arpgessentialsx.weapons.passives.WeaponPassive;
import com.ahren.arpgessentialsx.weapons.passives.WeaponPassiveContext;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.List;
import java.util.Random;

/**
 * Applies a random positive potion buff to the player on kill.
 *
 * yml params:
 *   duration_ticks: 100   (how long the buff lasts, default 100)
 *   amplifier:      0     (effect level, 0 = level I, default 0)
 *
 * Trigger: ON_KILL
 */
public final class RandomBuffPassive implements WeaponPassive {

    private static final List<PotionEffectType> BUFFS = List.of(
            PotionEffectType.SPEED,
            PotionEffectType.STRENGTH,
            PotionEffectType.REGENERATION,
            PotionEffectType.RESISTANCE,
            PotionEffectType.HASTE,
            PotionEffectType.JUMP_BOOST
    );

    private static final Random RANDOM = new Random();

    @Override
    public Trigger getTrigger() {
        return Trigger.ON_KILL;
    }

    @Override
    public void apply(WeaponPassiveContext ctx) {
        if (ctx.getTrigger() != WeaponPassiveContext.Trigger.ON_KILL) return;

        int duration  = ctx.getInt("duration_ticks", ctx.getInt("duration", 100));
        int amplifier = ctx.getInt("amplifier", 0);

        PotionEffectType type = BUFFS.get(RANDOM.nextInt(BUFFS.size()));
        ctx.getPlayer().addPotionEffect(new PotionEffect(type, duration, amplifier, true, false));
    }
}