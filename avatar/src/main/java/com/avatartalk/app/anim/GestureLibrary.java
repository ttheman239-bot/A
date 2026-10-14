package com.avatartalk.app.anim;

import java.util.LinkedHashMap;
import java.util.Map;

/** Predefined target poses the user can trigger. Each is a Pose with duration/easing hints. */
public final class GestureLibrary {

    public static final class Gesture {
        public final String id;
        public final String label;
        public final Pose target;
        public final long durationMs;
        public final long holdMs;
        public final boolean oneShot;
        public Gesture(String id, String label, Pose target, long duration, long hold, boolean oneShot) {
            this.id = id; this.label = label; this.target = target;
            this.durationMs = duration; this.holdMs = hold; this.oneShot = oneShot;
        }
    }

    private static final Map<String, Gesture> MAP = new LinkedHashMap<String, Gesture>();

    static {
        add(buildWave());
        add(buildPoint());
        add(buildThumbsUp());
        add(buildClap());
        add(buildHandsUp());
        add(buildAkimbo());
        add(buildArmsCrossed());
        add(buildCount(1));
        add(buildCount(2));
        add(buildCount(3));
        add(buildCount(4));
        add(buildCount(5));
        add(buildRelax());
    }

    private static void add(Gesture g) { MAP.put(g.id, g); }
    public static Gesture get(String id) { return MAP.get(id); }
    public static Map<String, Gesture> all() { return MAP; }

    private static Pose base() { Pose p = new Pose(); p.setToRest(); return p; }

    private static Gesture buildWave() {
        Pose p = base();
        p.rShoulderPitch = 150; p.rShoulderYaw = 25; p.rElbow = 50; p.rWristPitch = -20;
        extendFingers(p.rFingers, 5);
        p.headYaw = 6;
        return new Gesture("wave", "Wave", p, 350, 1600, true);
    }

    private static Gesture buildPoint() {
        Pose p = base();
        p.rShoulderPitch = 95; p.rShoulderYaw = 5; p.rElbow = 10; p.rWristPitch = 0;
        curlFingers(p.rFingers, 80);
        p.rFingers[3] = 0; p.rFingers[4] = 0; p.rFingers[5] = 0;
        p.rFingers[0] = 10; p.rFingers[1] = 60; p.rFingers[2] = 50;
        return new Gesture("point", "Point", p, 300, 1800, true);
    }

    private static Gesture buildThumbsUp() {
        Pose p = base();
        p.rShoulderPitch = 60; p.rShoulderYaw = 20; p.rElbow = 95; p.rWristPitch = 15;
        curlFingers(p.rFingers, 85);
        p.rFingers[0] = -60; p.rFingers[1] = 0; p.rFingers[2] = 0;
        p.mouthWide = 0.6f;
        return new Gesture("thumbs", "Thumbs up", p, 320, 1800, true);
    }

    private static Gesture buildClap() {
        Pose p = base();
        p.lShoulderPitch = 65; p.lShoulderYaw = 25;
        p.rShoulderPitch = 65; p.rShoulderYaw = 25;
        p.lElbow = 95; p.rElbow = 95;
        p.lWristPitch = 5; p.rWristPitch = 5;
        extendFingers(p.lFingers, 2);
        extendFingers(p.rFingers, 2);
        return new Gesture("clap", "Clap", p, 240, 1600, true);
    }

    private static Gesture buildHandsUp() {
        Pose p = base();
        p.lShoulderPitch = 165; p.lShoulderYaw = 15; p.lElbow = 15;
        p.rShoulderPitch = 165; p.rShoulderYaw = 15; p.rElbow = 15;
        p.lWristPitch = -10; p.rWristPitch = -10;
        extendFingers(p.lFingers, 8);
        extendFingers(p.rFingers, 8);
        p.mouthOpen = 0.3f;
        p.browL = 0.6f; p.browR = 0.6f;
        return new Gesture("hands_up", "Hands up", p, 360, 2000, true);
    }

    private static Gesture buildAkimbo() {
        Pose p = base();
        p.lShoulderPitch = 15; p.lShoulderYaw = 40; p.lElbow = 95;
        p.rShoulderPitch = 15; p.rShoulderYaw = 40; p.rElbow = 95;
        p.lWristRoll = 25; p.rWristRoll = 25;
        return new Gesture("akimbo", "Hands on hips", p, 400, 2500, true);
    }

    private static Gesture buildArmsCrossed() {
        Pose p = base();
        p.lShoulderPitch = 70; p.lShoulderYaw = 50; p.lElbow = 110;
        p.rShoulderPitch = 70; p.rShoulderYaw = 50; p.rElbow = 110;
        p.lWristRoll = 15; p.rWristRoll = -15;
        curlFingers(p.lFingers, 20);
        curlFingers(p.rFingers, 20);
        return new Gesture("arms_crossed", "Arms crossed", p, 440, 2500, true);
    }

    private static Gesture buildCount(int n) {
        Pose p = base();
        p.rShoulderPitch = 85; p.rShoulderYaw = 15; p.rElbow = 85; p.rWristPitch = 0;
        curlFingers(p.rFingers, 90);
        if (n >= 1) { p.rFingers[3] = 0; p.rFingers[4] = 0; p.rFingers[5] = 0; }
        if (n >= 2) { p.rFingers[6] = 0; p.rFingers[7] = 0; p.rFingers[8] = 0; }
        if (n >= 3) { p.rFingers[9] = 0; p.rFingers[10] = 0; p.rFingers[11] = 0; }
        if (n >= 4) { p.rFingers[12] = 0; p.rFingers[13] = 0; p.rFingers[14] = 0; }
        if (n >= 5) { p.rFingers[0] = -40; p.rFingers[1] = 0; p.rFingers[2] = 0; }
        return new Gesture("count_" + n, Integer.toString(n), p, 260, 1500, true);
    }

    private static Gesture buildRelax() {
        Pose p = base();
        return new Gesture("relax", "Relax", p, 500, 0, false);
    }

    private static void curlFingers(float[] f, float curl) {
        for (int i = 0; i < 5; i++) {
            int base = i * 3;
            f[base] = (i == 0 ? -5f : curl * 0.6f);
            f[base + 1] = curl * 0.9f;
            f[base + 2] = curl * 0.7f;
        }
    }

    private static void extendFingers(float[] f, float splay) {
        float[] splayDir = {-splay*1.4f, -splay*0.6f, 0f, splay*0.6f, splay*1.2f};
        for (int i = 0; i < 5; i++) {
            int base = i * 3;
            f[base] = splayDir[i];
            f[base + 1] = 2f;
            f[base + 2] = 2f;
        }
    }
}
