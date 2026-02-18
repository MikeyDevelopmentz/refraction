package me.mikey.refraction.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;

public class RefractionTabCompleter implements TabCompleter {

   @Override
   public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
      List<String> completions = new ArrayList<>();
      if (args.length == 1) {
         String partial = args[0].toLowerCase();
         if (sender.hasPermission("refraction.reload") && "reload".startsWith(partial)) completions.add("reload");
         if (sender.hasPermission("refraction.reload") && "status".startsWith(partial)) completions.add("status");
         if (sender.hasPermission("refraction.debug") && "debug".startsWith(partial)) completions.add("debug");
         return completions;
      }
      if (args.length == 2 && args[0].equalsIgnoreCase("debug") && sender.hasPermission("refraction.debug")) {
         String partial = args[1].toLowerCase();
         if ("on".startsWith(partial)) completions.add("on");
         if ("off".startsWith(partial)) completions.add("off");
         return completions;
      }
      return completions;
   }
}
