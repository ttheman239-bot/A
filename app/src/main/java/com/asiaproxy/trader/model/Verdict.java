package com.asiaproxy.trader.model;

public class Verdict {
    public enum Tone { BULL, BEAR, WARN, NEUTRAL }

    public final String headline;
    public final String reason;
    public final int reactionScore;     // 0..100
    public final Tone tone;

    public Verdict(String headline, String reason, int reactionScore, Tone tone) {
        this.headline = headline;
        this.reason = reason;
        this.reactionScore = reactionScore;
        this.tone = tone;
    }
}
