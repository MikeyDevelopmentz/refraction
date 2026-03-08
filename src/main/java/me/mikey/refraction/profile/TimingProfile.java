package me.mikey.refraction.profile;

// tracks a rolling window of timing samples for a player and scores new values against their baseline
public class TimingProfile {

    public static final long FLOOR_TICK_PERFECT_MS = 16L;
    public static final int  MIN_CALIBRATION = 20; // samples needed before we start scoring
    public static final int  MAX_SAMPLES = 60;

    private static final double MIN_HUMAN_STDDEV_MS = 5.0; // below this = too consistent to be human

    private final long[] buf = new long[MAX_SAMPLES];
    private int head = 0;
    private int count = 0;

    private boolean dirty = true;
    private double cachedMean;
    private double cachedStdDev;

    public void record(long ms) {
        buf[head] = ms;
        head = (head + 1) % MAX_SAMPLES;
        if (count < MAX_SAMPLES) count++;
        dirty = true;
    }

    public boolean isCalibrated() { return count >= MIN_CALIBRATION; }
    public int sampleCount()      { return count; }
    public double getMean()       { recompute(); return cachedMean; }
    public double getStdDev()     { recompute(); return cachedStdDev; }

    // returns 0-10, higher = more suspicious
    public double score(long ms) {
        if (!isCalibrated()) return 0.0;
        recompute();

        // stddev too low = bot/macro (humans always have some jitter)
        if (cachedStdDev < MIN_HUMAN_STDDEV_MS)
            return Math.min(10.0, 6.0 + (MIN_HUMAN_STDDEV_MS - cachedStdDev));

        // how many stddevs faster than their own average
        double deviations = (cachedMean - ms) / cachedStdDev;
        if (deviations <= 0.0) return 0.0;
        return Math.min(10.0, deviations * 2.5);
    }

    private void recompute() {
        if (!dirty) return;
        double sum = 0;
        for (int i = 0; i < count; i++) sum += buf[i];
        cachedMean = sum / count;
        double varSum = 0;
        for (int i = 0; i < count; i++) {
            double d = buf[i] - cachedMean;
            varSum += d * d;
        }
        cachedStdDev = Math.sqrt(varSum / count);
        dirty = false;
    }
}
