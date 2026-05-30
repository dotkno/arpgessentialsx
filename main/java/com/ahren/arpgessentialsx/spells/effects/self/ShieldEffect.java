package com.ahren.arpgessentialsx.spells.effects.self;

import com.ahren.arpgessentialsx.spells.SpellEffect;
import com.ahren.arpgessentialsx.spells.SpellEffectContext;
import org.bukkit.Bukkit;
import org.bukkit.attribute.Attribute;
import org.bukkit.attribute.AttributeInstance;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/** Grants absorption hearts. yml: amount (half-hearts, default 8.0) */
public final class ShieldEffect implements SpellEffect {
    @Override
    public void execute(SpellEffectContext ctx) {
        double amount = ctx.getDouble("amount", 8.0);
        ctx.getCaster().setAbsorptionAmount(ctx.getCaster().getAbsorptionAmount() + amount);
    }
}