# Refraction

Crystal PvP anticheat for Paper servers (1.21+).

Monitors end crystal placement/breaking, respawn anchor charging, and totem swapping for inhuman speeds and suspiciously consistent timing.

## Checks

**FastCrystal** - flags players placing or breaking end crystals faster than the configured threshold. tracks delay variance across a sample window to catch automation with unnaturally consistent timing.

**AutoAnchor** - adaptive profile-based anchor detection. builds a baseline of each players charge speed over 20 samples, then flags if they suddenly become way faster than their own average or if their timing is too consistent to be human. ignores sub-tick noise from keybinds, but flags sustained sub-tick streaks (bots spamming charges faster than one server tick).

**AutoTotem** - flags instant totem swaps by measuring inventory open-to-click time, damage reaction time, and close speed. variance tracking catches scripts with robotic consistency.

## Commands

| Command | Permission | Description |
|---|---|---|
| `/refraction reload` | `refraction.reload` | reload config and reset violations |
| `/refraction status` | `refraction.reload` | show current check settings |
| `/refraction debug on/off` | `refraction.debug` | toggle debug logging |
| `/refraction profile <player>` | `refraction.reload` | show a players anchor profile and violation counts |
| `/alerts` | `refraction.alerts` | toggle alert messages for yourself |

## Config

```yaml
checks:
  fast-crystal:
    enabled: true
    delay: 50            # min ms between crystal placements
    min-break-delay: 50  # min ms between crystal breaks
    max-variance: 1      # max ms variance to flag consistency
    samples: 10
  auto-anchor:
    enabled: true
    void-after-ms: 5000          # ignore if its been this long since last charge
    sub-tick-streak-threshold: 5 # consecutive <16ms charges before flagging
    log-charges: true
    log-blowups: true
  auto-totem:
    enabled: true
    min-open-delay: 100
    min-reaction-delay: 150
    min-close-delay: 60
```

## Building

Requires Java 21.

```
mvn clean package
```

Drop the jar into `plugins/`.

## License

MIT
