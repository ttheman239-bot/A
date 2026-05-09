package com.asiaproxy.trader.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.asiaproxy.trader.R;
import com.asiaproxy.trader.engine.Catalog;
import com.asiaproxy.trader.model.Pair;
import com.asiaproxy.trader.util.Views;

public class PairDetailActivity extends Activity {

    public static final String EXTRA_PAIR_ID = "pair_id";

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_pair_detail);
        if (getActionBar() != null) getActionBar().setDisplayHomeAsUpEnabled(true);

        String id = getIntent().getStringExtra(EXTRA_PAIR_ID);
        Pair pair = null;
        for (Pair p : Catalog.pairs()) if (p.id.equals(id)) { pair = p; break; }
        if (pair == null) { finish(); return; }

        ((TextView) findViewById(R.id.txtPairTitle)).setText(pair.title());
        ((TextView) findViewById(R.id.txtPairCategory)).setText(pair.category + "  ·  " + pair.usName + " / " + pair.asiaName);
        ((TextView) findViewById(R.id.txtPairTier)).setText("Conviction " + pair.stars());
        setTitle(pair.usTicker + " ↔ " + pair.asiaTicker);

        ((TextView) findViewById(R.id.txtCatalyst)).setText(pair.catalyst);

        LinearLayout list = (LinearLayout) findViewById(R.id.listChecklist);
        for (String n : pair.notes) {
            TextView tv = Views.make(this, "• " + n, 14, Views.color(this, R.color.ink), false);
            Views.margins(tv, 0, Views.dp(this, 2), 0, Views.dp(this, 2));
            list.addView(tv);
        }

        LinearLayout links = (LinearLayout) findViewById(R.id.listLinks);
        addLink(links, "TradingView · " + pair.usTicker, pair.tradingViewUs);
        addLink(links, "TradingView · " + pair.asiaTicker, pair.tradingViewAsia);
        addLink(links, "Investing.com — search " + pair.usTicker,
                "https://th.investing.com/search/?q=" + pair.usTicker);
        addLink(links, "Yahoo Finance · " + pair.usTicker,
                "https://finance.yahoo.com/quote/" + pair.usTicker);
        addLink(links, "Benzinga · " + pair.usTicker,
                "https://www.benzinga.com/quote/" + pair.usTicker);
    }

    private void addLink(LinearLayout list, String label, String url) {
        Button b = new Button(this);
        b.setText("↗  " + label);
        b.setTextColor(Views.color(this, R.color.accent));
        b.setBackground(null);
        b.setAllCaps(false);
        b.setGravity(Gravity.START);
        b.setPadding(0, Views.dp(this, 4), 0, Views.dp(this, 4));
        b.setOnClickListener(v -> Views.openUrl(this, url));
        list.addView(b);
    }
}
