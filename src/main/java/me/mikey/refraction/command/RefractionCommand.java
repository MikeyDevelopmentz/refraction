package me.mikey.refraction.command;

import me.mikey.refraction.Refraction;
import me.mikey.refraction.config.CachedConfig;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;

public class RefractionCommand implements CommandExecutor {
   private static final MiniMessage MINI = MiniMessage.miniMessage();

   public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
      if (args.length > 0 && args[0].equalsIgnoreCase("reload")) {
         if (!sender.hasPermission("refraction.reload")) {
            sender.sendMessage(MINI.deserialize("<red>No permission."));
            return true;
         }
         Refraction refraction = Refraction.getInstance();
         refraction.reloadConfig();
         refraction.getCachedConfig().reload(refraction.getConfig(), refraction.getLogger());
         refraction.getPlayerDataManager().resetAllViolations();
         sender.sendMessage(MINI.deserialize("<green>Config reloaded."));
         return true;
      }
      if (args.length > 0 && args[0].equalsIgnoreCase("status")) {
         if (!sender.hasPermission("refraction.reload")) {
            sender.sendMessage(MINI.deserialize("<red>No permission."));
            return true;
         }
         Refraction refraction = Refraction.getInstance();
         CachedConfig c = refraction.getCachedConfig();
         sender.sendMessage(MINI.deserialize("<yellow>--- Refraction status ---</yellow>"));
         sender.sendMessage(MINI.deserialize("<gray>violation-threshold: <white>" + c.getViolationThreshold() + "</white></gray>"));
         sender.sendMessage(MINI.deserialize("<gray>debug.logging: " + (c.isDebugLogging() ? "<green>on" : "<red>off") + "</gray>"));
         CachedConfig.FastCrystal fc = c.getFastCrystal();
         sender.sendMessage(MINI.deserialize("<gray>FastCrystal: " + (fc.enabled ? "<green>on" : "<red>off") + "</gray> <white>delay=" + fc.delay + "ms</white> <white>min-break=" + fc.minBreakDelay + "ms</white> <white>samples=" + fc.samples + "</white>"));
         CachedConfig.AutoAnchor ac = c.getAutoAnchor();
         sender.sendMessage(MINI.deserialize("<gray>AutoAnchor: " + (ac.enabled ? "<green>on" : "<red>off") + "</gray> <white>charge-delay=" + ac.chargeDelay + "ms</white> <white>void-after=" + ac.voidAfterMs + "ms</white> <white>suspicious=" + ac.suspiciousMs + "ms/" + ac.suspiciousCount + "</white>"));
         CachedConfig.AutoTotem tc = c.getAutoTotem();
         sender.sendMessage(MINI.deserialize("<gray>AutoTotem: " + (tc.enabled ? "<green>on" : "<red>off") + "</gray> <white>open=" + tc.minOpenDelay + "ms</white> <white>reaction=" + tc.minReactionDelay + "ms</white> <white>close=" + tc.minCloseDelay + "ms</white>"));
         return true;
      }
      if (args.length > 0 && args[0].equalsIgnoreCase("debug")) {
         if (!sender.hasPermission("refraction.debug")) {
            sender.sendMessage(MINI.deserialize("<red>No permission."));
            return true;
         }
         Refraction refraction = Refraction.getInstance();
         boolean current = refraction.getCachedConfig().isDebugLogging();
         if (args.length < 2) {
            sender.sendMessage(MINI.deserialize("<gray>Debug logging: " + (current ? "<green>on" : "<red>off") + "</gray>"));
            sender.sendMessage(MINI.deserialize("<gray>refraction debug on / refraction debug off</gray>"));
            return true;
         }
         boolean enable;
         if (args[1].equalsIgnoreCase("on")) enable = true;
         else if (args[1].equalsIgnoreCase("off")) enable = false;
         else {
            sender.sendMessage(MINI.deserialize("<red>refraction debug on or off</red>"));
            return true;
         }
         refraction.getConfig().set("debug.logging", enable);
         refraction.saveConfig();
         refraction.getCachedConfig().reload(refraction.getConfig(), refraction.getLogger());
         sender.sendMessage(MINI.deserialize("<green>Debug " + (enable ? "on" : "off") + "."));
         return true;
      }
      sender.sendMessage(MINI.deserialize("<red>refraction reload, refraction status, refraction debug on/off</red>"));
      return true;
   }
}
