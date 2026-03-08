package me.mikey.refraction.check.impl;

import java.util.Collections;
import me.mikey.refraction.Refraction;
import me.mikey.refraction.check.Check;
import me.mikey.refraction.config.CachedConfig;
import me.mikey.refraction.data.PlayerData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.EnderCrystal;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.entity.EntityDamageByEntityEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class FastCrystal extends Check {

    private static boolean isValidCrystalSurface(Block block) {
        if (block == null) return false;
        return block.getType() == Material.OBSIDIAN || block.getType() == Material.BEDROCK;
    }

    public FastCrystal(Refraction plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onInteract(PlayerInteractEvent event) {
        CachedConfig.FastCrystal cfg = plugin.getCachedConfig().getFastCrystal();
        if (!cfg.enabled) return;
        if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
        if (event.getItem() == null || event.getItem().getType() != Material.END_CRYSTAL) return;

        Block clicked = event.getClickedBlock();
        if (!isValidCrystalSurface(clicked)) return;

        Player player = event.getPlayer();
        PlayerData data = getData(player);
        if (data == null) return;

        long now = System.nanoTime();

        if (clicked.getLocation().equals(data.lastCrystalLocation)) {
            data.lastCrystalPlace = now;
            return;
        }

        if (data.lastCrystalPlace == 0L) {
            data.lastCrystalPlace = now;
            data.lastCrystalLocation = clicked.getLocation();
            return;
        }

        long diffMs = (now - data.lastCrystalPlace) / NS_PER_MS;

        if (diffMs < cfg.delay)
            alert(player, CachedConfig.replace(cfg.messagePlace, "delay", diffMs));

        data.crystalDelays.add(diffMs);
        if (data.crystalDelays.size() > cfg.samples) data.crystalDelays.removeFirst();
        if (data.crystalDelays.size() == cfg.samples) {
            long variance = Collections.max(data.crystalDelays) - Collections.min(data.crystalDelays);
            if (variance <= cfg.maxVariance) {
                alert(player, CachedConfig.replace(
                    CachedConfig.replace(cfg.messagePlaceConsistency, "variance", variance), "samples", cfg.samples));
                data.crystalDelays.clear();
            }
        }

        data.lastCrystalPlace = now;
        data.lastCrystalLocation = clicked.getLocation();
    }

    @EventHandler(ignoreCancelled = true)
    public void onCrystalDamage(EntityDamageByEntityEvent event) {
        CachedConfig.FastCrystal cfg = plugin.getCachedConfig().getFastCrystal();
        if (!cfg.enabled) return;
        if (!(event.getEntity() instanceof EnderCrystal)) return;
        if (!(event.getDamager() instanceof Player player)) return;

        PlayerData data = getData(player);
        if (data == null) return;

        long now = System.nanoTime();

        if (data.lastCrystalBreak == 0L) {
            data.lastCrystalBreak = now;
            return;
        }

        long diffMs = (now - data.lastCrystalBreak) / NS_PER_MS;

        if (diffMs < cfg.minBreakDelay)
            alert(player, CachedConfig.replace(cfg.messageBreak, "delay", diffMs));

        data.crystalBreakDelays.add(diffMs);
        if (data.crystalBreakDelays.size() > cfg.breakSamples) data.crystalBreakDelays.removeFirst();
        if (data.crystalBreakDelays.size() == cfg.breakSamples) {
            long variance = Collections.max(data.crystalBreakDelays) - Collections.min(data.crystalBreakDelays);
            if (variance <= cfg.breakMaxVariance) {
                alert(player, CachedConfig.replace(
                    CachedConfig.replace(cfg.messageBreakConsistency, "variance", variance), "samples", cfg.breakSamples));
                data.crystalBreakDelays.clear();
            }
        }

        data.lastCrystalBreak = now;
    }
}
