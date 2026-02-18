package me.mikey.refraction.data;

import java.util.HashMap;
import java.util.LinkedList;
import java.util.Map;
import java.util.UUID;
import org.bukkit.Location;

public class PlayerData {
   private final UUID uuid;
   public long lastCrystalPlace = 0L;
   public long lastCrystalBreak = 0L;
   public Location lastCrystalLocation = null;
   public LinkedList<Long> crystalBreakDelays = new LinkedList<>();
   public long lastAnchorPlace = 0L;
   public long lastInventoryOpen = 0L;
   public long lastDamageTaken = 0L;
   public long lastTotemMove = 0L;
   public LinkedList<Long> anchorDelays = new LinkedList<>();
   public LinkedList<Long> crystalDelays = new LinkedList<>();
   public LinkedList<Long> totemDelays = new LinkedList<>();
   public boolean alertsEnabled = true;
   private final Map<String, Integer> violations = new HashMap<>();

   public PlayerData(UUID uuid) {
      this.uuid = uuid;
   }

   public UUID getUuid() {
      return this.uuid;
   }

   public int getViolations(String checkName) {
      return violations.getOrDefault(checkName, 0);
   }

   public int incrementViolations(String checkName) {
      int count = violations.getOrDefault(checkName, 0) + 1;
      violations.put(checkName, count);
      return count;
   }

   public void resetViolations(String checkName) {
      violations.put(checkName, 0);
   }

   public void resetAllViolations() {
      violations.clear();
   }
}
