# Refraction

Crystal PvP anticheat for Paper and Folia servers (1.20+).

Monitors end crystal placement/breaking, respawn anchor charging, and totem swapping for inhuman speeds and suspiciously consistent timing patterns.

## Checks

**FastCrystal** - Flags players placing or breaking end crystals faster than the configured threshold. Tracks delay variance across a sample window to catch automation that produces unnaturally consistent timing.

**AutoAnchor** - Flags fast anchor charges after placement. Detects repeated sub-threshold charge times and low-variance patterns across samples. Optional debug logging for individual charge/blowup events.

**AutoTotem** - Flags instant totem swaps by measuring inventory open-to-click time, damage-to-swap reaction time, and inventory close speed. Variance tracking catches scripts that produce robotic consistency.

All checks use `System.nanoTime()` for precision. Each check has a configurable violation threshold before alerts fire.

## Commands

| Command | Permission | Description |
|---|---|---|
| `/refraction reload` | `refraction.reload` | Reload config and reset violations |
| `/refraction status` | `refraction.reload` | Show current check configuration |
| `/refraction debug on/off` | `refraction.debug` | Toggle debug logging |
| `/alerts` | `refraction.alerts` | Toggle alert messages for yourself |

## Config

Thresholds, sample sizes, variance limits, and alert formats are all configurable in `config.yml`. Messages use MiniMessage formatting.

```yaml
checks:
  fast-crystal:
    enabled: true
    delay: 50            # min ms between crystal placements
    min-break-delay: 50  # min ms between crystal breaks
    max-variance: 1      # max ms variance to flag consistency
    samples: 10          # sample window size
  auto-anchor:
    enabled: true
    charge-delay: 50
    void-after-ms: 150   # ignore charges older than this after placement
    suspicious-charge-ms: 100
    suspicious-charge-count: 5
  auto-totem:
    enabled: true
    min-open-delay: 100
    min-reaction-delay: 150
    min-close-delay: 60
```

## Building

Requires Java 17+.

```
mvn clean package
```

Drop the jar into your `plugins/` folder.

## License

MIT
