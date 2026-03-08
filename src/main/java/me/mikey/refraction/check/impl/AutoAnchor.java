package me.mikey.refraction.check.impl;

import me.mikey.refraction.Refraction;
import me.mikey.refraction.check.Check;
import me.mikey.refraction.config.CachedConfig;
import me.mikey.refraction.data.PlayerData;
import me.mikey.refraction.profile.SuspicionScore;
import me.mikey.refraction.profile.TimingProfile;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;

public class AutoAnchor extends Check {

    public AutoAnchor(Refraction plugin) {
        super(plugin);
    }

    // scores a charge->charge cycle. sub-tick (<16ms) events are noise from keybinds so we ignore them
    static SuspicionScore analyzeCharge(long cycleMs, TimingProfile profile, CachedConfig.AutoAnchor cfg) {
        if (cycleMs > cfg.voidAfterMs) return null;
        if (cycleMs < TimingProfile.FLOOR_TICK_PERFECT_MS) return null;
        return new SuspicionScore(profile.score(cycleMs));
    }

    // if someone gets X sub-tick events in a row with no normal charge in between, thats a bot
    static SuspicionScore analyzeSubTickStreak(int streak, CachedConfig.AutoAnchor cfg) {
        if (streak < cfg.subTickStreakThreshold) return null;
        return new SuspicionScore(9.5);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        CachedConfig.AutoAnchor cfg = plugin.getCachedConfig().getAutoAnchor();
        if (!cfg.enabled) return;
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getItem() == null || event.getItem().getType() != Material.GLOWSTONE) return;
        if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.RESPAWN_ANCHOR) return;

        Player player = event.getPlayer();
        PlayerData data = getData(player);
        if (data == null) return;

        long now = System.nanoTime();

        if (data.lastAnchorChargeNano == 0L) {
            data.lastAnchorChargeNano = now;
            return;
        }

        long cycleMs = (now - data.lastAnchorChargeNano) / NS_PER_MS;

        SuspicionScore score = analyzeCharge(cycleMs, data.anchorChargeProfile, cfg);
        if (score == null) {
            if (cycleMs < TimingProfile.FLOOR_TICK_PERFECT_MS) {
                // sub-tick noise, track streak but dont advance the clock
                data.anchorSubTickStreak++;
                SuspicionScore streakScore = analyzeSubTickStreak(data.anchorSubTickStreak, cfg);
                if (streakScore != null) {
                    data.anchorSubTickStreak = 0;
                    alertScored(player, "sub-tick streak x" + cfg.subTickStreakThreshold, streakScore);
                }
            } else {
                // void break (too long since last charge), reset and move on
                data.anchorSubTickStreak = 0;
                data.lastAnchorChargeNano = now;
            }
            return;
        }

        data.anchorSubTickStreak = 0;
        data.lastAnchorChargeNano = now;

        if (plugin.getCachedConfig().isDebugLogging() && cfg.logCharges)
            plugin.getAlertManager().sendChargeLog(player, cycleMs);

        data.anchorChargeProfile.record(cycleMs);

        String profileInfo = data.anchorChargeProfile.isCalibrated()
            ? String.format("avg %.0fms ±%.0fms", data.anchorChargeProfile.getMean(), data.anchorChargeProfile.getStdDev())
            : "calibrating " + data.anchorChargeProfile.sampleCount() + "/" + TimingProfile.MIN_CALIBRATION;

        alertScored(player, cycleMs + "ms | " + profileInfo, score);
    }

    @EventHandler(ignoreCancelled = true)
    public void onBlockExplode(BlockExplodeEvent event) {
        CachedConfig.AutoAnchor cfg = plugin.getCachedConfig().getAutoAnchor();
        if (!plugin.getCachedConfig().isDebugLogging() || !cfg.logBlowups) return;
        if (event.getBlock().getType() != Material.RESPAWN_ANCHOR) return;
        plugin.getAlertManager().sendBlowupLog(event.getBlock().getLocation(), "overcharge");
    }

    @EventHandler(ignoreCancelled = true)
    public void onEntityExplode(EntityExplodeEvent event) {
        CachedConfig.AutoAnchor cfg = plugin.getCachedConfig().getAutoAnchor();
        if (!plugin.getCachedConfig().isDebugLogging() || !cfg.logBlowups) return;
        for (Block block : event.blockList()) {
            if (block.getType() == Material.RESPAWN_ANCHOR) {
                plugin.getAlertManager().sendBlowupLog(block.getLocation(),
                    event.getEntityType().name().toLowerCase().replace("_", " "));
                break;
            }
        }
    }
}
