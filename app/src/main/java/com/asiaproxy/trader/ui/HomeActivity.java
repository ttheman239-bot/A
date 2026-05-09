package com.asiaproxy.trader.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.asiaproxy.trader.R;
import com.asiaproxy.trader.engine.Catalog;
import com.asiaproxy.trader.engine.RecommendationEngine;
import com.asiaproxy.trader.model.Catalyst;
import com.asiaproxy.trader.model.MarketSession;
import com.asiaproxy.trader.model.QuickLink;
import com.asiaproxy.trader.model.Verdict;
import com.asiaproxy.trader.util.Views;

import java.text.SimpleDateFormat;
import java.util.Locale;

public class HomeActivity extends Activity {

    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tick = new Runnable() {
        @Override public void run() {
            renderSafe();
            handler.postDelayed(this, 30000L);
        }
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        try {
            setContentView(R.layout.activity_home);
        } catch (Throwable t) {
            Toast.makeText(this, "Layout error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        wireNavigation();
        renderSafe();
    }

    @Override protected void onResume() {
        super.onResume();
        handler.post(tick);
    }

    @Override protected void onPause() {
        super.onPause();
        handler.removeCallbacks(tick);
    }

    private void wireNavigation() {
        View refresh = findViewById(R.id.btnRefresh);
        if (refresh != null) {
            refresh.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { renderSafe(); }
            });
        }
        wireNavButton(R.id.navPairs, PairsActivity.class);
        wireNavButton(R.id.navCalendar, CalendarActivity.class);
        wireNavButton(R.id.navRules, RulesActivity.class);
        wireNavButton(R.id.navJournal, JournalActivity.class);
    }

