package com.asiaproxy.trader.model;

public class JournalEntry {
    public final long timestamp;
    public final String dateLabel;
    public final String proxy;
    public final String us;
    public final String verdict;

    public JournalEntry(long timestamp, String dateLabel, String proxy, String us, String verdict) {
        this.timestamp = timestamp;
        this.dateLabel = dateLabel;
        this.proxy = proxy;
        this.us = us;
        this.verdict = verdict;
    }
}
