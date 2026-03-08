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

    public AutoTotem(Refraction plugin) {
        super(plugin);
    }

    @EventHandler(ignoreCancelled = true)
    public void onDamage(EntityDamageEvent event) {
        if (!(event.getEntity() instanceof Player player)) return;
        PlayerData data = getData(player);
        if (data != null) data.lastDamageTaken = System.nanoTime();
    }

    @EventHandler
    public void onInventoryOpen(InventoryOpenEvent event) {
        if (!(event.getPlayer() instanceof Player player)) return;
        PlayerData data = getData(player);
        if (data != null) data.lastInventoryOpen = System.nanoTime();
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        CachedConfig.AutoTotem cfg = plugin.getCachedConfig().getAutoTotem();
        if (!cfg.enabled) return;
        if (!(event.getPlayer() instanceof Player player)) return;
        PlayerData data = getData(player);
        if (data == null) return;

        long diffMs = (System.nanoTime() - data.lastTotemMove) / NS_PER_MS;
        if (diffMs < cfg.minCloseDelay)
            alert(player, CachedConfig.replace(cfg.messageClose, "delay", diffMs));
    }

    @EventHandler(ignoreCancelled = true)
    public void onInventoryClick(InventoryClickEvent event) {
        CachedConfig.AutoTotem cfg = plugin.getCachedConfig().getAutoTotem();
        if (!cfg.enabled) return;
        if (!(event.getWhoClicked() instanceof Player player)) return;
        PlayerData data = getData(player);
        if (data == null) return;

        InventoryType type = event.getInventory().getType();
        boolean isPlayerInventory = type == InventoryType.CRAFTING || type == InventoryType.PLAYER;

        boolean isTotem = (event.getCurrentItem() != null && event.getCurrentItem().getType() == Material.TOTEM_OF_UNDYING)
            || (event.getCursor() != null && event.getCursor().getType() == Material.TOTEM_OF_UNDYING);
        if (!isTotem) return;

        long now = System.nanoTime();

        if (!isPlayerInventory) {
            long openToClickMs = (now - data.lastInventoryOpen) / NS_PER_MS;
            if (openToClickMs < 10000 && openToClickMs < cfg.minOpenDelay)
                alert(player, CachedConfig.replace(cfg.messageOpenClick, "delay", openToClickMs));
        }

        long reactionMs = (now - data.lastDamageTaken) / NS_PER_MS;
        if (reactionMs < cfg.minReactionDelay)
            alert(player, CachedConfig.replace(cfg.messageReaction, "delay", reactionMs));

        if ((now - data.lastDamageTaken) / NS_PER_MS < 2000L) {
            data.totemDelays.add(reactionMs);
            if (data.totemDelays.size() > cfg.samples) data.totemDelays.removeFirst();
            if (data.totemDelays.size() == cfg.samples) {
                long variance = Collections.max(data.totemDelays) - Collections.min(data.totemDelays);
                if (variance <= cfg.maxVariance) {
                    alert(player, CachedConfig.replace(
                        CachedConfig.replace(cfg.messageConsistency, "variance", variance), "samples", cfg.samples));
                    data.totemDelays.clear();
                }
            }
        }

        data.lastTotemMove = now;
    }
}
