package com.asiaproxy.trader.engine;

import com.asiaproxy.trader.model.Catalyst;
import com.asiaproxy.trader.model.MarketSession;
import com.asiaproxy.trader.model.Pair;
import com.asiaproxy.trader.model.Verdict;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.TimeZone;

public final class RecommendationEngine {

    private RecommendationEngine() {}

    public static class Snapshot {
        public final Calendar bangkok;
        public final List<MarketSession> openSessions;
        public final List<Catalyst> activeCatalysts;
        public final List<String> watchlistTickers;
        public final List<String> usFocusTickers;
        public final Verdict verdict;
        public final String marketStatusLabel;

        Snapshot(Calendar bangkok, List<MarketSession> open, List<Catalyst> active,
                 List<String> watch, List<String> usFocus,
                 Verdict v, String statusLabel) {
            this.bangkok = bangkok;
            this.openSessions = open;
            this.activeCatalysts = active;
            this.watchlistTickers = watch;
            this.usFocusTickers = usFocus;
            this.verdict = v;
            this.marketStatusLabel = statusLabel;
        }
    }

    public static Snapshot snapshot() {
        Calendar bkk = Calendar.getInstance(TimeZone.getTimeZone("Asia/Bangkok"));
        int minOfDay = bkk.get(Calendar.HOUR_OF_DAY) * 60 + bkk.get(Calendar.MINUTE);
        int dayOfMonth = bkk.get(Calendar.DAY_OF_MONTH);
        int dow = bkk.get(Calendar.DAY_OF_WEEK); // 1=Sun..7=Sat

        List<MarketSession> open = new ArrayList<>();
        List<String> watch = new ArrayList<>();
        List<String> usFocus = new ArrayList<>();
        for (MarketSession s : Catalog.sessions()) {
            if (s.isOpenAt(minOfDay)) {
                open.add(s);
                for (String t : s.proxyTickers) addUnique(watch, t);
                for (String t : s.usTickers) addUnique(usFocus, t);
            }
        }

        List<Catalyst> active = new ArrayList<>();
        for (Catalyst c : Catalog.catalysts()) {
            if (c.isLikelyToday(dayOfMonth)) active.add(c);
        }

        Verdict v = computeVerdict(minOfDay, dow, open, active);
        String statusLabel = statusLabel(open, dow);

        return new Snapshot(bkk, open, active, watch, usFocus, v, statusLabel);
    }

    private static void addUnique(List<String> dst, String t) {
        if (!dst.contains(t)) dst.add(t);
    }

    private static String statusLabel(List<MarketSession> open, int dow) {
        boolean weekend = (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY);
        if (open.isEmpty()) {
            return weekend ? "Weekend · Markets closed" : "Quiet window · No major Asia/US session";
        }
        StringBuilder sb = new StringBuilder("Live: ");
        for (int i = 0; i < open.size(); i++) {
            if (i > 0) sb.append(" · ");
            sb.append(open.get(i).name);
        }
        return sb.toString();
    }

    private static Verdict computeVerdict(int minOfDay, int dow,
                                          List<MarketSession> open, List<Catalyst> active) {
        boolean weekend = (dow == Calendar.SATURDAY || dow == Calendar.SUNDAY);
        if (weekend) {
            return new Verdict(
                "Weekend · Plan, don't trade",
                "Markets closed. Use today to review your journal, mark catalysts for the week, and update your TradingView layouts. Setup for Monday's HK + ADR open.",
                25, Verdict.Tone.NEUTRAL);
        }

        boolean fridayLate = (dow == Calendar.FRIDAY && minOfDay >= 22 * 60);
        if (fridayLate) {
            return new Verdict(
                "Late Friday · Avoid new entries",
                "Liquidity thins after 22:00 Friday ICT. Close runners or hedge weekend gap risk. Wait for Monday confirmation.",
                30, Verdict.Tone.WARN);
        }

        boolean tier_a_open = false;
        for (MarketSession s : open) if ("A".equals(s.tier)) tier_a_open = true;
        boolean catalystToday = !active.isEmpty();

        // 07:00–08:00 Korea/Japan window
        if (minOfDay >= 7 * 60 && minOfDay < 8 * 60) {
            return new Verdict(
                "Watch Korea / Japan opens",
                "SK Hynix (000660), Samsung (005930), Tokyo Electron (8035). Use this as the earliest read on memory and equipment names — sets tone for NVDA / MU later.",
                55, Verdict.Tone.NEUTRAL);
        }

        // 08:00–12:30 TWSE
        if (minOfDay >= 8 * 60 && minOfDay < 12 * 60 + 30) {
            String reason = "Taiwan is the strongest Asia signal for US semis. Watch TSMC 2330 and Foxconn 2317 — moves > 2% on volume usually carry into NVDA / AAPL pre-market.";
            if (catalystToday) reason += " Catalyst today raises conviction.";
            return new Verdict(
                "TWSE live · prime semi setup",
                reason,
                catalystToday ? 78 : 65, Verdict.Tone.BULL);
        }

        // 08:30–15:00 HKEX overlap
        if (minOfDay >= 8 * 60 + 30 && minOfDay < 15 * 60) {
            return new Verdict(
                "HKEX live · China + Macau tape",
                "BYD 1211, Alibaba 9988, Wynn Macau 1128. Big moves in HK feed BABA / TSLA / WYNN ADRs at the US open.",
                catalystToday ? 75 : 60, Verdict.Tone.BULL);
        }

        // 15:00–20:30 US pre-market
        if (minOfDay >= 15 * 60 && minOfDay < 20 * 60 + 30) {
            String reason = "Asia closes have printed. Confirm with ES / NQ futures and pre-market volume on the tickers above. Wait at least 15-30 min after the catalyst before entering.";
            return new Verdict(
                "Post-Asia · pre-market window",
                reason,
                catalystToday ? 72 : 50, Verdict.Tone.BULL);
        }

        // 20:30+ US open
        if (minOfDay >= 20 * 60 + 30) {
            return new Verdict(
                "US session live · execute the plan",
                "If your Asia thesis is confirmed by the open, trade size with stops. If divergence (Asia bullish but US weak), trim aggressively.",
                catalystToday ? 70 : 55, Verdict.Tone.BULL);
        }

        // overnight quiet
        return new Verdict(
            "Quiet window · prep, don't chase",
            "No major Asia or US session live. Mark levels, scan overnight news, set alerts on TradingView for when TWSE / HKEX open.",
            30, Verdict.Tone.NEUTRAL);
    }

    public static List<Pair> rankedPairsForToday(int dayOfMonth) {
        List<Pair> out = new ArrayList<>(Catalog.pairs());
        // Bias higher tier first; bonus for catalyst day match
        Collections.sort(out, new Comparator<Pair>() {
            @Override public int compare(Pair a, Pair b) {
                return Integer.compare(b.tier, a.tier);
            }
        });
        return out;
    }
}
