package com.ahren.arpgessentialsx.weapons;

import java.util.Arrays;
import java.util.List;

/**
 * The six weapon types, each with:
 *   - Natural class tags (players in these classes use the weapon at full power)
 *   - Off-class damage multiplier (applied to non-natural users)
 *   - Off-class attack speed modifier (negative = slower swing)
 *
 * Class tags: 1=Fighter  2=Mage  3=Marksman  4=Assassin  5=Tank
 */
public enum WeaponType {

    SWORD(
            "Sword",
            List.of(1, 4),          // Fighter, Assassin
            0.75,                   // -25% damage
            -0.6                    // 60% slower for off-class
    ),

    CLAYMORE(
            "Claymore",
            List.of(1, 5),          // Fighter, Tank
            0.75,
            -0.8                    // 80% slower — claymores are heavy
    ),

    POLEARM(
            "Polearm",
            List.of(1, 5),          // Fighter, Tank
            0.75,
            -0.7                    // 70% slower — polearms require reach
    ),

    DAGGER(
            "Dagger",
            List.of(4, 3),          // Assassin, Marksman
            0.75,
            -0.5                    // 50% slower — daggers are light but still penalized
    ),

    BOW(
            "Bow",
            List.of(3),             // Marksman only
            0.75,
            -0.7                    // 70% slower draw for non-Marksman
    ),

    CATALYST(
            "Catalyst",
            List.of(2),             // Mage only — others deal 0 damage
            0.0,                    // 0 = completely useless for non-Mages
            -0.9                    // 90% slower for non-Mages
    );

    private final String displayName;
    private final List<Integer> naturalClasses;
    private final double offClassDamageMultiplier;
    private final double offClassSpeedModifier;

    WeaponType(String displayName, List<Integer> naturalClasses,
               double offClassDamageMultiplier, double offClassSpeedModifier) {
        this.displayName              = displayName;
        this.naturalClasses           = naturalClasses;
        this.offClassDamageMultiplier = offClassDamageMultiplier;
        this.offClassSpeedModifier    = offClassSpeedModifier;
    }

    public String getDisplayName()              { return displayName; }
    public List<Integer> getNaturalClasses()    { return naturalClasses; }
    public double getOffClassDamageMultiplier() { return offClassDamageMultiplier; }
    public double getOffClassSpeedModifier()    { return offClassSpeedModifier; }

    public boolean isNaturalFor(int classTag)   { return naturalClasses.contains(classTag); }

    /**
     * Parses a weapon type from a config string.
     * Case-insensitive. Returns SWORD as fallback.
     */
    public static WeaponType fromString(String s) {
        if (s == null) return SWORD;
        return Arrays.stream(values())
                .filter(t -> t.name().equalsIgnoreCase(s)
                        || t.displayName.equalsIgnoreCase(s))
                .findFirst()
                .orElse(SWORD);
    }
}