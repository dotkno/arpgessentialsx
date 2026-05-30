package com.ahren.arpgessentialsx.relics.effects.visual;

import com.ahren.arpgessentialsx.relics.RelicEffect;
import com.ahren.arpgessentialsx.relics.RelicEffectContext;
import org.bukkit.Sound;

/**
 * Plays a sound at the caster's location on activation.
 *
 * yml params:
 *   sound: ITEM_GOAT_HORN_SOUND_0   (any Bukkit Sound name)
 *   volume: 1.0                     (default 1.0)
 *   pitch: 1.0                      (default 1.0)
 */
public final class PlaySoundEffect implements RelicEffect {
    @Override
    public void execute(RelicEffectContext ctx) {
        String soundName = ctx.getString("sound", "ITEM_GOAT_HORN_SOUND_0").toUpperCase();
        float volume = (float) ctx.getDouble("volume", 1.0);
        float pitch  = (float) ctx.getDouble("pitch",  1.0);

        Sound sound;
        try {
            sound = Sound.valueOf(soundName);
        } catch (IllegalArgumentException e) {
            ctx.getPlugin().getLogger().warning("[PlaySoundEffect] Unknown sound: " + soundName);
            return;
        }

        ctx.getCaster().getWorld().playSound(ctx.getCastLocation(), sound, volume, pitch);
    }
}