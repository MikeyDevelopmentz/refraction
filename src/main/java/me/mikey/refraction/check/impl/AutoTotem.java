package me.mikey.refraction.check.impl;

import java.util.Collections;
import me.mikey.refraction.Refraction;
import me.mikey.refraction.check.Check;
import me.mikey.refraction.config.CachedConfig;
import me.mikey.refraction.data.PlayerData;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.event.inventory.InventoryOpenEvent;
import org.bukkit.event.inventory.InventoryType;

public class AutoTotem extends Check {
   private static final long NANOS_PER_MS = 1_000_000L;

   public AutoTotem(Refraction plugin) {
      super(plugin);
   }

   @EventHandler(ignoreCancelled = true)
   public void onDamage(EntityDamageEvent event) {
      if (event.getEntity() instanceof Player) {
         PlayerData data = getData((Player) event.getEntity());
         if (data != null) {
            data.lastDamageTaken = System.nanoTime();
         }
      }
   }

   @EventHandler
   public void onInventoryOpen(InventoryOpenEvent event) {
      if (event.getPlayer() instanceof Player) {
         PlayerData data = getData((Player) event.getPlayer());
         if (data != null) {
            data.lastInventoryOpen = System.nanoTime();
         }
      }
   }

   @EventHandler
   public void onInventoryClose(InventoryCloseEvent event) {
      CachedConfig.AutoTotem cfg = plugin.getCachedConfig().getAutoTotem();
      if (!cfg.enabled) return;
      if (!(event.getPlayer() instanceof Player)) return;
      Player player = (Player) event.getPlayer();
      PlayerData data = getData(player);
      if (data == null) return;

      long nowNanos = System.nanoTime();
      long diffMs = (nowNanos - data.lastTotemMove) / NANOS_PER_MS;
      if (diffMs < cfg.minCloseDelay) {
         String msg = CachedConfig.replace(cfg.messageClose, "delay", diffMs);
         alert(player, msg);
      }
   }

   @EventHandler(ignoreCancelled = true)
   public void onInventoryClick(InventoryClickEvent event) {
      CachedConfig.AutoTotem cfg = plugin.getCachedConfig().getAutoTotem();
      if (!cfg.enabled) return;
      if (!(event.getWhoClicked() instanceof Player)) return;
      Player player = (Player) event.getWhoClicked();
      PlayerData data = getData(player);
      if (data == null) return;

      InventoryType type = event.getInventory().getType();
      boolean isPlayerInventory = type == InventoryType.CRAFTING || type == InventoryType.PLAYER;

      boolean isTotem = (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.TOTEM_OF_UNDYING)
              || (event.getCursor() != null && event.getCursor().getType() == Material.TOTEM_OF_UNDYING);
      if (!isTotem) return;

      long nowNanos = System.nanoTime();

      if (!isPlayerInventory) {
         long openToClickMs = (nowNanos - data.lastInventoryOpen) / NANOS_PER_MS;
         if (openToClickMs < 10000 && openToClickMs < cfg.minOpenDelay) {
            String msg = CachedConfig.replace(cfg.messageOpenClick, "delay", openToClickMs);
            alert(player, msg);
         }
      }

      long reactionMs = (nowNanos - data.lastDamageTaken) / NANOS_PER_MS;
      if (reactionMs < cfg.minReactionDelay) {
         String msg = CachedConfig.replace(cfg.messageReaction, "delay", reactionMs);
         alert(player, msg);
      }

      if ((nowNanos - data.lastDamageTaken) / NANOS_PER_MS < 2000L) {
         data.totemDelays.add(reactionMs);
         if (data.totemDelays.size() > cfg.samples) {
            data.totemDelays.removeFirst();
         }
         if (data.totemDelays.size() == cfg.samples) {
            long maxDelay = Collections.max(data.totemDelays);
            long minDelay = Collections.min(data.totemDelays);
            long variance = maxDelay - minDelay;
            if (variance <= cfg.maxVariance) {
               String msg = CachedConfig.replace(CachedConfig.replace(cfg.messageConsistency, "variance", variance), "samples", cfg.samples);
               alert(player, msg);
               data.totemDelays.clear();
            }
         }
      }

      data.lastTotemMove = nowNanos;
   }
}