    private void wireNavButton(int id, final Class<?> target) {
        View v = findViewById(id);
        if (v == null) return;
        v.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View view) {
                try {
                    startActivity(new Intent(HomeActivity.this, target));
                } catch (Throwable t) {
                    Toast.makeText(HomeActivity.this, "Open failed: " + t.getMessage(), Toast.LENGTH_SHORT).show();
                }
            }
        });
    }

    private void renderSafe() {
        try {
            render();
        } catch (Throwable t) {
            TextView v = (TextView) findViewById(R.id.txtVerdict);
            if (v != null) v.setText("Render error");
            TextView r = (TextView) findViewById(R.id.txtVerdictReason);
            if (r != null) r.setText(String.valueOf(t));
        }
    }

    private void render() {
        RecommendationEngine.Snapshot s = RecommendationEngine.snapshot();

        SimpleDateFormat clockFmt = new SimpleDateFormat("HH:mm", Locale.US);
        clockFmt.setTimeZone(s.bangkok.getTimeZone());
        SimpleDateFormat dateFmt = new SimpleDateFormat("EEEE, dd MMM yyyy", Locale.ENGLISH);
        dateFmt.setTimeZone(s.bangkok.getTimeZone());

        setTextSafe(R.id.txtClock, clockFmt.format(s.bangkok.getTime()));
        setTextSafe(R.id.txtDate, dateFmt.format(s.bangkok.getTime()) + "  ·  Asia/Bangkok");
        setTextSafe(R.id.pillStatus, s.marketStatusLabel);
        setTextSafe(R.id.txtVerdict, s.verdict.headline);
        setTextSafe(R.id.txtVerdictReason, s.verdict.reason);
        setTextSafe(R.id.txtReactionScore, "Reaction Score: " + s.verdict.reactionScore + " / 100");

        TextView scoreView = (TextView) findViewById(R.id.txtReactionScore);
        if (scoreView != null) applyTone(scoreView, s.verdict.tone);

        renderWatchlist(s);
        renderUsFocus(s);
        renderCatalysts(s);
        renderQuickLinks();

        SimpleDateFormat upd = new SimpleDateFormat("HH:mm:ss", Locale.US);
        upd.setTimeZone(s.bangkok.getTimeZone());
        setTextSafe(R.id.txtUpdated, "Updated " + upd.format(s.bangkok.getTime()) + " · auto-refresh 30s");
    }

    private void setTextSafe(int id, String value) {
        TextView v = (TextView) findViewById(id);
        if (v != null) v.setText(value);
    }

    private void applyTone(TextView pill, Verdict.Tone tone) {
        int bg;
        switch (tone) {
            case BULL: bg = R.drawable.bg_pill_bull; break;
            case BEAR: bg = R.drawable.bg_pill_bear; break;
            case WARN: bg = R.drawable.bg_pill_warn; break;
            default:    bg = R.drawable.bg_pill_muted; break;
        }
        pill.setBackgroundResource(bg);
    }

    private void renderWatchlist(RecommendationEngine.Snapshot s) {
        LinearLayout list = (LinearLayout) findViewById(R.id.listWatchlist);
        if (list == null) return;
        list.removeAllViews();
        if (s.openSessions.isEmpty()) {
            list.addView(Views.make(this, "No live sessions. Set alerts on TradingView for the next open.",
                    14, Views.color(this, R.color.ink_dim), false));
            return;
        }
        for (MarketSession sess : s.openSessions) {
            LinearLayout row = Views.column(this);
            Views.margins(row, 0, Views.dp(this, 4), 0, Views.dp(this, 4));

            row.addView(Views.make(this,
                    sess.name + "  ·  " + sess.hours() + "  ·  Tier " + sess.tier,
                    13, Views.color(this, R.color.ink), true));

            StringBuilder b = new StringBuilder();
            for (int i = 0; i < sess.proxyTickers.length; i++) {
                if (i > 0) b.append("   •   ");
                b.append(sess.proxyTickers[i]);
            }
            TextView body = Views.make(this, b.toString(), 14, Views.color(this, R.color.ink), false);
            Views.margins(body, 0, Views.dp(this, 2), 0, 0);
            row.addView(body);
            list.addView(row);
        }
    }

    private void renderUsFocus(RecommendationEngine.Snapshot s) {
        TextView tv = (TextView) findViewById(R.id.txtUsFocus);
        if (tv == null) return;
        if (s.usFocusTickers.isEmpty()) {
            tv.setText("—");
            return;
        }
        StringBuilder b = new StringBuilder();
        for (int i = 0; i < s.usFocusTickers.size(); i++) {
            if (i > 0) b.append("   ·   ");
            b.append(s.usFocusTickers.get(i));
        }
        tv.setText(b.toString());
    }

    private void renderCatalysts(RecommendationEngine.Snapshot s) {
        LinearLayout list = (LinearLayout) findViewById(R.id.listCatalysts);
        if (list == null) return;
        list.removeAllViews();
        if (s.activeCatalysts.isEmpty()) {
            list.addView(Views.make(this, "No headline catalysts likely today. Stay disciplined.",
                    14, Views.color(this, R.color.ink_dim), false));
            return;
        }
        for (final Catalyst c : s.activeCatalysts) {
            LinearLayout row = Views.column(this);
            Views.margins(row, 0, Views.dp(this, 4), 0, Views.dp(this, 4));

            row.addView(Views.make(this, c.name + "  ·  " + c.window, 13,
                    Views.color(this, R.color.ink), true));
            row.addView(Views.make(this, "Impact: " + c.impact,
                    13, Views.color(this, R.color.ink_dim), false));

            Button open = new Button(this);
            open.setText("Open source");
            open.setTextColor(Views.color(this, R.color.accent));
            open.setBackground(null);
            open.setAllCaps(false);
            open.setPadding(0, Views.dp(this, 2), 0, 0);
            open.setGravity(Gravity.START);
            open.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { Views.openUrl(HomeActivity.this, c.url); }
            });
            row.addView(open);

            list.addView(row);
        }
    }

    private void renderQuickLinks() {
        LinearLayout list = (LinearLayout) findViewById(R.id.listQuickLinks);
        if (list == null) return;
        list.removeAllViews();
        for (final QuickLink l : Catalog.globalLinks()) {
            Button b = new Button(this);
            b.setText("• " + l.label);
            b.setTextColor(Views.color(this, R.color.accent));
            b.setBackground(null);
            b.setAllCaps(false);
            b.setGravity(Gravity.START);
            b.setPadding(0, Views.dp(this, 4), 0, Views.dp(this, 4));
            b.setOnClickListener(new View.OnClickListener() {
                @Override public void onClick(View v) { Views.openUrl(HomeActivity.this, l.url); }
            });
            list.addView(b);
        }
    }
}
