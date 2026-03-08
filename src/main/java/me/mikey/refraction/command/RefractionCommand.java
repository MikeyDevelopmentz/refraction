package me.mikey.refraction.command;

import me.mikey.refraction.Refraction;
import me.mikey.refraction.config.CachedConfig;
import me.mikey.refraction.data.PlayerData;
import me.mikey.refraction.profile.TimingProfile;
import net.kyori.adventure.text.minimessage.MiniMessage;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class RefractionCommand implements CommandExecutor {

    private static final MiniMessage MM = MiniMessage.miniMessage();

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (args.length == 0) {
            sender.sendMessage(MM.deserialize("<red>Usage: /refraction <reload|status|debug|profile>"));
            return true;
        }

        Refraction plugin = Refraction.getInstance();

        switch (args[0].toLowerCase()) {
            case "reload" -> {
                if (!sender.hasPermission("refraction.reload")) { noPerms(sender); return true; }
                plugin.reloadConfig();
                plugin.getCachedConfig().reload(plugin.getConfig(), plugin.getLogger());
                plugin.getPlayerDataManager().resetAllViolations();
                sender.sendMessage(MM.deserialize("<green>Config reloaded."));
            }
            case "status" -> {
                if (!sender.hasPermission("refraction.reload")) { noPerms(sender); return true; }
                CachedConfig c = plugin.getCachedConfig();
                CachedConfig.FastCrystal fc = c.getFastCrystal();
                CachedConfig.AutoAnchor  ac = c.getAutoAnchor();
                CachedConfig.AutoTotem   tc = c.getAutoTotem();
                sender.sendMessage(MM.deserialize("<yellow>--- Refraction ---"));
                sender.sendMessage(MM.deserialize("<gray>vl-threshold: <white>" + c.getViolationThreshold()
                    + " <gray>| debug: " + toggle(c.isDebugLogging())));
                sender.sendMessage(MM.deserialize("<gray>FastCrystal: " + toggle(fc.enabled)
                    + " <white>delay=" + fc.delay + "ms break=" + fc.minBreakDelay + "ms samples=" + fc.samples));
                sender.sendMessage(MM.deserialize("<gray>AutoAnchor: " + toggle(ac.enabled)
                    + " <white>void-after=" + ac.voidAfterMs + "ms streak-threshold=" + ac.subTickStreakThreshold));
                sender.sendMessage(MM.deserialize("<gray>AutoTotem: " + toggle(tc.enabled)
                    + " <white>open=" + tc.minOpenDelay + "ms reaction=" + tc.minReactionDelay + "ms close=" + tc.minCloseDelay + "ms"));
            }
            case "debug" -> {
                if (!sender.hasPermission("refraction.debug")) { noPerms(sender); return true; }
                if (args.length < 2) {
                    sender.sendMessage(MM.deserialize("<gray>Debug: " + toggle(plugin.getCachedConfig().isDebugLogging())
                        + " <gray>| /refraction debug on|off"));
                    return true;
                }
                boolean enable = switch (args[1].toLowerCase()) {
                    case "on"  -> true;
                    case "off" -> false;
                    default -> {
                        sender.sendMessage(MM.deserialize("<red>on or off."));
                        yield plugin.getCachedConfig().isDebugLogging();
                    }
                };
                plugin.getConfig().set("debug.logging", enable);
                plugin.saveConfig();
                plugin.getCachedConfig().reload(plugin.getConfig(), plugin.getLogger());
                sender.sendMessage(MM.deserialize("<green>Debug " + (enable ? "on" : "off") + "."));
            }
            case "profile" -> {
                if (!sender.hasPermission("refraction.reload")) { noPerms(sender); return true; }
                if (args.length < 2) {
                    sender.sendMessage(MM.deserialize("<red>Usage: /refraction profile <player>"));
                    return true;
                }
                Player target = Bukkit.getPlayerExact(args[1]);
                if (target == null) {
                    sender.sendMessage(MM.deserialize("<red>" + args[1] + " is not online."));
                    return true;
                }
                PlayerData data = plugin.getPlayerDataManager().get(target);
                if (data == null) {
                    sender.sendMessage(MM.deserialize("<red>No data for " + target.getName() + "."));
                    return true;
                }
                TimingProfile anchor = data.anchorChargeProfile;
                String anchorLine = anchor.isCalibrated()
                    ? String.format("avg <white>%.0fms</white> ±<white>%.0fms</white> | <white>%d</white> samples | streak <white>%d</white>",
                        anchor.getMean(), anchor.getStdDev(), anchor.sampleCount(), data.anchorSubTickStreak)
                    : "calibrating <white>" + anchor.sampleCount() + "/" + TimingProfile.MIN_CALIBRATION
                        + "</white> | streak <white>" + data.anchorSubTickStreak + "</white>";
                sender.sendMessage(MM.deserialize("<yellow>--- " + target.getName() + "'s profile ---"));
                sender.sendMessage(MM.deserialize("<gray>AutoAnchor: " + anchorLine));
                sender.sendMessage(MM.deserialize("<gray>FastCrystal vls: <white>" + data.getViolations("FastCrystal")));
                sender.sendMessage(MM.deserialize("<gray>AutoAnchor vls:  <white>" + data.getViolations("AutoAnchor")));
                sender.sendMessage(MM.deserialize("<gray>AutoTotem vls:   <white>" + data.getViolations("AutoTotem")));
            }
            default -> sender.sendMessage(MM.deserialize("<red>Unknown subcommand. Try reload, status, debug, profile."));
        }
        return true;
    }

    private static void noPerms(CommandSender sender) {
        sender.sendMessage(MM.deserialize("<red>No permission."));
    }

    private static String toggle(boolean on) {
        return on ? "<green>on</green>" : "<red>off</red>";
    }
}
