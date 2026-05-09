package com.asiaproxy.trader.ui;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Typeface;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.asiaproxy.trader.R;
import com.asiaproxy.trader.engine.Catalog;
import com.asiaproxy.trader.model.Pair;
import com.asiaproxy.trader.util.Views;

import java.util.LinkedHashMap;
import java.util.Map;

public class PairsActivity extends Activity {

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_list);

        TextView title = (TextView) findViewById(R.id.txtScreenTitle);
        if (title != null) title.setText(R.string.tab_pairs);

        LinearLayout container = (LinearLayout) findViewById(R.id.listContainer);
        if (container == null) return;

        Map<String, LinearLayout> groups = new LinkedHashMap<String, LinearLayout>();
        for (Pair p : Catalog.pairs()) {
            LinearLayout g = groups.get(p.category);
            if (g == null) {
                g = newCategoryCard(p.category);
                groups.put(p.category, g);
                container.addView(g);
            }
            g.addView(makeRow(p));
        }
    }

    private LinearLayout newCategoryCard(String category) {
        LinearLayout col = Views.column(this);
        col.setBackgroundResource(R.drawable.bg_card);
        col.setPadding(Views.dp(this, 14), Views.dp(this, 14), Views.dp(this, 14), Views.dp(this, 14));
        Views.margins(col, 0, 0, 0, Views.dp(this, 12));

        col.addView(Views.make(this, category, 14, Views.color(this, R.color.ink_dim), true));
        return col;
    }

    private View makeRow(final Pair p) {
        LinearLayout row = Views.column(this);
        row.setPadding(0, Views.dp(this, 8), 0, Views.dp(this, 8));
        row.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                Intent i = new Intent(PairsActivity.this, PairDetailActivity.class);
                i.putExtra(PairDetailActivity.EXTRA_PAIR_ID, p.id);
                startActivity(i);
            }
        });

        LinearLayout top = Views.row(this);
        TextView title = Views.make(this, p.title(), 16, Views.color(this, R.color.ink), true);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        top.addView(title);

        top.addView(Views.make(this, p.stars(), 12, Views.color(this, R.color.accent), true));
        row.addView(top);

        TextView sub = Views.make(this, p.usName + " ↔ " + p.asiaName,
                13, Views.color(this, R.color.ink_dim), false);
        Views.margins(sub, 0, Views.dp(this, 2), 0, 0);
        row.addView(sub);

        TextView cat = Views.make(this, "Catalyst: " + p.catalyst,
                12, Views.color(this, R.color.ink), false);
        cat.setTypeface(cat.getTypeface(), Typeface.ITALIC);
        Views.margins(cat, 0, Views.dp(this, 4), 0, 0);
        row.addView(cat);

        View divider = new View(this);
        divider.setBackgroundColor(Views.color(this, R.color.divider));
        LinearLayout.LayoutParams dp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT, Views.dp(this, 1));
        dp.topMargin = Views.dp(this, 8);
        divider.setLayoutParams(dp);
        row.addView(divider);

        return row;
    }
}
