package com.ahren.arpgessentialsx.stats;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.classes.RPGClass;
import com.ahren.arpgessentialsx.data.PlayerData;
import com.ahren.arpgessentialsx.util.ColorUtil;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.scoreboard.*;

import java.util.*;

public final class StatsHUDManager {

    private final ARPGEssentialsX plugin;
    private final Set<UUID> activeHUDs = new HashSet<>();
    private static final String HEADER = buildGradientHeader();

    // Store base values for each player (vanilla + class only, no armor bonuses)
    private final Map<UUID, BaseStats> baseStatsMap = new HashMap<>();

    public StatsHUDManager(ARPGEssentialsX plugin) {
        this.plugin = plugin;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public boolean show(Player player) {
        // Hide party HUD if it's active
        if (plugin.getPartyManager().getPartyOf(player.getUniqueId()) != null) {
            plugin.getPartyHUDManager().hide(player);
        }

        activeHUDs.add(player.getUniqueId());
        updateBaseStats(player);
        updateHUD(player);
        return true;
    }

    public void hide(Player player) {
        activeHUDs.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());

        // If player is in a party, show party HUD instead
        if (plugin.getPartyManager().getPartyOf(player.getUniqueId()) != null) {
            plugin.getPartyHUDManager().show(player);
        }
    }

    public void toggle(Player player) {
        if (activeHUDs.contains(player.getUniqueId())) {
            hide(player);
            player.sendMessage(ColorUtil.translate("&7Stats HUD hidden."));
        } else {
            if (show(player)) {
                player.sendMessage(ColorUtil.translate("&aStats HUD shown."));
            }
        }
    }

