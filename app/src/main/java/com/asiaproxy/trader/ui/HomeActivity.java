package com.asiaproxy.trader.ui;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

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
            render();
            handler.postDelayed(this, 30_000L);
        }
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_home);

        findViewById(R.id.btnRefresh).setOnClickListener(v -> render());
        findViewById(R.id.navPairs).setOnClickListener(v -> startActivity(new Intent(this, PairsActivity.class)));
        findViewById(R.id.navCalendar).setOnClickListener(v -> startActivity(new Intent(this, CalendarActivity.class)));
        findViewById(R.id.navRules).setOnClickListener(v -> startActivity(new Intent(this, RulesActivity.class)));
        findViewById(R.id.navJournal).setOnClickListener(v -> startActivity(new Intent(this, JournalActivity.class)));

        render();
    }

    @Override protected void onResume() {
        super.onResume();
        handler.post(tick);
    }

    @Override protected void onPause() {
        super.onPause();
        handler.removeCallbacks(tick);
    }

    private void render() {
        RecommendationEngine.Snapshot s = RecommendationEngine.snapshot();

        SimpleDateFormat clockFmt = new SimpleDateFormat("HH:mm", Locale.US);
        clockFmt.setTimeZone(s.bangkok.getTimeZone());
        SimpleDateFormat dateFmt = new SimpleDateFormat("EEEE, dd MMM yyyy", Locale.ENGLISH);
        dateFmt.setTimeZone(s.bangkok.getTimeZone());

        ((TextView) findViewById(R.id.txtClock)).setText(clockFmt.format(s.bangkok.getTime()));
        ((TextView) findViewById(R.id.txtDate)).setText(dateFmt.format(s.bangkok.getTime()) + "  ·  Asia/Bangkok");
        ((TextView) findViewById(R.id.pillStatus)).setText(s.marketStatusLabel);

        TextView verdictView = (TextView) findViewById(R.id.txtVerdict);
        TextView reasonView = (TextView) findViewById(R.id.txtVerdictReason);
        TextView scoreView = (TextView) findViewById(R.id.txtReactionScore);
        verdictView.setText(s.verdict.headline);
        reasonView.setText(s.verdict.reason);
        scoreView.setText("Reaction Score: " + s.verdict.reactionScore + " / 100");
        applyTone(scoreView, s.verdict.tone);

        renderWatchlist(s);
        renderUsFocus(s);
        renderCatalysts(s);
        renderQuickLinks();

        SimpleDateFormat upd = new SimpleDateFormat("HH:mm:ss", Locale.US);
        upd.setTimeZone(s.bangkok.getTimeZone());
        ((TextView) findViewById(R.id.txtUpdated)).setText("Updated " + upd.format(s.bangkok.getTime()) + " · auto-refresh 30s");
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
        list.removeAllViews();
        if (s.openSessions.isEmpty()) {
            TextView tv = Views.make(this, "No live sessions. Set alerts on TradingView for the next open.", 14,
                    Views.color(this, R.color.ink_dim), false);
            list.addView(tv);
            return;
        }
        for (MarketSession sess : s.openSessions) {
            LinearLayout row = Views.column(this);
            Views.margins(row, 0, Views.dp(this, 4), 0, Views.dp(this, 4));

            TextView head = Views.make(this,
                    sess.name + "  ·  " + sess.hours() + "  ·  Tier " + sess.tier,
                    13, Views.color(this, R.color.ink), true);
            row.addView(head);

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
        list.removeAllViews();
        if (s.activeCatalysts.isEmpty()) {
            list.addView(Views.make(this, "No headline catalysts likely today. Stay disciplined.",
                    14, Views.color(this, R.color.ink_dim), false));
            return;
        }
        for (Catalyst c : s.activeCatalysts) {
            LinearLayout row = Views.column(this);
            Views.margins(row, 0, Views.dp(this, 4), 0, Views.dp(this, 4));

            TextView head = Views.make(this, c.name + "  ·  " + c.window, 13,
                    Views.color(this, R.color.ink), true);
            row.addView(head);
            TextView impact = Views.make(this, "Impact: " + c.impact,
                    13, Views.color(this, R.color.ink_dim), false);
            row.addView(impact);

            Button open = new Button(this);
            open.setText("Open source ↗");
            open.setTextColor(Views.color(this, R.color.accent));
            open.setBackground(null);
            open.setAllCaps(false);
            open.setPadding(0, Views.dp(this, 2), 0, 0);
            open.setGravity(Gravity.START);
            open.setOnClickListener(v -> Views.openUrl(this, c.url));
            row.addView(open);

            list.addView(row);
        }
    }

    private void renderQuickLinks() {
        LinearLayout list = (LinearLayout) findViewById(R.id.listQuickLinks);
        list.removeAllViews();
        for (QuickLink l : Catalog.globalLinks()) {
            Button b = new Button(this);
            b.setText("• " + l.label);
            b.setTextColor(Views.color(this, R.color.accent));
            b.setBackground(null);
            b.setAllCaps(false);
            b.setGravity(Gravity.START);
            b.setPadding(0, Views.dp(this, 4), 0, Views.dp(this, 4));
            b.setOnClickListener(v -> Views.openUrl(this, l.url));
            list.addView(b);
        }
    }
}
