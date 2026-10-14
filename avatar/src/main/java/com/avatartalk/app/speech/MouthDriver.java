package com.avatartalk.app.speech;

/**
 * Produces a mouth-open value in 0..1 that fakes speech. When isSpeaking is true,
 * it oscillates between short closed/open phases at ~5 openings per second with
 * random amplitude so it doesn't look robotic. When not speaking it decays to 0.
 */
public final class MouthDriver {

    private volatile boolean speaking = false;
    private final long bootMs = System.currentTimeMillis();
    private float last = 0f;
    private float target = 0f;
    private long nextSwitchMs = 0L;

    public void setSpeaking(boolean v) {
        this.speaking = v;
        if (!v) {
            nextSwitchMs = 0L;
            target = 0f;
        }
    }

    public boolean isSpeaking() { return speaking; }

    /** Called every frame; returns 0..1. */
    public float sample() {
        long t = System.currentTimeMillis();

        if (speaking) {
            if (t >= nextSwitchMs) {
                // Pick a new target with mixed phoneme-like amplitude
                double r = Math.random();
                if (r < 0.18) target = 0.08f + (float) Math.random() * 0.1f;        // closed consonant
                else if (r < 0.6) target = 0.35f + (float) Math.random() * 0.3f;    // mid vowel
                else target = 0.65f + (float) Math.random() * 0.3f;                 // open "ah"
                long gap = 80L + (long)(Math.random() * 160L);  // 5-12 Hz
                nextSwitchMs = t + gap;
            }
        } else {
            target = 0f;
        }

        // Smooth toward target
        float step = speaking ? 0.35f : 0.15f;
        last += (target - last) * step;
        if (last < 0f) last = 0f;
        if (last > 1f) last = 1f;
        return last;
    }
}
