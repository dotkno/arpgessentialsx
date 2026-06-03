package com.ahren.arpgessentialsx;

import com.ahren.arpgessentialsx.attributes.ClassAttributeApplier;
import com.ahren.arpgessentialsx.classes.ClassManager;
import com.ahren.arpgessentialsx.classes.RPGClass;
import com.ahren.arpgessentialsx.data.PlayerData;
import com.ahren.arpgessentialsx.data.PlayerDataManager;
import com.ahren.arpgessentialsx.gui.AdminMenuGUI;
import com.ahren.arpgessentialsx.gui.ClassSelectionGUI;
import com.ahren.arpgessentialsx.listeners.AdminMenuListener;
import com.ahren.arpgessentialsx.listeners.AuthMeLoginListener;
import com.ahren.arpgessentialsx.listeners.ClassSelectionListener;
import com.ahren.arpgessentialsx.listeners.DamageTrackerListener;
import com.ahren.arpgessentialsx.listeners.MageRestrictionListener;
import com.ahren.arpgessentialsx.listeners.PassiveListener;
import com.ahren.arpgessentialsx.listeners.PlayerDeathListener;
import com.ahren.arpgessentialsx.listeners.PlayerJoinListener;
import com.ahren.arpgessentialsx.listeners.SpellCastListener;
import com.ahren.arpgessentialsx.tasks.PassiveTickTask;
import com.ahren.arpgessentialsx.commands.ArpgCommand;
import com.ahren.arpgessentialsx.commands.ClassCommand;
import com.ahren.arpgessentialsx.commands.PartyCommand;
import com.ahren.arpgessentialsx.spells.SpellManager;
import com.ahren.arpgessentialsx.spells.SpellCastManager;
import com.ahren.arpgessentialsx.relics.RelicManager;
import com.ahren.arpgessentialsx.relics.RelicCastListener;
import com.ahren.arpgessentialsx.util.ColorUtil;
import com.ahren.arpgessentialsx.weapons.CatalystManager;
import com.ahren.arpgessentialsx.weapons.SkillCooldownTracker;
import com.ahren.arpgessentialsx.weapons.WeaponCombatListener;
import com.ahren.arpgessentialsx.weapons.WeaponEquipListener;
import com.ahren.arpgessentialsx.weapons.WeaponManager;
import com.ahren.arpgessentialsx.weapons.WeaponProficiencyListener;
import com.ahren.arpgessentialsx.weapons.WeaponSkillListener;
import com.ahren.arpgessentialsx.customitems.CustomItemManager;
import com.ahren.arpgessentialsx.armors.ArmorManager;
import com.ahren.arpgessentialsx.armors.ArmorEquipListener;
import com.ahren.arpgessentialsx.armors.SetBonusEventListener;
import com.ahren.arpgessentialsx.party.PartyManager;
import com.ahren.arpgessentialsx.party.hud.PartyHUDManager;
import com.ahren.arpgessentialsx.party.hud.PartyHUDTask;
import com.ahren.arpgessentialsx.stats.StatsHUDManager;
import com.ahren.arpgessentialsx.stats.StatsHUDTask;
import com.ahren.arpgessentialsx.party.listeners.PartyGUIListener;
import com.ahren.arpgessentialsx.party.listeners.PartyGameplayListeners;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public final class ARPGEssentialsX extends JavaPlugin {

    private static ARPGEssentialsX instance;

    // Core systems
    private ClassManager classManager;
    private PlayerDataManager playerDataManager;
    private ClassAttributeApplier attributeApplier;
    private ClassSelectionGUI classSelectionGUI;
    private AdminMenuGUI adminMenuGUI;

    // Spell system
    private SpellManager spellManager;
    private SpellCastManager spellCastManager;

    // Relic system
    private RelicManager relicManager;

    // Weapon system
    private WeaponManager weaponManager;
    private CatalystManager catalystManager;
    private SkillCooldownTracker skillCooldownTracker;
    private WeaponCombatListener weaponCombatListener;
    private WeaponEquipListener weaponEquipListener;

    // Custom items
    private CustomItemManager customItemManager;

    // Armor system
    private ArmorManager armorManager;
    private ArmorEquipListener armorEquipListener;
    private SetBonusEventListener setBonusEventListener;
    private com.ahren.arpgessentialsx.armors.ArmorPassiveListener armorPassiveListener;

    // Party system
    private PartyManager partyManager;
    private PartyHUDManager partyHUDManager;

    // Stats HUD
    private StatsHUDManager statsHUDManager;

    // Shared state
    private final Map<UUID, Long> lastDamageTime = new HashMap<>();

    @Override
    public void onEnable() {
        instance = this;

        // Core systems
        classManager      = new ClassManager(this);
        playerDataManager = new PlayerDataManager(this);
        attributeApplier  = new ClassAttributeApplier(this);
        classSelectionGUI = new ClassSelectionGUI(this);

        // Spell system
        spellManager     = new SpellManager(this);
        spellCastManager = new SpellCastManager(this, spellManager.getBookFactory());

        // Relic system
        relicManager = new RelicManager(this);
        // ...existing code...
        getServer().getPluginManager().registerEvents(new com.ahren.arpgessentialsx.relics.RelicThrowableListener(relicManager, relicManager.getItemFactory()), this);

        // Weapon system (CatalystManager after WeaponManager so it can read weapons.yml)
        weaponManager        = new WeaponManager(this);
        catalystManager      = new CatalystManager(this);
        skillCooldownTracker = new SkillCooldownTracker(this);
        weaponCombatListener = new WeaponCombatListener(this, weaponManager.getItemFactory(), skillCooldownTracker);
        weaponEquipListener  = new WeaponEquipListener(this, weaponManager, weaponManager.getItemFactory());

        // Armor system
        armorManager          = new ArmorManager(this);
        armorEquipListener    = new ArmorEquipListener(this, armorManager);
        armorPassiveListener  = new com.ahren.arpgessentialsx.armors.ArmorPassiveListener(this, armorEquipListener, lastDamageTime);
        setBonusEventListener = new SetBonusEventListener(this, armorManager);

        // Custom items
        customItemManager = new CustomItemManager(this);
        // Register custom item listeners (consumables + throwables)
        getServer().getPluginManager().registerEvents(new com.ahren.arpgessentialsx.customitems.CustomItemConsumeListener(this, customItemManager), this);
        getServer().getPluginManager().registerEvents(new com.ahren.arpgessentialsx.customitems.CustomItemThrowableListener(this, customItemManager), this);

        // Party system
        partyManager    = new PartyManager(this);
        partyHUDManager = new PartyHUDManager(this);

        // Stats HUD
        statsHUDManager = new StatsHUDManager(this);

        // Admin menu (must be after managers are initialized)
        adminMenuGUI = new AdminMenuGUI(this);

        // Register listeners
        getServer().getPluginManager().registerEvents(new SpellCastListener(this, spellCastManager, spellManager.getBookFactory()), this);
        getServer().getPluginManager().registerEvents(new MageRestrictionListener(this), this);
        getServer().getPluginManager().registerEvents(new RelicCastListener(this, relicManager.getItemFactory()), this);
        getServer().getPluginManager().registerEvents(weaponCombatListener, this);
        getServer().getPluginManager().registerEvents(weaponEquipListener, this);
        getServer().getPluginManager().registerEvents(new WeaponSkillListener(this, weaponManager.getItemFactory(), skillCooldownTracker), this);
        getServer().getPluginManager().registerEvents(new WeaponProficiencyListener(this, weaponManager.getItemFactory(), weaponCombatListener), this);
        getServer().getPluginManager().registerEvents(new ClassSelectionListener(this, classSelectionGUI.getClassIdKey()), this);
        getServer().getPluginManager().registerEvents(new PassiveListener(this), this);
        getServer().getPluginManager().registerEvents(new DamageTrackerListener(this, lastDamageTime), this);
        getServer().getPluginManager().registerEvents(new PartyGUIListener(this, partyHUDManager), this);
        getServer().getPluginManager().registerEvents(new PartyGameplayListeners(this), this);
        getServer().getPluginManager().registerEvents(new AdminMenuListener(this, adminMenuGUI), this);
        getServer().getPluginManager().registerEvents(armorEquipListener, this);
        getServer().getPluginManager().registerEvents(armorPassiveListener, this);
        getServer().getPluginManager().registerEvents(setBonusEventListener, this);
        getServer().getPluginManager().registerEvents(new PlayerDeathListener(this), this);

        // Register join listeners
        registerJoinListeners();

        // Register commands
        getCommand("arpg").setExecutor(new ArpgCommand(this));
        getCommand("class").setExecutor(new ClassCommand(this));
        getCommand("party").setExecutor(new PartyCommand(this));

        // Start background tasks
        new PassiveTickTask(this, lastDamageTime).runTaskTimer(this, 20L, 20L);
        new PartyHUDTask(partyHUDManager).runTaskTimer(this, 2L, 2L);
        new StatsHUDTask(this, statsHUDManager).runTaskTimer(this, 4L, 4L);

        getLogger().info("=================================");
        getLogger().info("  ARPGEssentialsX v" + getDescription().getVersion());
        getLogger().info("  Plugin enabled successfully.");
        getLogger().info("=================================");
    }

    private void registerJoinListeners() {
        getServer().getPluginManager().registerEvents(new PlayerJoinListener(this), this);
        getServer().getPluginManager().registerEvents(new com.ahren.arpgessentialsx.listeners.PlayerQuitListener(this), this);
        if (Bukkit.getPluginManager().getPlugin("AuthMe") != null) {
            getServer().getPluginManager().registerEvents(new AuthMeLoginListener(this), this);
            getLogger().info("AuthMe detected — class attributes will apply after login.");
        } else {
            getLogger().info("AuthMe not detected — class attributes will apply on join.");
        }
    }

    @Override
    public void onDisable() {
        if (playerDataManager != null) {
            playerDataManager.saveAll();
        }
        getLogger().info("ARPGEssentialsX disabled.");
    }

    public static ARPGEssentialsX getInstance()          { return instance; }
    public ClassManager getClassManager()                { return classManager; }
    public PlayerDataManager getPlayerDataManager()      { return playerDataManager; }
    public ClassAttributeApplier getAttributeApplier()   { return attributeApplier; }
    public ClassSelectionGUI getClassSelectionGUI()      { return classSelectionGUI; }
    public AdminMenuGUI getAdminMenuGUI()                { return adminMenuGUI; }
    public SpellManager getSpellManager()                { return spellManager; }
    public SpellCastManager getSpellCastManager()        { return spellCastManager; }
    public RelicManager getRelicManager()                { return relicManager; }
    public WeaponManager getWeaponManager()              { return weaponManager; }
    public CatalystManager getCatalystManager()          { return catalystManager; }
    public SkillCooldownTracker getSkillCooldownTracker(){ return skillCooldownTracker; }
    public CustomItemManager getCustomItemManager()      { return customItemManager; }
    public PartyManager getPartyManager()                { return partyManager; }
    public PartyHUDManager getPartyHUDManager()          { return partyHUDManager; }
    public StatsHUDManager getStatsHUDManager()          { return statsHUDManager; }
    public ArmorManager getArmorManager()                { return armorManager; }
    public ArmorEquipListener getArmorEquipListener()     { return armorEquipListener; }
    public com.ahren.arpgessentialsx.armors.ArmorPassiveListener getArmorPassiveListener() { return armorPassiveListener; }
    public SetBonusEventListener getSetBonusEventListener() { return setBonusEventListener; }

    public void applyClassToPlayer(Player player) {
        PlayerData data = playerDataManager.getOrCreatePlayerData(player.getUniqueId());
        if (data.hasClass()) {
            RPGClass rpgClass = classManager.getClass(data.getClassId());
            if (rpgClass != null) {
                attributeApplier.applyAttributes(player, rpgClass);
                statsHUDManager.updateBaseStats(player);
                player.sendMessage(ColorUtil.translate("&aClass attributes applied!"));
                getLogger().info("Applied class '" + rpgClass.getId() + "' to " + player.getName());
            } else {
                getLogger().warning("Player " + player.getName()
                        + " has saved class '" + data.getClassId()
                        + "' but it no longer exists in classes.yml!");
            }
        } else {
            attributeApplier.applyCivilianStats(player);
            statsHUDManager.updateBaseStats(player);
            Bukkit.getScheduler().runTaskLater(this, () -> classSelectionGUI.open(player), 1L);
        }
    }
}