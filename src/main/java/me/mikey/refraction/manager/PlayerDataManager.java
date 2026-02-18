package me.mikey.refraction.manager;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import me.mikey.refraction.Refraction;
import me.mikey.refraction.data.PlayerData;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerQuitEvent;

public class PlayerDataManager implements Listener {
   private final Map<UUID, PlayerData> playerDataMap = new HashMap<>();

   public PlayerDataManager(Refraction plugin) {
      Bukkit.getPluginManager().registerEvents(this, plugin);
      for (Player player : Bukkit.getOnlinePlayers()) {
         this.add(player.getUniqueId());
      }
   }

   @EventHandler
   public void onJoin(PlayerJoinEvent event) {
      this.add(event.getPlayer().getUniqueId());
   }

   @EventHandler
   public void onQuit(PlayerQuitEvent event) {
      this.remove(event.getPlayer().getUniqueId());
   }

   public void add(UUID uuid) {
      this.playerDataMap.put(uuid, new PlayerData(uuid));
   }

   public void remove(UUID uuid) {
      this.playerDataMap.remove(uuid);
   }

   public PlayerData get(Player player) {
      return this.playerDataMap.get(player.getUniqueId());
   }

   public void resetAllViolations() {
      for (PlayerData data : playerDataMap.values()) {
         data.resetAllViolations();
      }
   }
}
