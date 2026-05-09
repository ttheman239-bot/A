package com.asiaproxy.trader.model;

public class MarketSession {
    public final String name;
    public final String region;
    public final int openMin;   // minutes from midnight, Bangkok time
    public final int closeMin;
    public final String tier;   // "A" / "B"
    public final String[] proxyTickers;
    public final String[] usTickers;

    public MarketSession(String name, String region, int openMin, int closeMin,
                         String tier, String[] proxyTickers, String[] usTickers) {
        this.name = name;
        this.region = region;
        this.openMin = openMin;
        this.closeMin = closeMin;
        this.tier = tier;
        this.proxyTickers = proxyTickers;
        this.usTickers = usTickers;
    }

    public boolean isOpenAt(int minOfDay) {
        return minOfDay >= openMin && minOfDay < closeMin;
    }

    public String hours() {
        return fmt(openMin) + " – " + fmt(closeMin);
    }

    private static String fmt(int m) {
        int h = m / 60, mm = m % 60;
        return (h < 10 ? "0" : "") + h + ":" + (mm < 10 ? "0" : "") + mm;
    }
}
