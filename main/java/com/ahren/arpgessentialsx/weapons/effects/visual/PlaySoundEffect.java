package com.ahren.arpgessentialsx.weapons.effects.visual;

import com.ahren.arpgessentialsx.weapons.WeaponEffect;
import com.ahren.arpgessentialsx.weapons.WeaponEffectContext;
import org.bukkit.Sound;

/**
 * Plays a sound at the attacker's location.
 * Usable in both on_hit and skill effect lists.
 *
 * yml params:
 *   sound: ENTITY_PLAYER_ATTACK_SWEEP   (any Bukkit Sound name)
 *   volume: 1.0                         (default 1.0)
 *   pitch: 1.0                          (default 1.0)
 */
public final class PlaySoundEffect implements WeaponEffect {
    @Override
    public void execute(WeaponEffectContext ctx) {
        String soundName = ctx.getString("sound", "ENTITY_PLAYER_ATTACK_SWEEP").toUpperCase();
        float volume = (float) ctx.getDouble("volume", 1.0);
        float pitch  = (float) ctx.getDouble("pitch",  1.0);

        Sound sound;
        try {
            sound = Sound.valueOf(soundName);
        } catch (IllegalArgumentException e) {
            ctx.getPlugin().getLogger().warning("[PlaySoundEffect] Unknown sound: " + soundName);
            return;
        }

        ctx.getAttacker().getWorld().playSound(
                ctx.getCastLocation(), sound, volume, pitch);
    }
}