package me.mikey.refraction.check.impl;

import java.util.Collections;
import me.mikey.refraction.Refraction;
import me.mikey.refraction.check.Check;
import me.mikey.refraction.config.CachedConfig;
import me.mikey.refraction.data.PlayerData;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.block.Action;
import org.bukkit.event.block.BlockExplodeEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.bukkit.event.entity.EntityExplodeEvent;
import org.bukkit.event.player.PlayerInteractEvent;

public class AutoAnchor extends Check {
   private static final long NANOS_PER_MS = 1_000_000L;

   public AutoAnchor(Refraction plugin) {
      super(plugin);
   }

   @EventHandler(ignoreCancelled = true)
   public void onPlace(BlockPlaceEvent event) {
      CachedConfig.AutoAnchor cfg = plugin.getCachedConfig().getAutoAnchor();
      if (!cfg.enabled) return;
      if (event.getBlock().getType() != Material.RESPAWN_ANCHOR) return;
      PlayerData data = getData(event.getPlayer());
      if (data != null) {
         data.lastAnchorPlace = System.nanoTime();
      }
   }

   @EventHandler(ignoreCancelled = true)
   public void onInteract(PlayerInteractEvent event) {
      CachedConfig.AutoAnchor cfg = plugin.getCachedConfig().getAutoAnchor();
      if (!cfg.enabled) return;
      if (event.getAction() != Action.RIGHT_CLICK_BLOCK) return;
      if (event.getItem() == null || event.getItem().getType() != Material.GLOWSTONE) return;
      if (event.getClickedBlock() == null || event.getClickedBlock().getType() != Material.RESPAWN_ANCHOR) return;

      Player player = event.getPlayer();
      PlayerData data = getData(player);
      if (data == null) return;

      long nowNanos = System.nanoTime();
      long diffMs = (nowNanos - data.lastAnchorPlace) / NANOS_PER_MS;
      if (diffMs > cfg.voidAfterMs) return;

      if (plugin.getCachedConfig().isDebugLogging() && cfg.logCharges) {
         plugin.getAlertManager().sendChargeLog(player, diffMs);
      }

      if (diffMs < cfg.chargeDelay) {
         String msg = CachedConfig.replace(cfg.messageCharge, "delay", diffMs);
         alert(player, msg);
      }

      data.anchorDelays.add(diffMs);
      long fastChargeCount = data.anchorDelays.stream().filter(delay -> delay < cfg.suspiciousMs).count();
      if (fastChargeCount >= cfg.suspiciousCount) {
         String msg = CachedConfig.replace(CachedConfig.replace(cfg.messageSuspicious, "threshold", cfg.suspiciousMs), "count", fastChargeCount);
         alert(player, msg);
         data.anchorDelays.clear();
         return;
      }
      if (data.anchorDelays.size() > cfg.samples) {
         data.anchorDelays.removeFirst();
      }
      if (data.anchorDelays.size() == cfg.samples) {
         long maxDelay = Collections.max(data.anchorDelays);
         long minDelay = Collections.min(data.anchorDelays);
         long variance = maxDelay - minDelay;
         if (variance <= cfg.maxVariance) {
            String msg = CachedConfig.replace(CachedConfig.replace(cfg.messageChargeConsistency, "variance", variance), "samples", cfg.samples);
            alert(player, msg);
            data.anchorDelays.clear();
         }
      }
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
            String cause = event.getEntityType().name().toLowerCase().replace("_", " ");
            plugin.getAlertManager().sendBlowupLog(block.getLocation(), cause);
            break;
         }
      }
   }
}
