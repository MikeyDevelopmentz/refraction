package me.mikey.refraction.profile;

// score from 0-10, low = clean, high = cheating
// low (0-3.9) doesnt alert, medium (4-6.9) and high (7+) do
public class SuspicionScore {

    public enum Level { LOW, MEDIUM, HIGH }

    private final double value;
    private final Level level;

    public SuspicionScore(double value) {
        this.value = Math.max(0.0, Math.min(10.0, value));
        if (this.value >= 7.0) this.level = Level.HIGH;
        else if (this.value >= 4.0) this.level = Level.MEDIUM;
        else this.level = Level.LOW;
    }

    public double getValue() { return value; }
    public Level getLevel() { return level; }
    public boolean shouldAlert() { return level != Level.LOW; }

    public String format() {
        return String.format("%.1f/10 [%s]", value, level.name());
    }
}
