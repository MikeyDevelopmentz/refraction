package me.mikey.refraction.config;

import java.util.logging.Logger;
import org.bukkit.configuration.file.FileConfiguration;

public class CachedConfig {

   private static final int MIN_SAMPLES = 1;
   private static final int MAX_SAMPLES = 100;
   private static final int MIN_THRESHOLD = 1;
   private static final int MAX_THRESHOLD = 100;
   private static final long MIN_DELAY = 0L;

   private int violationThreshold = 3;
   private boolean debugLogging = false;

   private final FastCrystal fastCrystal = new FastCrystal();
   private final AutoAnchor autoAnchor = new AutoAnchor();
   private final AutoTotem autoTotem = new AutoTotem();

   public void reload(FileConfiguration config) {
      reload(config, null);
   }

   public void reload(FileConfiguration config, Logger logger) {
      int rawThreshold = config.getInt("violation-threshold", 3);
      violationThreshold = clamp(rawThreshold, MIN_THRESHOLD, MAX_THRESHOLD);
      if (logger != null && rawThreshold != violationThreshold) {
         logger.warning("Refraction: violation-threshold clamped from " + rawThreshold + " to " + violationThreshold);
      }
      debugLogging = config.getBoolean("debug.logging", false);

      fastCrystal.reload(config, logger);
      autoAnchor.reload(config, logger);
      autoTotem.reload(config, logger);
   }

   private static int clamp(int value, int min, int max) {
      if (value < min) return min;
      return Math.min(value, max);
   }

   private static long clampLong(long value, long min) {
      return value < min ? min : value;
   }

   public int getViolationThreshold() { return violationThreshold; }
   public boolean isDebugLogging() { return debugLogging; }
   public FastCrystal getFastCrystal() { return fastCrystal; }
   public AutoAnchor getAutoAnchor() { return autoAnchor; }
   public AutoTotem getAutoTotem() { return autoTotem; }

   public static String replace(String template, String placeholder, Object value) {
      return template.replace("<" + placeholder + ">", String.valueOf(value));
   }

   public static final class FastCrystal {
      public boolean enabled = true;
      public long delay = 50L;
      public long minBreakDelay = 50L;
      public int samples = 10;
      public int breakSamples = 10;
      public long maxVariance = 1L;
      public long breakMaxVariance = 1L;
      public String messagePlace = "place: <delay>ms";
      public String messagePlaceConsistency = "place consistency: <variance>ms variance over <samples> samples";
      public String messageBreak = "break: <delay>ms";
      public String messageBreakConsistency = "break consistency: <variance>ms variance over <samples> samples";

      void reload(FileConfiguration config, Logger logger) {
         enabled = config.getBoolean("checks.fast-crystal.enabled", true);
         delay = clampLong(config.getLong("checks.fast-crystal.delay", 50L), MIN_DELAY);
         minBreakDelay = clampLong(config.getLong("checks.fast-crystal.min-break-delay", 50L), MIN_DELAY);
         int rawSamples = config.getInt("checks.fast-crystal.samples", 10);
         samples = clamp(rawSamples, MIN_SAMPLES, MAX_SAMPLES);
         if (logger != null && rawSamples != samples) logger.warning("Refraction: fast-crystal.samples clamped to " + samples);
         int rawBreakSamples = config.getInt("checks.fast-crystal.break-samples", 10);
         breakSamples = clamp(rawBreakSamples, MIN_SAMPLES, MAX_SAMPLES);
         if (logger != null && rawBreakSamples != breakSamples) logger.warning("Refraction: fast-crystal.break-samples clamped to " + breakSamples);
         maxVariance = clampLong(config.getLong("checks.fast-crystal.max-variance", 1L), MIN_DELAY);
         breakMaxVariance = clampLong(config.getLong("checks.fast-crystal.break-max-variance", 1L), MIN_DELAY);
         messagePlace = config.getString("messages.fast-crystal.place", messagePlace);
         messagePlaceConsistency = config.getString("messages.fast-crystal.place-consistency", messagePlaceConsistency);
         messageBreak = config.getString("messages.fast-crystal.break", messageBreak);
         messageBreakConsistency = config.getString("messages.fast-crystal.break-consistency", messageBreakConsistency);
      }
   }

