package me.mikey.refraction.command;

import me.mikey.refraction.Refraction;
import me.mikey.refraction.data.PlayerData;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class AlertsCommand implements CommandExecutor {
   private static final MiniMessage MINI = MiniMessage.miniMessage();

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (!(sender instanceof Player)) {
         sender.sendMessage(MINI.deserialize("<red>Players only."));
         return true;
      }
      Player player = (Player) sender;
      if (!player.hasPermission("refraction.alerts")) {
         player.sendMessage(MINI.deserialize("<red>No permission."));
         return true;
      }
      PlayerData data = Refraction.getInstance().getPlayerDataManager().get(player);
      if (data != null) {
         data.alertsEnabled = !data.alertsEnabled;
         String status = data.alertsEnabled ? "<green>ON" : "<red>OFF";
         player.sendMessage(MINI.deserialize("<yellow>Alerts: " + status));
      }
      return true;
   }
}
