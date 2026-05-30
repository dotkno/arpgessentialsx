package com.ahren.arpgessentialsx.spells.effects.visual;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.Sound;

/**
 * Plays a sound at the cast location.
 *
 * yml params:
 *   sound: ENTITY_LIGHTNING_BOLT_THUNDER   (any Bukkit Sound name)
 *   volume: 1.0                            (default 1.0)
 *   pitch: 1.0                             (default 1.0)
 */
public final class PlaySoundEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        String soundName = ctx.getString("sound", "ENTITY_LIGHTNING_BOLT_THUNDER").toUpperCase();
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