   public static final class AutoAnchor {
      public boolean enabled = true;
      public long chargeDelay = 50L;
      public long voidAfterMs = 150L;
      public long suspiciousMs = 100L;
      public int suspiciousCount = 5;
      public long maxVariance = 1L;
      public int samples = 10;
      public boolean logCharges = true;
      public boolean logBlowups = true;
      public String messageCharge = "charge: <delay>ms";
      public String messageChargeConsistency = "charge consistency: <variance>ms variance over <samples> samples";
      public String messageSuspicious = "consistent sub-<threshold>ms charges (<count> under <threshold>ms)";

      void reload(FileConfiguration config, Logger logger) {
         enabled = config.getBoolean("checks.auto-anchor.enabled", true);
         chargeDelay = clampLong(config.getLong("checks.auto-anchor.charge-delay", 50L), MIN_DELAY);
         voidAfterMs = clampLong(config.getLong("checks.auto-anchor.void-after-ms", 150L), MIN_DELAY);
         suspiciousMs = clampLong(config.getLong("checks.auto-anchor.suspicious-charge-ms", 100L), MIN_DELAY);
         suspiciousCount = clamp(config.getInt("checks.auto-anchor.suspicious-charge-count", 5), 1, 50);
         maxVariance = clampLong(config.getLong("checks.auto-anchor.max-variance", 1L), MIN_DELAY);
         samples = clamp(config.getInt("checks.auto-anchor.samples", 10), MIN_SAMPLES, MAX_SAMPLES);
         logCharges = config.getBoolean("checks.auto-anchor.log-charges", true);
         logBlowups = config.getBoolean("checks.auto-anchor.log-blowups", true);
         messageCharge = config.getString("messages.auto-anchor.charge", messageCharge);
         messageChargeConsistency = config.getString("messages.auto-anchor.charge-consistency", messageChargeConsistency);
         messageSuspicious = config.getString("messages.auto-anchor.suspicious", messageSuspicious);
      }
   }

   public static final class AutoTotem {
      public boolean enabled = true;
      public long minOpenDelay = 100L;
      public long minReactionDelay = 150L;
      public long minCloseDelay = 60L;
      public long maxVariance = 1L;
      public int samples = 5;
      public String messageClose = "Instant Close: <delay>ms";
      public String messageOpenClick = "Instant Open-Click: <delay>ms";
      public String messageReaction = "Fast Reaction: <delay>ms";
      public String messageConsistency = "Consistency: <variance>ms variance over <samples> samples";

      void reload(FileConfiguration config, Logger logger) {
         enabled = config.getBoolean("checks.auto-totem.enabled", true);
         minOpenDelay = clampLong(config.getLong("checks.auto-totem.min-open-delay", 100L), MIN_DELAY);
         minReactionDelay = clampLong(config.getLong("checks.auto-totem.min-reaction-delay", 150L), MIN_DELAY);
         minCloseDelay = clampLong(config.getLong("checks.auto-totem.min-close-delay", 60L), MIN_DELAY);
         maxVariance = clampLong(config.getLong("checks.auto-totem.max-variance", 1L), MIN_DELAY);
         samples = clamp(config.getInt("checks.auto-totem.samples", 5), MIN_SAMPLES, MAX_SAMPLES);
         messageClose = config.getString("messages.auto-totem.close", messageClose);
         messageOpenClick = config.getString("messages.auto-totem.open-click", messageOpenClick);
         messageReaction = config.getString("messages.auto-totem.reaction", messageReaction);
         messageConsistency = config.getString("messages.auto-totem.consistency", messageConsistency);
      }
   }
}
