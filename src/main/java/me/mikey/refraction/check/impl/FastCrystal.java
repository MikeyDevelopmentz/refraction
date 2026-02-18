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
   private static final long NANOS_PER_MS = 1_000_000L;

   private static boolean isValidCrystalSurface(Block block) {
      if (block == null) return false;
      Material type = block.getType();
      return type == Material.OBSIDIAN || type == Material.BEDROCK;
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
      if (clicked == null || !isValidCrystalSurface(clicked)) return;

      Player player = event.getPlayer();
      PlayerData data = getData(player);
      if (data == null) return;

      long nowNanos = System.nanoTime();

      if (clicked.getLocation().equals(data.lastCrystalLocation)) {
         data.lastCrystalPlace = nowNanos;
         return;
      }

      if (data.lastCrystalPlace == 0L) {
         data.lastCrystalPlace = nowNanos;
         data.lastCrystalLocation = clicked.getLocation();
         return;
      }

      long diffMs = (nowNanos - data.lastCrystalPlace) / NANOS_PER_MS;
      if (diffMs < cfg.delay) {
         String msg = CachedConfig.replace(cfg.messagePlace, "delay", diffMs);
         alert(player, msg);
      }

      data.crystalDelays.add(diffMs);
      if (data.crystalDelays.size() > cfg.samples) {
         data.crystalDelays.removeFirst();
      }
      if (data.crystalDelays.size() == cfg.samples) {
         long maxDelay = Collections.max(data.crystalDelays);
         long minDelay = Collections.min(data.crystalDelays);
         long variance = maxDelay - minDelay;
         if (variance <= cfg.maxVariance) {
            String msg = CachedConfig.replace(CachedConfig.replace(cfg.messagePlaceConsistency, "variance", variance), "samples", cfg.samples);
            alert(player, msg);
            data.crystalDelays.clear();
         }
      }

      data.lastCrystalPlace = nowNanos;
      data.lastCrystalLocation = clicked.getLocation();
   }

   @EventHandler(ignoreCancelled = true)
   public void onCrystalDamage(EntityDamageByEntityEvent event) {
      CachedConfig.FastCrystal cfg = plugin.getCachedConfig().getFastCrystal();
      if (!cfg.enabled) return;
      if (!(event.getEntity() instanceof EnderCrystal)) return;
      if (!(event.getDamager() instanceof Player)) return;

      Player player = (Player) event.getDamager();
      PlayerData data = getData(player);
      if (data == null) return;

      long nowNanos = System.nanoTime();

      if (data.lastCrystalBreak == 0L) {
         data.lastCrystalBreak = nowNanos;
         return;
      }

      long diffMs = (nowNanos - data.lastCrystalBreak) / NANOS_PER_MS;
      if (diffMs < cfg.minBreakDelay) {
         String msg = CachedConfig.replace(cfg.messageBreak, "delay", diffMs);
         alert(player, msg);
      }

      data.crystalBreakDelays.add(diffMs);
      if (data.crystalBreakDelays.size() > cfg.breakSamples) {
         data.crystalBreakDelays.removeFirst();
      }
      if (data.crystalBreakDelays.size() == cfg.breakSamples) {
         long maxDelay = Collections.max(data.crystalBreakDelays);
         long minDelay = Collections.min(data.crystalBreakDelays);
         long variance = maxDelay - minDelay;
         if (variance <= cfg.breakMaxVariance) {
            String msg = CachedConfig.replace(CachedConfig.replace(cfg.messageBreakConsistency, "variance", variance), "samples", cfg.breakSamples);
            alert(player, msg);
            data.crystalBreakDelays.clear();
         }
      }

      data.lastCrystalBreak = nowNanos;
   }
}
