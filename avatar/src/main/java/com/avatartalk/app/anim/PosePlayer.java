package com.avatartalk.app.anim;

/**
 * Owns the current rendered pose. Blends from a base pose toward a gesture target,
 * then returns to rest. Idle (breathing, blinks, head sway) is layered on top each frame.
 */
public final class PosePlayer {

    private final Pose rest = new Pose();
    private final Pose from = new Pose();
    private final Pose to = new Pose();
    private final Pose current = new Pose();

    private long gestureStartMs = 0L;
    private long gestureDurMs = 0L;
    private long gestureHoldEndMs = 0L;
    private long gestureReleaseDurMs = 0L;
    private boolean gestureReleasing = false;
    private boolean inGesture = false;

    private float mouthDrive = 0f;
    private boolean mouthMuted = false;

    private final long bootMs = System.currentTimeMillis();

    private long nextBlinkMs = 0L;
    private long blinkEndMs = 0L;

    public PosePlayer() {
        rest.setToRest();
        from.copyFrom(rest);
        to.copyFrom(rest);
        current.copyFrom(rest);
        scheduleNextBlink(2500L);
    }

    public Pose currentPose() { return current; }

    public void setMouthMuted(boolean muted) { this.mouthMuted = muted; }
    public boolean isMouthMuted() { return mouthMuted; }

    public void setMouthDrive(float v) {
        if (v < 0) v = 0; else if (v > 1) v = 1;
        this.mouthDrive = v;
    }

    public void playGesture(GestureLibrary.Gesture g) {
        if (g == null) return;
        from.copyFrom(current);
        to.copyFrom(g.target);
        gestureStartMs = now();
        gestureDurMs = g.durationMs;
        gestureHoldEndMs = gestureStartMs + g.durationMs + g.holdMs;
        gestureReleaseDurMs = g.oneShot ? Math.max(220L, g.durationMs - 60L) : 0L;
        gestureReleasing = false;
        inGesture = true;
    }

    public Pose tick() {
        long t = now();

        if (!inGesture) {
            current.copyFrom(rest);
        } else if (t < gestureStartMs + gestureDurMs) {
            float u = (t - gestureStartMs) / (float) gestureDurMs;
            Pose.lerp(from, to, easeOutCubic(u), current);
        } else if (t < gestureHoldEndMs) {
            current.copyFrom(to);
        } else if (gestureReleaseDurMs > 0) {
            if (!gestureReleasing) {
                gestureReleasing = true;
                from.copyFrom(current);
                gestureStartMs = t;
                gestureDurMs = gestureReleaseDurMs;
                gestureHoldEndMs = t;
            }
            long rel = t - gestureStartMs;
            if (rel >= gestureReleaseDurMs) {
                current.copyFrom(rest);
                inGesture = false;
                gestureReleasing = false;
            } else {
                float u = rel / (float) gestureReleaseDurMs;
                Pose.lerp(from, rest, easeInOutQuad(u), current);
            }
        } else {
            inGesture = false;
            current.copyFrom(to);
        }

        float ts = (t - bootMs) / 1000f;

        float breathe = 0.5f + 0.5f * (float) Math.sin(ts * 2.0f * Math.PI / 4.0f);
        current.torsoBreathe = Math.max(current.torsoBreathe, breathe * 0.35f);

        current.headYaw   += (float) Math.sin(ts * 0.7f) * 2.5f;
        current.headPitch += (float) Math.sin(ts * 0.5f + 1.2f) * 1.5f;

        if (t >= nextBlinkMs && blinkEndMs == 0L) {
            blinkEndMs = t + 160L;
        }
        if (blinkEndMs > 0L) {
            if (t >= blinkEndMs) {
                current.eyeLidL = Math.max(current.eyeLidL, 0f);
                current.eyeLidR = Math.max(current.eyeLidR, 0f);
                blinkEndMs = 0L;
                scheduleNextBlink(2500L + (long)(Math.random() * 3000L));
            } else {
                long dur = 160L;
                long rel = dur - (blinkEndMs - t);
                float u = rel / (float) dur;
                float lid = u < 0.5f ? (u * 2f) : (1f - (u - 0.5f) * 2f);
                current.eyeLidL = Math.max(current.eyeLidL, lid);
                current.eyeLidR = Math.max(current.eyeLidR, lid);
            }
        }

        if (!mouthMuted) {
            current.mouthOpen = Math.max(current.mouthOpen, mouthDrive);
        }

        return current;
    }

    private void scheduleNextBlink(long baseMs) {
        nextBlinkMs = now() + baseMs;
    }

    private static long now() { return System.currentTimeMillis(); }

    private static float easeOutCubic(float u) {
        if (u < 0) u = 0; else if (u > 1) u = 1;
        float inv = 1f - u;
        return 1f - inv * inv * inv;
    }

    private static float easeInOutQuad(float u) {
        if (u < 0) u = 0; else if (u > 1) u = 1;
        return u < 0.5f ? 2f * u * u : 1f - (float) Math.pow(-2f * u + 2f, 2) / 2f;
    }
}
