package com.ahren.arpgessentialsx.spells.effects.projectile;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.Bukkit;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.HandlerList;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.util.Vector;

/**
 * Fires a projectile that reflects off surfaces up to X times.
 *
 * yml params:
 *   bounces: 3      (max bounces, default 3)
 *   speed: 1.5      (default 1.5)
 */
public final class BouncingProjectileEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        int maxBounces = ctx.getInt("bounces", 3);
        double speed = ctx.getDouble("speed", 1.5);

        Snowball ball = ctx.getCaster().launchProjectile(Snowball.class);
        ball.setVelocity(ctx.getCaster().getEyeLocation().getDirection().multiply(speed));

        int[] bounces = {0};

        Listener listener = new Listener() {
            @EventHandler
            public void onHit(ProjectileHitEvent e) {
                if (!e.getEntity().equals(ball)) return;
                if (bounces[0] >= maxBounces) { HandlerList.unregisterAll(this); return; }

                if (e.getHitBlock() != null) {
                    e.setCancelled(true);
                    bounces[0]++;
                    Vector vel = ball.getVelocity();
                    // Reflect based on hit face
                    switch (e.getHitBlockFace()) {
                        case NORTH, SOUTH -> vel.setZ(-vel.getZ());
                        case EAST, WEST   -> vel.setX(-vel.getX());
                        default           -> vel.setY(-vel.getY());
                    }
                    ball.setVelocity(vel);
                } else {
                    HandlerList.unregisterAll(this);
                }
            }
        };
        Bukkit.getPluginManager().registerEvents(listener, ctx.getPlugin());
    }
}