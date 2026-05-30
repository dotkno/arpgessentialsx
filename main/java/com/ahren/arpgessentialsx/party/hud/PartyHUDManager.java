package com.ahren.arpgessentialsx.party.hud;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.party.Party;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;
import org.bukkit.scoreboard.*;

import java.util.*;

public final class PartyHUDManager {

    private final ARPGEssentialsX plugin;
    private final Set<UUID> activeHUDs = new HashSet<>();
    private static final String HEADER = buildGradientHeader();

    public PartyHUDManager(ARPGEssentialsX plugin) {
        this.plugin = plugin;
    }

    // ── Public API ────────────────────────────────────────────────────────────

    public void show(Player player) {
        activeHUDs.add(player.getUniqueId());
        updateHUD(player);
    }

    public void hide(Player player) {
        activeHUDs.remove(player.getUniqueId());
        player.setScoreboard(Bukkit.getScoreboardManager().getMainScoreboard());
    }

    public void tickAll() {
        for (UUID uuid : new HashSet<>(activeHUDs)) {
            Player player = Bukkit.getPlayer(uuid);
            if (player == null) { activeHUDs.remove(uuid); continue; }
            if (plugin.getPartyManager().getPartyOf(uuid) == null) { hide(player); continue; }
            updateHUD(player);
        }
    }

    // ── HUD Construction ──────────────────────────────────────────────────────

    @SuppressWarnings("deprecation")
    private void updateHUD(Player player) {
        Party party = plugin.getPartyManager().getPartyOf(player.getUniqueId());
        if (party == null) return;

        ScoreboardManager manager = Bukkit.getScoreboardManager();
        Scoreboard board = manager.getNewScoreboard();

        Objective obj = board.registerNewObjective("partyhud", Criteria.DUMMY, HEADER);
        obj.setDisplaySlot(DisplaySlot.SIDEBAR);

        List<String> lines = buildLines(party);

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

    /**
     * Builds sidebar lines for the party.
     *
     * KEY FIX: Scoreboard entries must be unique strings. If two members
     * share the same effects (e.g. both have SPD), the second entry
     * silently overwrites the first — the leader's effects disappear.
     *
     * Fix: append invisible unique padding (varying §r counts) to every
     * line so entries are guaranteed unique regardless of content.
     */
    private List<String> buildLines(Party party) {
        List<String> lines = new ArrayList<>();
        List<UUID> members = party.getMembers();
        int[] counter = {0};

        for (int m = 0; m < members.size(); m++) {
            UUID uuid = members.get(m);

            // Spacer — each must be a unique string
            if (m > 0) lines.add("§r" + " ".repeat(m));

            Player member = Bukkit.getPlayer(uuid);
            boolean isLeader = party.isLeader(uuid);

            String name = member != null ? member.getName()
                    : Bukkit.getOfflinePlayer(uuid).getName();
            if (name == null) name = uuid.toString().substring(0, 8);

            // Name line — player names are already unique
            lines.add(isLeader
                    ? "§b✦ §f§l" + name + " §r§7[Leader]"
                    : "§e§l" + name);

            if (member != null && member.isOnline()) {
                double hp    = Math.ceil(member.getHealth());
                double maxHp = getMaxHealth(member);
                int armor    = getArmor(member);

                // HP/armor line — padded to be unique
                lines.add("§c❤ §f" + (int)hp + "§7/§f" + (int)maxHp
                        + "  §a🛡 §f" + armor + "§7/§f30"
                        + uniquePad(counter[0]++));

                // Effects line — only added if non-empty, padded to be unique
                String effects = buildEffectsLine(member);
                if (!effects.isEmpty()) {
                    lines.add(effects + uniquePad(counter[0]++));
                }
            } else {
                lines.add("§8Offline" + uniquePad(counter[0]++));
            }
        }

        return lines;
    }

    /**
     * Appends invisible unique padding using varying counts of §r (color reset).
     * Visually invisible to players but makes each scoreboard entry key unique.
     */
    private String uniquePad(int index) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i <= index; i++) sb.append("§r");
        return sb.toString();
    }

    /**
     * Builds a colored bold effect abbreviation line.
     *
     * Color guide:
     *   §e = yellow  — offensive buffs (STR, HASTE)
     *   §a = green   — movement buffs (SPD, JUMP)
     *   §d = pink    — healing (REGEN)
     *   §b = aqua    — defensive buffs (RES, FRES)
     *   §7 = gray    — neutral (INVIS)
     *   §c = red     — debuffs (PSN, WEAK, SLOW)
     *   §8 = dark gray — severe (BLIND)
     *   §4 = dark red  — severe (WITHER, NAUSEA)
     */
    private String buildEffectsLine(Player player) {
        List<String> abbrevs = new ArrayList<>();
        for (PotionEffect effect : player.getActivePotionEffects()) {
            String colored = coloredAbbrev(effect.getType());
            if (colored != null) abbrevs.add(colored);
        }
        if (abbrevs.isEmpty()) return "";
        return String.join("§7, ", abbrevs);
    }

    private String coloredAbbrev(PotionEffectType type) {
        // Offensive buffs
        if (type.equals(PotionEffectType.STRENGTH))        return "§e§lSTR";
        if (type.equals(PotionEffectType.HASTE))           return "§e§lHASTE";
        // Movement buffs
        if (type.equals(PotionEffectType.SPEED))           return "§a§lSPD";
        if (type.equals(PotionEffectType.JUMP_BOOST))      return "§a§lJUMP";
        // Healing
        if (type.equals(PotionEffectType.REGENERATION))    return "§d§lREGEN";
        // Defensive buffs
        if (type.equals(PotionEffectType.RESISTANCE))      return "§b§lRES";
        if (type.equals(PotionEffectType.FIRE_RESISTANCE)) return "§b§lFRES";
        // Neutral
        if (type.equals(PotionEffectType.INVISIBILITY))    return "§7§lINVIS";
        // Debuffs
        if (type.equals(PotionEffectType.POISON))          return "§c§lPSN";
        if (type.equals(PotionEffectType.WEAKNESS))        return "§c§lWEAK";
        if (type.equals(PotionEffectType.SLOWNESS))        return "§c§lSLOW";
        if (type.equals(PotionEffectType.BLINDNESS))       return "§8§lBLIND";
        if (type.equals(PotionEffectType.WITHER))          return "§4§lWITHER";
        if (type.equals(PotionEffectType.NAUSEA))          return "§4§lNAUSEA";
        return null;
    }

    private double getMaxHealth(Player player) {
        AttributeInstance inst = player.getAttribute(Attribute.GENERIC_MAX_HEALTH);
        return inst != null ? inst.getValue() : 20.0;
    }

    private int getArmor(Player player) {
        AttributeInstance inst = player.getAttribute(Attribute.GENERIC_ARMOR);
        return inst != null ? (int) inst.getValue() : 0;
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