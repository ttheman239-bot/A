package com.asiaproxy.trader.engine;

import android.content.Context;
import android.content.SharedPreferences;

import com.asiaproxy.trader.model.JournalEntry;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public final class JournalStore {

    private static final String PREF = "journal_v1";
    private static final String KEY = "entries";
    private static final String SEP_LINE = "";   // record sep
    private static final String SEP_FIELD = "";  // field sep

    private final SharedPreferences prefs;

    public JournalStore(Context ctx) {
        this.prefs = ctx.getApplicationContext().getSharedPreferences(PREF, Context.MODE_PRIVATE);
    }

    public List<JournalEntry> all() {
        String raw = prefs.getString(KEY, "");
        List<JournalEntry> out = new ArrayList<>();
        if (raw.length() == 0) return out;
        String[] rows = raw.split(SEP_LINE);
        for (String r : rows) {
            if (r.length() == 0) continue;
            String[] f = r.split(SEP_FIELD, -1);
            if (f.length < 5) continue;
            long ts = Long.parseLong(f[0]);
            out.add(new JournalEntry(ts, f[1], f[2], f[3], f[4]));
        }
        // newest first
        Collections.sort(out, new Comparator<JournalEntry>() {
            @Override public int compare(JournalEntry a, JournalEntry b) {
                return Long.compare(b.timestamp, a.timestamp);
            }
        });
        return out;
    }

    public void add(String proxy, String us, String verdict) {
        List<JournalEntry> existing = all();
        long ts = System.currentTimeMillis();
        String label = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.US).format(new Date(ts));
        existing.add(0, new JournalEntry(ts, label, proxy, us, verdict));
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < existing.size(); i++) {
            JournalEntry e = existing.get(i);
            if (i > 0) sb.append(SEP_LINE);
            sb.append(e.timestamp).append(SEP_FIELD)
                    .append(safe(e.dateLabel)).append(SEP_FIELD)
                    .append(safe(e.proxy)).append(SEP_FIELD)
                    .append(safe(e.us)).append(SEP_FIELD)
                    .append(safe(e.verdict));
        }
        prefs.edit().putString(KEY, sb.toString()).apply();
    }

    public void clear() {
        prefs.edit().remove(KEY).apply();
    }

    private static String safe(String s) {
        if (s == null) return "";
        return s.replace(SEP_LINE, " ").replace(SEP_FIELD, " ");
    }
}
