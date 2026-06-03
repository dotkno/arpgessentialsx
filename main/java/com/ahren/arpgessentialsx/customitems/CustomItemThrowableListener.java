package com.ahren.arpgessentialsx.customitems;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Player;
import org.bukkit.entity.Projectile;
import org.bukkit.entity.Snowball;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.entity.ProjectileHitEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.util.Vector;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class CustomItemThrowableListener implements Listener {

    private final ARPGEssentialsX plugin;
    private final CustomItemManager manager;
    private final CustomItemFactory factory;

    // cooldowns per player per item
    private final Map<java.util.UUID, Map<String, Long>> lastThrow = new HashMap<>();

    public CustomItemThrowableListener(ARPGEssentialsX plugin, CustomItemManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        this.factory = manager.getItemFactory();
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        if (event.getHand() != null && event.getHand() != EquipmentSlot.HAND) return;
        if (!event.getAction().isRightClick()) return;
        Player player = event.getPlayer();
        ItemStack item = event.getItem();
        if (item == null) return;
        String id = factory.getCustomItemId(item);
        if (id == null) return;
        CustomItem custom = manager.getItem(id);
        if (custom == null || !custom.isThrowable()) return;

        // cooldown (use throw.cooldown_seconds, not consume)
        if (!canUse(player.getUniqueId(), id, custom.getThrowCooldown())) {
            event.setCancelled(true);
            return;
        }

        // consume one
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
            player.getInventory().setItemInMainHand(item);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        // launch projectile
        Snowball proj = player.launchProjectile(Snowball.class);
        Vector dir = player.getEyeLocation().getDirection().multiply(custom.getThrowPower());
        proj.setVelocity(dir);
        // tag projectile with custom item id so we can resolve on hit
        proj.getPersistentDataContainer().set(factory.getCustomItemIdKey(), PersistentDataType.STRING, custom.getId());

        recordUse(player.getUniqueId(), id);
        event.setCancelled(true);
    }

    @EventHandler
    public void onProjectileHit(ProjectileHitEvent event) {
        Projectile p = event.getEntity();
        if (p == null) return;
        String id = p.getPersistentDataContainer().get(factory.getCustomItemIdKey(), PersistentDataType.STRING);
        if (id == null) return;
        CustomItem custom = manager.getItem(id);
        if (custom == null) return;

        LivingEntity shooter = p.getShooter() instanceof LivingEntity ? (LivingEntity) p.getShooter() : null;

        // apply effects: damage entity hit
        Entity hit = event.getHitEntity();
        if (hit instanceof LivingEntity && shooter != null) {
            LivingEntity le = (LivingEntity) hit;
            double dmg = custom.getThrowDamage();
            if (dmg > 0) {
                le.damage(dmg, (Entity) shooter);
            }
        }

        // apply on-hit effects
        org.bukkit.Location hitLoc = event.getHitBlock() != null ? event.getHitBlock().getLocation() : p.getLocation();
        applyOnHitEffects(custom, hitLoc, shooter);

        // explosion
        if (custom.isThrowExplode()) {
            p.getWorld().createExplosion(p.getLocation(), custom.getThrowExplosionPower(), false, false);
        }

        // remove projectile
        p.remove();
    }

    private void applyOnHitEffects(CustomItem custom, org.bukkit.Location location, LivingEntity shooter) {
        List<String> effects = custom.getThrowOnHitEffects();
        if (effects == null || effects.isEmpty()) return;

        for (String effectStr : effects) {
            try {
                String[] parts = effectStr.split(":", 2);
                if (parts.length < 1) continue;
                String type = parts[0].toUpperCase();

                switch (type) {
                    case "PARTICLE" -> {
                        // format: PARTICLE:name:count
                        String[] args = parts.length > 1 ? parts[1].split(":") : new String[]{};
                        if (args.length >= 1) {
                            try {
                                Particle p = Particle.valueOf(args[0].toUpperCase());
                                int count = args.length > 1 ? Math.max(1, Integer.parseInt(args[1])) : 10;
                                location.getWorld().spawnParticle(p, location, count);
                            } catch (Exception ignored) {}
                        }
                    }
                    case "SOUND" -> {
                        // format: SOUND:name:volume:pitch
                        String[] args = parts.length > 1 ? parts[1].split(":") : new String[]{};
                        if (args.length >= 1) {
                            try {
                                Sound s = Sound.valueOf(args[0].toUpperCase());
                                float vol = args.length > 1 ? Float.parseFloat(args[1]) : 1.0f;
                                float pitch = args.length > 2 ? Float.parseFloat(args[2]) : 1.0f;
                                location.getWorld().playSound(location, s, vol, pitch);
                            } catch (Exception ignored) {}
                        }
                    }
                    case "POTION" -> {
                        // format: POTION:name:duration_seconds:amplifier:radius
                        String[] args = parts.length > 1 ? parts[1].split(":") : new String[]{};
                        if (args.length >= 1) {
                            try {
                                PotionEffectType ptype = PotionEffectType.getByName(args[0].toUpperCase());
                                if (ptype == null) continue;
                                int duration = args.length > 1 ? (Integer.parseInt(args[1]) * 20) : (10 * 20);
                                int amp = args.length > 2 ? Math.max(0, Integer.parseInt(args[2]) - 1) : 0;
                                double radius = args.length > 3 ? Double.parseDouble(args[3]) : 3.0;

                                // apply to all entities in radius, except shooter
                                for (Entity e : location.getWorld().getNearbyEntities(location, radius, radius, radius)) {
                                    if (e instanceof LivingEntity && e != shooter) {
                                        ((LivingEntity) e).addPotionEffect(new PotionEffect(ptype, duration, amp, false, false));
                                    }
                                }
                            } catch (Exception ignored) {}
                        }
                    }
                }
            } catch (Exception ignored) {}
        }
    }

    private boolean canUse(java.util.UUID player, String id, int cooldownSeconds) {
        if (cooldownSeconds <= 0) return true;
        Map<String, Long> map = lastThrow.get(player);
        if (map == null) return true;
        Long last = map.get(id);
        if (last == null) return true;
        return System.currentTimeMillis() - last >= cooldownSeconds * 1000L;
    }

    private void recordUse(java.util.UUID player, String id) {
        lastThrow.computeIfAbsent(player, k -> new HashMap<>()).put(id, System.currentTimeMillis());
    }
}

