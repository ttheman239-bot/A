package com.asiaproxy.trader.model;

public class Catalyst {
    public final String name;
    public final String window;     // e.g. "1st-3rd of month", "10th"
    public final String impact;     // affected US tickers
    public final int dayStart;
    public final int dayEnd;
    public final String url;

    public Catalyst(String name, String window, String impact,
                    int dayStart, int dayEnd, String url) {
        this.name = name;
        this.window = window;
        this.impact = impact;
        this.dayStart = dayStart;
        this.dayEnd = dayEnd;
        this.url = url;
    }

    public boolean isLikelyToday(int dayOfMonth) {
        return dayOfMonth >= dayStart && dayOfMonth <= dayEnd;
    }
}
