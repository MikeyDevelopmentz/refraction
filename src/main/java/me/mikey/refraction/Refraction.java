package me.mikey.refraction;

import me.mikey.refraction.check.impl.AutoAnchor;
import me.mikey.refraction.check.impl.AutoTotem;
import me.mikey.refraction.check.impl.FastCrystal;
import me.mikey.refraction.command.AlertsCommand;
import me.mikey.refraction.command.RefractionCommand;
import me.mikey.refraction.command.RefractionTabCompleter;
import me.mikey.refraction.config.CachedConfig;
import me.mikey.refraction.manager.AlertManager;
import me.mikey.refraction.manager.PlayerDataManager;
import org.bukkit.Bukkit;
import org.bukkit.plugin.java.JavaPlugin;

public class Refraction extends JavaPlugin {
   private static Refraction instance;
   private CachedConfig cachedConfig;
   private PlayerDataManager playerDataManager;
   private AlertManager alertManager;

   public void onEnable() {
      instance = this;
      saveDefaultConfig();
      cachedConfig = new CachedConfig();
      cachedConfig.reload(getConfig(), getLogger());
      alertManager = new AlertManager();
      playerDataManager = new PlayerDataManager(this);
      Bukkit.getPluginManager().registerEvents(new FastCrystal(this), this);
      Bukkit.getPluginManager().registerEvents(new AutoAnchor(this), this);
      Bukkit.getPluginManager().registerEvents(new AutoTotem(this), this);
      getCommand("alerts").setExecutor(new AlertsCommand());
      getCommand("refraction").setExecutor(new RefractionCommand());
      getCommand("refraction").setTabCompleter(new RefractionTabCompleter());
   }

   public void onDisable() {
   }

   public static Refraction getInstance() {
      return instance;
   }

   public CachedConfig getCachedConfig() {
      return cachedConfig;
   }

   public PlayerDataManager getPlayerDataManager() {
      return playerDataManager;
   }

   public AlertManager getAlertManager() {
      return alertManager;
   }
}
