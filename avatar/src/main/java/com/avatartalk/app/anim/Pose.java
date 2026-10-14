package com.avatartalk.app.anim;

/**
 * Full-body humanoid pose. Angles are in degrees.
 *
 * Shoulder pitch: 0 = arm hanging down, +90 = arm forward, +180 = arm straight up.
 * Shoulder yaw:   0 = arm in body plane, +90 = arm reaches sideways out of torso.
 * Elbow:          0 = straight, +120 = fully bent (forearm folded against upper arm).
 * Wrist pitch:    0 = continues forearm line, +/- bends hand up/down.
 * Fingers:        each of 3 segments 0 = straight, +90 = fully curled.
 * mouthOpen:      0 = closed, 1 = wide.
 * eyeLid:         0 = open, 1 = closed.
 */
public final class Pose {
    public float headYaw;
    public float headPitch;
    public float headRoll;
    public float neck;
    public float torsoLean;
    public float torsoBreathe;      // 0..1 scales chest Y

    public float lShoulderPitch;
    public float lShoulderYaw;
    public float lElbow;
    public float lWristPitch;
    public float lWristRoll;

    public float rShoulderPitch;
    public float rShoulderYaw;
    public float rElbow;
    public float rWristPitch;
    public float rWristRoll;

    /** 5 fingers x 3 segments = 15 angles. Index 0..2 = thumb, 3..5 = index, 6..8 = middle, 9..11 = ring, 12..14 = pinky. */
    public final float[] lFingers = new float[15];
    public final float[] rFingers = new float[15];

    public float mouthOpen;         // 0..1
    public float mouthWide;         // 0..1 smile vs oh
    public float eyeLidL;           // 0 open .. 1 closed
    public float eyeLidR;
    public float browL;             // -1..1
    public float browR;

    public Pose() {
        setToRest();
    }

    public void setToRest() {
        headYaw = 0; headPitch = 0; headRoll = 0;
        neck = 0;
        torsoLean = 0; torsoBreathe = 0;

        lShoulderPitch = 8;  lShoulderYaw = 10; lElbow = 15;
        lWristPitch = 0;     lWristRoll = 0;
        rShoulderPitch = 8;  rShoulderYaw = 10; rElbow = 15;
        rWristPitch = 0;     rWristRoll = 0;

        for (int i = 0; i < 15; i++) {
            lFingers[i] = 10f;
            rFingers[i] = 10f;
        }
        // Thumb opposes slightly
        lFingers[0] = -20; lFingers[1] = 10; lFingers[2] = 10;
        rFingers[0] = -20; rFingers[1] = 10; rFingers[2] = 10;

        mouthOpen = 0;
        mouthWide = 0.3f;
        eyeLidL = 0; eyeLidR = 0;
        browL = 0; browR = 0;
    }

    public void copyFrom(Pose other) {
        headYaw = other.headYaw; headPitch = other.headPitch; headRoll = other.headRoll;
        neck = other.neck;
        torsoLean = other.torsoLean; torsoBreathe = other.torsoBreathe;
        lShoulderPitch = other.lShoulderPitch; lShoulderYaw = other.lShoulderYaw;
        lElbow = other.lElbow; lWristPitch = other.lWristPitch; lWristRoll = other.lWristRoll;
        rShoulderPitch = other.rShoulderPitch; rShoulderYaw = other.rShoulderYaw;
        rElbow = other.rElbow; rWristPitch = other.rWristPitch; rWristRoll = other.rWristRoll;
        for (int i = 0; i < 15; i++) {
            lFingers[i] = other.lFingers[i];
            rFingers[i] = other.rFingers[i];
        }
        mouthOpen = other.mouthOpen; mouthWide = other.mouthWide;
        eyeLidL = other.eyeLidL; eyeLidR = other.eyeLidR;
        browL = other.browL; browR = other.browR;
    }

    /** p = lerp(from, to, t) with t clamped to 0..1 */
    public static void lerp(Pose from, Pose to, float t, Pose out) {
        if (t < 0) t = 0; else if (t > 1) t = 1;
        float u = 1f - t;
        out.headYaw   = u*from.headYaw   + t*to.headYaw;
        out.headPitch = u*from.headPitch + t*to.headPitch;
        out.headRoll  = u*from.headRoll  + t*to.headRoll;
        out.neck      = u*from.neck      + t*to.neck;
        out.torsoLean = u*from.torsoLean + t*to.torsoLean;
        out.torsoBreathe = u*from.torsoBreathe + t*to.torsoBreathe;

        out.lShoulderPitch = u*from.lShoulderPitch + t*to.lShoulderPitch;
        out.lShoulderYaw   = u*from.lShoulderYaw   + t*to.lShoulderYaw;
        out.lElbow         = u*from.lElbow         + t*to.lElbow;
        out.lWristPitch    = u*from.lWristPitch    + t*to.lWristPitch;
        out.lWristRoll     = u*from.lWristRoll     + t*to.lWristRoll;

        out.rShoulderPitch = u*from.rShoulderPitch + t*to.rShoulderPitch;
        out.rShoulderYaw   = u*from.rShoulderYaw   + t*to.rShoulderYaw;
        out.rElbow         = u*from.rElbow         + t*to.rElbow;
        out.rWristPitch    = u*from.rWristPitch    + t*to.rWristPitch;
        out.rWristRoll     = u*from.rWristRoll     + t*to.rWristRoll;

        for (int i = 0; i < 15; i++) {
            out.lFingers[i] = u*from.lFingers[i] + t*to.lFingers[i];
            out.rFingers[i] = u*from.rFingers[i] + t*to.rFingers[i];
        }
        out.mouthOpen = u*from.mouthOpen + t*to.mouthOpen;
        out.mouthWide = u*from.mouthWide + t*to.mouthWide;
        out.eyeLidL   = u*from.eyeLidL   + t*to.eyeLidL;
        out.eyeLidR   = u*from.eyeLidR   + t*to.eyeLidR;
        out.browL     = u*from.browL     + t*to.browL;
        out.browR     = u*from.browR     + t*to.browR;
    }
}
