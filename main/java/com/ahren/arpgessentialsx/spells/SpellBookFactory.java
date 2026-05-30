package com.ahren.arpgessentialsx.spells;

import com.ahren.arpgessentialsx.ARPGEssentialsX;
import com.ahren.arpgessentialsx.util.ColorUtil;
import com.ahren.arpgessentialsx.weapons.CatalystItemFactory; // Imported to use the buildStarLine utility
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.legacy.LegacyComponentSerializer;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;

import java.util.ArrayList;
import java.util.List;

/**
 * Factory class that turns a Spell data object into a physical Minecraft ItemStack.
 *
 * Why a factory? Because we need to create this item in multiple places:
 *   1. When registering the crafting recipe (the result of the craft)
 *   2. Later, when giving a player a spell via a command
 *
 * Keeping the creation logic in one place prevents bugs if we ever change
 * how the book looks or what tags it holds.
 */
public final class SpellBookFactory {

    private final ARPGEssentialsX plugin;
    private final LegacyComponentSerializer serializer;

    /** PDC key to store the spell's ID (e.g., "fireball") */
    private final NamespacedKey spellIdKey;

    /** PDC key to store the remaining uses (e.g., 35) */
    private final NamespacedKey usesLeftKey;

    public SpellBookFactory(ARPGEssentialsX plugin) {
        this.plugin = plugin;
        this.serializer = LegacyComponentSerializer.legacyAmpersand();
        this.spellIdKey = new NamespacedKey(plugin, "spell_id");
        this.usesLeftKey = new NamespacedKey(plugin, "uses_left");
    }

    /**
     * Creates a Spell Book ItemStack for a given spell.
     *
     * @param spell The spell to create the book for
     * @return The finished Spell Book item
     */
    public ItemStack createSpellBook(Spell spell) {
        ItemStack book = new ItemStack(Material.BOOK);
        ItemMeta meta = book.getItemMeta();

        // 1. Set the colored display name (1.21.1 Adventure API)
        String coloredName = ColorUtil.translate(spell.getDisplayName());
        meta.displayName(serializer.deserialize(coloredName));

        // 2. Build and set the lore, incorporating the unified star system layout
        List<Component> lore = new ArrayList<>();

        // Dynamically append the colored star rating and element tag to matching weapons.yml design
        lore.add(serializer.deserialize(
                CatalystItemFactory.buildStarLine(spell.getStars())
                        + " &8" + spell.getElement().toUpperCase() + " SPELL"
        ));
        lore.add(serializer.deserialize("&r "));

        // Append the rest of your custom lore setup from spells.yml
        for (String line : spell.getLore()) {
            lore.add(serializer.deserialize(ColorUtil.translate(line)));
        }
        meta.lore(lore);

        // 3. Add the enchanted glint (makes it look magical!)
        meta.setEnchantmentGlintOverride(true);

        // 4. Stamp invisible data onto the item using PDC
        meta.getPersistentDataContainer().set(spellIdKey, PersistentDataType.STRING, spell.getId());
        meta.getPersistentDataContainer().set(usesLeftKey, PersistentDataType.INTEGER, spell.getMaxUses());

        book.setItemMeta(meta);
        return book;
    }

    /**
     * @return The NamespacedKey used to check if an item is a spell book
     */
    public NamespacedKey getSpellIdKey() {
        return spellIdKey;
    }

    /**
     * @return The NamespacedKey used to check remaining spell uses
     */
    public NamespacedKey getUsesLeftKey() {
        return usesLeftKey;
    }

    /**
     * Consumes 1 use of a spell book. Updates the lore and destroys it if uses hit 0.
     *
     * @param book  The spell book itemstack
     * @param spell The spell data
     */
    public void consumeUse(ItemStack book, Spell spell) {
        ItemMeta meta = book.getItemMeta();
        if (meta == null) return;

        int usesLeft = meta.getPersistentDataContainer().get(usesLeftKey, PersistentDataType.INTEGER);
        if (usesLeft <= 0) return;

        int newUses = usesLeft - 1;

        // If out of uses, destroy the book with a poof effect
        if (newUses <= 0) {
            book.setAmount(0);
            return;
        }

        // Update the invisible tag
        meta.getPersistentDataContainer().set(usesLeftKey, PersistentDataType.INTEGER, newUses);

        // Update the visual lore while preserving the dynamic star tier heading
        List<Component> newLore = new ArrayList<>();

        newLore.add(serializer.deserialize(
                CatalystItemFactory.buildStarLine(spell.getStars())
                        + " &8" + spell.getElement().toUpperCase() + " SPELL"
        ));
        newLore.add(serializer.deserialize("&r "));

        for (String line : spell.getLore()) {
            if (line.toLowerCase().contains("uses:")) {
                // Replace the numbers in the "Uses: X/Y" line
                line = line.replaceAll("\\d+/\\d+", newUses + "/" + spell.getMaxUses());
            }
            newLore.add(serializer.deserialize(ColorUtil.translate(line)));
        }
        meta.lore(newLore);
        book.setItemMeta(meta);
    }
}