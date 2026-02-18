package me.mikey.refraction;

import me.mikey.refraction.command.AlertsCommand;
import me.mikey.refraction.command.RefractionCommand;
import me.mikey.refraction.command.RefractionTabCompleter;
import me.mikey.refraction.config.CachedConfig;
import me.mikey.refraction.manager.AlertManager;
import me.mikey.refraction.manager.CheckManager;
import me.mikey.refraction.manager.PlayerDataManager;
import org.bukkit.plugin.java.JavaPlugin;

public class Refraction extends JavaPlugin {
   private static Refraction instance;
   private CachedConfig cachedConfig;
   private PlayerDataManager playerDataManager;
   private CheckManager checkManager;
   private AlertManager alertManager;

   public void onEnable() {
      instance = this;
      saveDefaultConfig();
      cachedConfig = new CachedConfig();
      cachedConfig.reload(getConfig(), getLogger());
      alertManager = new AlertManager();
      playerDataManager = new PlayerDataManager(this);
      checkManager = new CheckManager(this);
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

   public CheckManager getCheckManager() {
      return checkManager;
   }

   public AlertManager getAlertManager() {
      return alertManager;
   }
}
