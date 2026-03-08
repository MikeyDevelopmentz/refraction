package me.mikey.refraction.check;

import me.mikey.refraction.Refraction;
import me.mikey.refraction.data.PlayerData;
import me.mikey.refraction.profile.SuspicionScore;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;

public abstract class Check implements Listener {

    protected static final long NS_PER_MS = 1_000_000L;

    protected final Refraction plugin;

    public Check(Refraction plugin) {
        this.plugin = plugin;
    }

    public String getCheckName() {
        return getClass().getSimpleName();
    }

    protected int getViolationThreshold() {
        return plugin.getCachedConfig().getViolationThreshold();
    }

    protected void alert(Player player, String details) {
        PlayerData data = getData(player);
        if (data == null) return;
        if (data.incrementViolations(getCheckName()) >= getViolationThreshold()) {
            data.resetViolations(getCheckName());
            plugin.getAlertManager().sendAlert(player, getCheckName(), details);
        }
    }

    // high scores count as 2 violations so they surface faster
    protected void alertScored(Player player, String details, SuspicionScore score) {
        if (!score.shouldAlert()) return;
        PlayerData data = getData(player);
        if (data == null) return;
        int weight = score.getLevel() == SuspicionScore.Level.HIGH ? 2 : 1;
        for (int i = 0; i < weight; i++) {
            if (data.incrementViolations(getCheckName()) >= getViolationThreshold()) {
                data.resetViolations(getCheckName());
                plugin.getAlertManager().sendAlert(player, getCheckName(), details, score);
                return;
            }
        }
    }

    protected PlayerData getData(Player player) {
        return plugin.getPlayerDataManager().get(player);
    }
}
