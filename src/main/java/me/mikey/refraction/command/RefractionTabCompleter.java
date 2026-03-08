package me.mikey.refraction.command;

import java.util.ArrayList;
import java.util.List;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.bukkit.entity.Player;

public class RefractionTabCompleter implements TabCompleter {

    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> out = new ArrayList<>();
        if (args.length == 1) {
            String q = args[0].toLowerCase();
            if (sender.hasPermission("refraction.reload")) {
                for (String s : List.of("reload", "status", "profile")) if (s.startsWith(q)) out.add(s);
            }
            if (sender.hasPermission("refraction.debug") && "debug".startsWith(q)) out.add("debug");
            return out;
        }
        if (args.length == 2) {
            String sub = args[0].toLowerCase();
            String q   = args[1].toLowerCase();
            if (sub.equals("debug") && sender.hasPermission("refraction.debug")) {
                for (String s : List.of("on", "off")) if (s.startsWith(q)) out.add(s);
            }
            if (sub.equals("profile") && sender.hasPermission("refraction.reload")) {
                for (Player p : Bukkit.getOnlinePlayers())
                    if (p.getName().toLowerCase().startsWith(q)) out.add(p.getName());
            }
        }
        return out;
    }
}
