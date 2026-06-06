package com.ahren.arpgessentialsx.customitems;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.event.player.PlayerItemConsumeEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.Sound;
import org.bukkit.Particle;

import java.util.HashMap;
import java.util.Map;

import java.util.List;

public class CustomItemConsumeListener implements Listener {

    private final ARPGEssentialsX plugin;
    private final CustomItemManager manager;
    private final CustomItemFactory factory;

    public CustomItemConsumeListener(ARPGEssentialsX plugin, CustomItemManager manager) {
        this.plugin = plugin;
        this.manager = manager;
        this.factory = manager.getItemFactory();
    }

    // player UUID -> (itemId -> last use millis)
    private final Map<java.util.UUID, Map<String, Long>> lastUse = new HashMap<>();

    @EventHandler
    public void onPlayerItemConsume(PlayerItemConsumeEvent event) {
        ItemStack item = event.getItem();
        if (item == null) return;
        String id = factory.getCustomItemId(item);
        if (id == null) return;
        CustomItem custom = manager.getItem(id);
        if (custom == null || !custom.isConsumable()) return;
        Player player = event.getPlayer();
        if (!canUse(player.getUniqueId(), id, custom.getConsumeCooldown())) {
            event.setCancelled(true);
            return;
        }

        recordUse(player.getUniqueId(), id);

        // play sound if configured
        if (custom.getConsumeSound() != null) {
            try {
                Sound s = Sound.valueOf(custom.getConsumeSound());
                player.playSound(player.getLocation(), s, 1.0f, 1.0f);
            } catch (Exception ignored) {}
        }

        // spawn particle if configured
        if (custom.getConsumeParticle() != null && custom.getConsumeParticleCount() > 0) {
            try {
                Particle p = Particle.valueOf(custom.getConsumeParticle());
                player.getWorld().spawnParticle(p, player.getLocation().add(0,1,0), custom.getConsumeParticleCount());
            } catch (Exception ignored) {}
        }

        // apply heal
        int heal = custom.getConsumeHeal();
        if (heal > 0) {
            double newHealth = Math.min(player.getMaxHealth(), player.getHealth() + heal);
            player.setHealth(newHealth);
        }

        // apply hunger and saturation
        int hunger = custom.getConsumeHunger();
        if (hunger > 0) {
            int newFood = Math.min(20, player.getFoodLevel() + hunger);
            player.setFoodLevel(newFood);
        }
        float saturation = custom.getConsumeSaturation();
        if (saturation > 0) {
            float newSaturation = Math.min(player.getSaturation() + saturation, player.getFoodLevel());
            player.setSaturation(newSaturation);
        }

        // apply potion effects
        List<String> effects = custom.getConsumeEffects();
        if (effects != null) {
            for (String s : effects) {
                // expected format: EFFECT_NAME:duration_seconds:amplifier
                try {
                    String[] parts = s.split(":");
                    if (parts.length >= 2) {
                        PotionEffectType type = PotionEffectType.getByName(parts[0].toUpperCase());
                        if (type == null) continue;
                        int duration = Integer.parseInt(parts[1]) * 20;
                        int amp = 0;
                        if (parts.length >= 3) amp = Math.max(0, Integer.parseInt(parts[2]) - 1);
                        player.addPotionEffect(new PotionEffect(type, duration, amp));
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    @EventHandler
    public void onPlayerInteract(PlayerInteractEvent event) {
        // handle using consumables that are not "edible" (non-food custom consumables)
        // edible materials will use PlayerItemConsumeEvent which shows eating animation
        if (event.getHand() != null && event.getHand() != EquipmentSlot.HAND) return; // only main hand
        if (!event.getAction().isRightClick()) return;
        ItemStack item = event.getItem();
        if (item == null) return;
        String id = factory.getCustomItemId(item);
        if (id == null) return;
        CustomItem custom = manager.getItem(id);
        if (custom == null || !custom.isConsumable()) return;
        
        // If the material is edible, let PlayerItemConsumeEvent handle it (shows eating animation)
        if (custom.getMaterial() != null && custom.getMaterial().isEdible()) {
            return;
        }
        
        Player player = event.getPlayer();
        if (!canUse(player.getUniqueId(), id, custom.getConsumeCooldown())) {
            event.setCancelled(true);
            return;
        }

        event.setCancelled(true);

        // decrease item in hand
        if (item.getAmount() > 1) {
            item.setAmount(item.getAmount() - 1);
            player.getInventory().setItemInMainHand(item);
        } else {
            player.getInventory().setItemInMainHand(null);
        }

        recordUse(player.getUniqueId(), id);

        // play sound and particle then apply effects
        if (custom.getConsumeSound() != null) {
            try { player.playSound(player.getLocation(), Sound.valueOf(custom.getConsumeSound()), 1.0f, 1.0f); } catch (Exception ignored) {}
        }
        if (custom.getConsumeParticle() != null && custom.getConsumeParticleCount() > 0) {
            try { player.getWorld().spawnParticle(Particle.valueOf(custom.getConsumeParticle()), player.getLocation().add(0,1,0), custom.getConsumeParticleCount()); } catch (Exception ignored) {}
        }

        int heal = custom.getConsumeHeal();
        if (heal > 0) {
            double newHealth = Math.min(player.getMaxHealth(), player.getHealth() + heal);
            player.setHealth(newHealth);
        }

        // apply hunger and saturation
        int hunger = custom.getConsumeHunger();
        if (hunger > 0) {
            int newFood = Math.min(20, player.getFoodLevel() + hunger);
            player.setFoodLevel(newFood);
        }
        float saturation = custom.getConsumeSaturation();
        if (saturation > 0) {
            float newSaturation = Math.min(player.getSaturation() + saturation, player.getFoodLevel());
            player.setSaturation(newSaturation);
        }

        List<String> effects = custom.getConsumeEffects();
        if (effects != null) {
            for (String s : effects) {
                try {
                    String[] parts = s.split(":");
                    if (parts.length >= 2) {
                        PotionEffectType type = PotionEffectType.getByName(parts[0].toUpperCase());
                        if (type == null) continue;
                        int duration = Integer.parseInt(parts[1]) * 20;
                        int amp = 0;
                        if (parts.length >= 3) amp = Math.max(0, Integer.parseInt(parts[2]) - 1);
                        player.addPotionEffect(new PotionEffect(type, duration, amp));
                    }
                } catch (Exception ignored) {}
            }
        }
    }

    private boolean canUse(java.util.UUID player, String id, int cooldownSeconds) {
        if (cooldownSeconds <= 0) return true;
        Map<String, Long> map = lastUse.get(player);
        if (map == null) return true;
        Long last = map.get(id);
        if (last == null) return true;
        return System.currentTimeMillis() - last >= cooldownSeconds * 1000L;
    }

    private void recordUse(java.util.UUID player, String id) {
        lastUse.computeIfAbsent(player, k -> new HashMap<>()).put(id, System.currentTimeMillis());
    }
}

