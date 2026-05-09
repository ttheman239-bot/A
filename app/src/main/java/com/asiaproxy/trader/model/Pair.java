package com.asiaproxy.trader.model;

public class Pair {
    public final String id;
    public final String category;       // "Semiconductor", "EV", "China ADR", "Gaming"
    public final String usTicker;
    public final String usName;
    public final String asiaTicker;
    public final String asiaName;
    public final int tier;              // 1..5 (5 = highest)
    public final String catalyst;       // e.g. "TSMC Monthly Revenue (10th)"
    public final String tradingViewUs;
    public final String tradingViewAsia;
    public final String[] notes;        // pre-trade checklist bullets

    public Pair(String id, String category, String usTicker, String usName,
                String asiaTicker, String asiaName, int tier, String catalyst,
                String tradingViewUs, String tradingViewAsia, String[] notes) {
        this.id = id;
        this.category = category;
        this.usTicker = usTicker;
        this.usName = usName;
        this.asiaTicker = asiaTicker;
        this.asiaName = asiaName;
        this.tier = tier;
        this.catalyst = catalyst;
        this.tradingViewUs = tradingViewUs;
        this.tradingViewAsia = tradingViewAsia;
        this.notes = notes;
    }

    public String stars() {
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < tier; i++) b.append('★');
        for (int i = tier; i < 5; i++) b.append('☆');
        return b.toString();
    }

    public String title() {
        return usTicker + "  ↔  " + asiaTicker;
    }
}
