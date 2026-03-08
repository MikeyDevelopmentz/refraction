package me.mikey.refraction.manager;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import me.mikey.refraction.Refraction;
import me.mikey.refraction.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerDataManager implements Listener {

    private final Map<UUID, PlayerData> dataMap = new ConcurrentHashMap<>();

    public PlayerDataManager(Refraction plugin) {
        Bukkit.getPluginManager().registerEvents(this, plugin);
        for (Player player : Bukkit.getOnlinePlayers()) dataMap.put(player.getUniqueId(), new PlayerData(player.getUniqueId()));
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {
        dataMap.put(event.getPlayer().getUniqueId(), new PlayerData(event.getPlayer().getUniqueId()));
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent event) {
        dataMap.remove(event.getPlayer().getUniqueId());
    }

    public PlayerData get(Player player) {
        return dataMap.get(player.getUniqueId());
    }

    public PlayerData get(UUID uuid) {
        return dataMap.get(uuid);
    }

    public void resetAllViolations() {
        dataMap.values().forEach(PlayerData::resetAllViolations);
    }
}
