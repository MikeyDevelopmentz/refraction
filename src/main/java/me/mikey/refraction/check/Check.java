package me.mikey.refraction.check;

import me.mikey.refraction.Refraction;
import me.mikey.refraction.data.PlayerData;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class Check implements Listener {
   protected final Refraction plugin;

   public Check(Refraction plugin) {
      this.plugin = plugin;
   }

   public String getCheckName() {
      return this.getClass().getSimpleName();
   }

   protected int getViolationThreshold() {
      return plugin.getCachedConfig().getViolationThreshold();
   }

   protected void alert(Player player, String details) {
      PlayerData data = getData(player);
      if (data == null) return;
      int violationCount = data.incrementViolations(getCheckName());
      if (violationCount >= getViolationThreshold()) {
         data.resetViolations(getCheckName());
         this.plugin.getAlertManager().sendAlert(player, getCheckName(), details);
      }
   }

   protected PlayerData getData(Player player) {
      return this.plugin.getPlayerDataManager().get(player);
   }
}
