package me.mikey.refraction.manager;

import me.mikey.refraction.Refraction;
import me.mikey.refraction.check.impl.AutoAnchor;
import me.mikey.refraction.check.impl.AutoTotem;
import me.mikey.refraction.check.impl.FastCrystal;
import org.bukkit.Bukkit;

public class CheckManager {
   public CheckManager(Refraction plugin) {
      Bukkit.getPluginManager().registerEvents(new FastCrystal(plugin), plugin);
      Bukkit.getPluginManager().registerEvents(new AutoAnchor(plugin), plugin);
      Bukkit.getPluginManager().registerEvents(new AutoTotem(plugin), plugin);
   }
}
