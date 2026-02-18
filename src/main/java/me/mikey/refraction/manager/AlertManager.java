package me.mikey.refraction.manager;

import me.mikey.refraction.Refraction;
import me.mikey.refraction.data.PlayerData;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.minimessage.MiniMessage;
import net.kyori.adventure.text.minimessage.tag.resolver.Placeholder;
import net.kyori.adventure.text.minimessage.tag.resolver.TagResolver;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.entity.Player;

public class AlertManager {
   private static final MiniMessage MINI = MiniMessage.miniMessage();

   public void sendChargeLog(Player player, long chargeTimeMs) {
      String format = Refraction.getInstance().getConfig().getString("alerts.log-charge-format",
         "<dark_gray>[Refraction]</dark_gray> <gold>Charge:</gold> <white><player></white> <gray>charged anchor in</gray> <white><ms>ms</white>");
      TagResolver resolver = TagResolver.resolver(
         Placeholder.unparsed("player", player.getName()),
         Placeholder.unparsed("ms", String.valueOf(chargeTimeMs))
      );
      Component message = MINI.deserialize(format, resolver);
      sendLogToAll(message);
   }

   public void sendBlowupLog(Location location, String cause) {
      String format = Refraction.getInstance().getConfig().getString("alerts.log-blowup-format",
         "<dark_gray>[Refraction]</dark_gray> <red>Blowup:</red> <gray>anchor at</gray> <white><location></white>");
      String locationStr = location.getBlockX() + ", " + location.getBlockY() + ", " + location.getBlockZ();
      if (cause != null && !cause.isEmpty()) locationStr += " (" + cause + ")";
      TagResolver resolver = Placeholder.unparsed("location", locationStr);
      Component message = MINI.deserialize(format, resolver);
      sendLogToAll(message);
   }

   private void sendLogToAll(Component message) {
      for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
         if (onlinePlayer.hasPermission("refraction.alerts")) {
            PlayerData playerData = Refraction.getInstance().getPlayerDataManager().get(onlinePlayer);
            if (playerData != null && playerData.alertsEnabled) {
               onlinePlayer.sendMessage(message);
            }
         }
      }
      Bukkit.getConsoleSender().sendMessage(message);
   }

   public void sendAlert(Player player, String checkName, String details) {
      String prefixFormat = Refraction.getInstance().getConfig().getString("alerts.prefix", "<red>[Refraction] </red><gray>");
      String messageFormat = Refraction.getInstance().getConfig().getString("alerts.message", "<yellow><player></yellow> <gray>failed</gray> <red><check></red> <gray>(<details>)</gray>");

      TagResolver resolver = TagResolver.resolver(
         Placeholder.unparsed("player", player.getName()),
         Placeholder.unparsed("check", checkName),
         Placeholder.unparsed("details", details)
      );

      Component prefix = MINI.deserialize(prefixFormat);
      Component message = MINI.deserialize(messageFormat, resolver);
      Component fullMessage = prefix.append(message);

      for (Player onlinePlayer : Bukkit.getOnlinePlayers()) {
         if (onlinePlayer.hasPermission("refraction.alerts")) {
            PlayerData playerData = Refraction.getInstance().getPlayerDataManager().get(onlinePlayer);
            if (playerData != null && playerData.alertsEnabled) {
               onlinePlayer.sendMessage(fullMessage);
            }
         }
      }
      Bukkit.getConsoleSender().sendMessage(fullMessage);
   }
}
