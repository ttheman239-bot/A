package com.asiaproxy.trader.ui;

import android.app.Activity;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.asiaproxy.trader.R;
import com.asiaproxy.trader.util.Views;

public class RulesActivity extends Activity {

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_list);

        TextView title = (TextView) findViewById(R.id.txtScreenTitle);
        if (title != null) title.setText(R.string.tab_rules);

        LinearLayout container = (LinearLayout) findViewById(R.id.listContainer);
        if (container == null) return;

        section(container, "Strategy Cycle — 5 Phases", new String[]{
                "1. Prep (1-2 days before): mark catalyst day, get consensus, plan bias",
                "2. Asia Session 07:00-15:00 ICT: collect signal — KOSPI 07:00, TWSE 08:00, HKEX 08:30",
                "3. Pre-US 15:00-20:30: read futures + premkt, line up entries",
                "4. Execution (post-news): high conviction = follow trend; in-line = often fades 30-60 min",
                "5. Post-trade: journal Asia signal vs US reaction; refine the playbook"
        });

        section(container, getString(R.string.rules_buy_title), new String[]{
                "Asia proxy moves > 2% on volume + Beat clearly above expectation",
                "US futures (ES / NQ) confirm direction",
                "Catalyst is Tier A and on schedule (TSMC rev, BYD sales, GGR)",
                "Volatility is moderate — wide enough to pay, not chaotic",
                "Pre-market shows real volume, not a tape painting"
        });

        section(container, getString(R.string.rules_avoid_title), new String[]{
                "No catalyst — Asia closed sideways and news is in-line",
                "Conflicting catalysts firing at the same time (confusion)",
                "VIX > 35 (panic regime)",
                "Day before FOMC / NFP / CPI",
                "Friday afternoon (low liquidity, weekend gap risk)"
        });

        section(container, getString(R.string.rules_post_title), new String[]{
                "Wait 15-30 min after news for first reaction to settle",
                "Read price first, headline second",
                "Bullish hold = beat + futures green + volume rising",
                "Bearish bias = beat but price fades = profit taking, fade rallies",
                "Mixed = stand aside, no edge"
        });

        section(container, "Execution Playbook", new String[]{
                "High conviction (big surprise): enter follow-trend, risk 0.5-1%",
                "In-line / mild beat-or-miss: expect fade in first 30-60 min — fade rallies",
                "Stop loss: 1-2% or 1× ATR, whichever is tighter — never widen",
                "Take profit: 1:2 R hard target, or trail with VWAP / structure",
                "Avoid overlapping catalysts (TSMC + BYD same morning = confusion)"
        });

        section(container, getString(R.string.rules_risk_title), new String[]{
                "Risk per trade: 0.5–1% of portfolio",
                "Hard stop on every entry, no exceptions",
                "Daily loss limit: 2%, then walk away",
                "No catalyst, no trade — wait for next scheduled event",
                "Journal every trade — verdict before, outcome after"
        });
    }

    private void section(LinearLayout parent, String title, String[] items) {
        LinearLayout col = Views.column(this);
        col.setBackgroundResource(R.drawable.bg_card);
        col.setPadding(Views.dp(this, 14), Views.dp(this, 14), Views.dp(this, 14), Views.dp(this, 14));
        Views.margins(col, 0, 0, 0, Views.dp(this, 12));

        col.addView(Views.make(this, title, 15, Views.color(this, R.color.ink), true));

        for (String s : items) {
            TextView tv = Views.make(this, "•  " + s, 14, Views.color(this, R.color.ink), false);
            Views.margins(tv, 0, Views.dp(this, 6), 0, 0);
            tv.setLineSpacing(Views.dp(this, 2), 1.0f);
            col.addView(tv);
        }
        parent.addView(col);
    }
}