    public void tickAll() {
        for (UUID uuid : new HashSet<>(activeHUDs)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) { activeHUDs.remove(uuid); continue; }
            // If player joined a party, hide stats HUD and show party HUD
            if (plugin.getPartyManager().getPartyOf(uuid) != null) {
                hide(player);
                continue;
            }
            updateHUD(player);
        }
    }

    // Called when class changes to update base stats
    public void updateBaseStats(Player player) {
        BaseStats base = new BaseStats();

        // Vanilla base values
        base.damage = 1.0;
        base.critMultiplier = 1.5;
        base.speed = 0.1;
        base.health = 20.0;
        base.armor = 0.0;
        base.armorToughness = 0.0;
        base.knockbackResistance = 0.0;

        // Add class modifiers to base
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        if (data != null && data.hasClass()) {
            RPGClass rpgClass = plugin.getClassManager().getClass(data.getClassId());
            if (rpgClass != null) {
                Double damageMod = rpgClass.getAttributes().get("damage");
                Double speedMod = rpgClass.getAttributes().get("speed");
                Double critMod = rpgClass.getPassives().get("crit_multiplier_bonus");
                Double healthMod = rpgClass.getAttributes().get("health");
                Double armorMod = rpgClass.getAttributes().get("armor");
                Double toughnessMod = rpgClass.getAttributes().get("armor_toughness");
                Double kbResMod = rpgClass.getAttributes().get("knockback_resistance");

                if (damageMod != null) base.damage += damageMod;
                if (speedMod != null) base.speed += speedMod;
                if (critMod != null) base.critMultiplier += critMod;
                if (healthMod != null) base.health += healthMod;
                if (armorMod != null) base.armor += armorMod;
                if (toughnessMod != null) base.armorToughness += toughnessMod;
                if (kbResMod != null) base.knockbackResistance += kbResMod;
            }
        }

        baseStatsMap.put(player.getUniqueId(), base);
    }

    // ── HUD Construction ──────────────────────────────────────────────────────

    @SuppressWarnings("deprecation")
    private void updateHUD(Player player) {
        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        Objective obj = board.registerNewObjective("statshud", Criteria.DUMMY, HEADER);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> lines = buildLines(player);

        int score = lines.size();
        for (String line : lines) {
            String entry = line.length() > 40 ? line.substring(0, 40) : line;
            Score s = obj.getScore(entry);
            s.setScore(score--);
            try {
                s.numberFormat(io.papermc.paper.scoreboard.numbers.NumberFormat.blank());
            } catch (NoSuchMethodError | NoClassDefFoundError ignored) {}
        }

        player.setScoreboard(board);
    }

    private List<String> buildLines(Player player) {
        List<String> lines = new ArrayList<>();
        BaseStats base = baseStatsMap.getOrDefault(player.getUniqueId(), new BaseStats());
        PlayerData data = plugin.getPlayerDataManager().getPlayerData(player.getUniqueId());
        
        // Get class name
        String className = "None";
        int classTag = 0;
        if (data != null && data.hasClass()) {
            RPGClass rpgClass = plugin.getClassManager().getClass(data.getClassId());
            if (rpgClass != null) {
                className = rpgClass.getDisplayName();
                classTag = rpgClass.getClassTag();
            }
        }
        
        lines.add("");
        lines.add("§f§lCHARACTER STATS");
        lines.add(ColorUtil.translate(className));
        lines.add("");

        // Level/Mana line
        String levelLabel = (classTag == 2) ? "Mana" : "Level";
        lines.add("§f=====§b✦" + levelLabel + " §f" + player.getLevel() + "§f=====");
        lines.add("");

        // ── Vitality ─────────────────────────────────────────────────────────
        lines.add("§c§lVITALITY:");
        
        double currentMaxHp = player.getAttribute(Attribute.GENERIC_MAX_HEALTH).getValue();
        double hpBonus = currentMaxHp - base.health;
        lines.add("§c❤ HP: §f" + String.format("%.2f", base.health) + formatBonus(hpBonus));

        lines.add("");

        // ── Attack & Defense ───────────────────────────────────────────────────
        lines.add("§e§lATK AND DEF:");

        // ATK
        double currentDamage = getCurrentDamage(player);
        double damageBonus = currentDamage - base.damage;
        lines.add("§c⚔ ATK: §f" + String.format("%.2f", base.damage) + formatBonus(damageBonus));

        // Crit
        double currentCrit = getCurrentCritMultiplier(player);
        double critBonus = currentCrit - base.critMultiplier;
        lines.add("§e⚔ CRITDMG: §f" + String.format("%.2f", base.critMultiplier) + formatBonus(critBonus, true));

        // Armor
        int currentArmor = getArmor(player);
        double armorBonus = currentArmor - base.armor;
        lines.add("§a🛡 AP: §f" + String.format("%.2f", (double)currentArmor));

        // Toughness
        double currentToughness = getToughness(player);
        lines.add("§a🛡 AT: §f" + String.format("%.2f", currentToughness));

        // Knockback Resistance
        double currentKbRes = getKnockbackResistance(player);
        lines.add("§a🛡 KB RES: §f" + String.format("%.2f%%", currentKbRes * 100));

        lines.add("");

        // ── Agility ───────────────────────────────────────────────────────────
        lines.add("§b§lAGILITY:");

        double currentSpeed = getCurrentSpeed(player);
        double speedBonus = currentSpeed - base.speed;
        lines.add("§b⚡ SPEED: §f" + String.format("%.2f", currentSpeed) + formatBonus(speedBonus));

        return lines;
    }

    // ── Stat Calculation Helpers ───────────────────────────────────────────────

    private double getCurrentDamage(Player player) {
        AttributeInstance inst = player.getAttribute(Attribute.GENERIC_ATTACK_DAMAGE);
        return inst != null ? inst.getValue() : 1.0;
    }

    private double getCurrentCritMultiplier(Player player) {
        // This would need to be stored/set by the class system
        // For now, use base value
        BaseStats base = baseStatsMap.getOrDefault(player.getUniqueId(), new BaseStats());
        return base.critMultiplier;
    }

    private double getCurrentSpeed(Player player) {
        AttributeInstance inst = player.getAttribute(Attribute.GENERIC_MOVEMENT_SPEED);
        return inst != null ? inst.getValue() : 0.1;
    }

    private int getArmor(Player player) {
        AttributeInstance inst = player.getAttribute(Attribute.GENERIC_ARMOR);
        return inst != null ? (int) inst.getValue() : 0;
    }

    private double getToughness(Player player) {
        AttributeInstance inst = player.getAttribute(Attribute.GENERIC_ARMOR_TOUGHNESS);
        return inst != null ? inst.getValue() : 0.0;
    }

    private double getKnockbackResistance(Player player) {
        AttributeInstance inst = player.getAttribute(Attribute.GENERIC_KNOCKBACK_RESISTANCE);
        return inst != null ? inst.getValue() : 0.0;
    }

    private String formatBonus(double bonus) {
        return formatBonus(bonus, false);
    }
    
    private String formatBonus(double bonus, boolean isDecimal) {
        if (Math.abs(bonus) < 0.001) return "";
        String sign = bonus > 0 ? "+" : "";
        return " §e" + sign + String.format("%.2f", bonus);
    }

    // ── Base Stats Storage ────────────────────────────────────────────────────

    private static class BaseStats {
        double damage;
        double critMultiplier;
        double speed;
        double health;
        double armor;
        double armorToughness;
        double knockbackResistance;
    }
    
    // ── Gradient Header ───────────────────────────────────────────────────────

    private static String buildGradientHeader() {
        String text = "HALCY✦N // SARPG";
        int[] startRGB = {0x55, 0xDD, 0xFF};
        int[] endRGB   = {0xAA, 0xFF, 0xFF};
        int len = text.length();
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < len; i++) {
            int r = lerp(startRGB[0], endRGB[0], i, len - 1);
            int g = lerp(startRGB[1], endRGB[1], i, len - 1);
            int b = lerp(startRGB[2], endRGB[2], i, len - 1);
            sb.append(hexColor(r, g, b)).append("§l").append(text.charAt(i));
        }
        return sb.toString();
    }

    private static int lerp(int start, int end, int step, int maxStep) {
        if (maxStep == 0) return start;
        return start + (end - start) * step / maxStep;
    }

    private static String hexColor(int r, int g, int b) {
        String hex = String.format("%02X%02X%02X", r, g, b);
        StringBuilder sb = new StringBuilder("§x");
        for (char c : hex.toCharArray()) sb.append('§').append(c);
        return sb.toString();
    }
}
