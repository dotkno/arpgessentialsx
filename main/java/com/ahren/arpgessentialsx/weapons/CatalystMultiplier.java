package com.ahren.arpgessentialsx.weapons;

/**
 * Holds all spell multipliers granted by a catalyst/wand.
 *
 * Built by CatalystItemFactory based on the item's star rating and passed
 * into SpellEffectContext so every effect can scale accordingly.
 *
 * A null multiplier (no catalyst equipped) = all values 1.0 / bonus 0.
 *
 * Star scaling summary:
 * ┌────────────┬───────┬───────┬───────┬───────┬───────┐
 * │ Stat       │  1★   │  2★   │  3★   │  4★   │  5★   │
 * ├────────────┼───────┼───────┼───────┼───────┼───────┤
 * │ Damage     │ ×1.00 │ ×1.10 │ ×1.25 │ ×1.45 │ ×1.70 │
 * │ Radius     │ ×1.00 │ ×1.05 │ ×1.10 │ ×1.20 │ ×1.30 │
 * │ Duration   │ ×1.00 │ ×1.10 │ ×1.20 │ ×1.35 │ ×1.50 │
 * │ Charge     │ ×1.00 │ ×0.95 │ ×0.85 │ ×0.75 │ ×0.60 │
 * │ Cooldown   │ ×1.00 │ ×0.95 │ ×0.85 │ ×0.75 │ ×0.65 │
 * │ Yield      │ ×1.00 │ ×1.10 │ ×1.20 │ ×1.40 │ ×1.60 │
 * │ Amplifier  │  +0   │  +0   │  +1   │  +1   │  +2   │
 * └────────────┴───────┴───────┴───────┴───────┴───────┘
 *
 * Amplifier bonus is added to the base amplifier set in the spell's yml.
 * Example: a spell with amplifier: 0 (Regen I) becomes Regen II at 3★ and Regen III at 5★.
 *
 * Usage in a spell effect:
 *   int amplifier = ctx.getInt("amplifier", 0) + ctx.catalyst().getAmplifierBonus();
 *   int ticks     = (int)(ctx.catalyst().scaleDuration(ctx.getDouble("duration", 3.0)) * 20);
 */
public final class CatalystMultiplier {

    /** Flat 1.0 multiplier — used when no catalyst is equipped */
    public static final CatalystMultiplier NONE = new CatalystMultiplier(1);

    private final int stars;

    private final double damageMultiplier;
    private final double radiusMultiplier;
    private final double durationMultiplier;
    private final double chargeTimeMultiplier;
    private final double cooldownMultiplier;
    private final double yieldMultiplier;

    /**
     * Flat bonus added to the spell's base amplifier.
     * 0 = no change, 1 = one level higher, 2 = two levels higher.
     * e.g. base amplifier 0 (Regen I) + bonus 2 = amplifier 2 (Regen III).
     */
    private final int amplifierBonus;

    public CatalystMultiplier(int stars) {
        this.stars = Math.max(1, Math.min(5, stars));

        switch (this.stars) {
            case 1 -> {
                damageMultiplier     = 1.00;
                radiusMultiplier     = 1.00;
                durationMultiplier   = 1.00;
                chargeTimeMultiplier = 1.00;
                cooldownMultiplier   = 1.00;
                yieldMultiplier      = 1.00;
                amplifierBonus       = 0;
            }
            case 2 -> {
                damageMultiplier     = 1.10;
                radiusMultiplier     = 1.05;
                durationMultiplier   = 1.10;
                chargeTimeMultiplier = 0.95;
                cooldownMultiplier   = 0.95;
                yieldMultiplier      = 1.10;
                amplifierBonus       = 0;    // still no amplifier upgrade at 2★
            }
            case 3 -> {
                damageMultiplier     = 1.25;
                radiusMultiplier     = 1.10;
                durationMultiplier   = 1.20;
                chargeTimeMultiplier = 0.85;
                cooldownMultiplier   = 0.85;
                yieldMultiplier      = 1.20;
                amplifierBonus       = 1;    // e.g. Regen I → Regen II
            }
            case 4 -> {
                damageMultiplier     = 1.45;
                radiusMultiplier     = 1.20;
                durationMultiplier   = 1.35;
                chargeTimeMultiplier = 0.75;
                cooldownMultiplier   = 0.75;
                yieldMultiplier      = 1.40;
                amplifierBonus       = 1;    // same +1 as 3★, but other stats are stronger
            }
            default -> { // 5★
                damageMultiplier     = 1.70;
                radiusMultiplier     = 1.30;
                durationMultiplier   = 1.50;
                chargeTimeMultiplier = 0.60;
                cooldownMultiplier   = 0.65;
                yieldMultiplier      = 1.60;
                amplifierBonus       = 2;    // e.g. Regen I → Regen III
            }
        }
    }

    // ── Getters ───────────────────────────────────────────────────────────────

    public int getStars()                   { return stars; }
    public double getDamageMultiplier()     { return damageMultiplier; }
    public double getRadiusMultiplier()     { return radiusMultiplier; }
    public double getDurationMultiplier()   { return durationMultiplier; }
    public double getChargeTimeMultiplier() { return chargeTimeMultiplier; }
    public double getCooldownMultiplier()   { return cooldownMultiplier; }
    public double getYieldMultiplier()      { return yieldMultiplier; }

    /**
     * Flat amplifier bonus to add on top of a spell effect's base amplifier.
     * Always non-negative. Use as:
     *   int amplifier = ctx.getInt("amplifier", 0) + ctx.catalyst().getAmplifierBonus();
     */
    public int getAmplifierBonus()          { return amplifierBonus; }

    // ── Scale helpers ─────────────────────────────────────────────────────────

    public double scaleDamage(double base)   { return base * damageMultiplier; }
    public double scaleRadius(double base)   { return base * radiusMultiplier; }
    public double scaleDuration(double base) { return base * durationMultiplier; }
    public double scaleYield(double base)    { return base * yieldMultiplier; }
    public int scaleYieldInt(int base)       { return (int) Math.round(base * yieldMultiplier); }

    /**
     * Applies the amplifier bonus to a base amplifier value.
     * Equivalent to: base + getAmplifierBonus()
     * Provided as a helper so call sites read naturally:
     *   int amp = ctx.catalyst().scaleAmplifier(ctx.getInt("amplifier", 0));
     */
    public int scaleAmplifier(int base)     { return base + amplifierBonus; }
}