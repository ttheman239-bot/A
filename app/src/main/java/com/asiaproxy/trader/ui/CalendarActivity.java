package com.asiaproxy.trader.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.Gravity;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.asiaproxy.trader.R;
import com.asiaproxy.trader.engine.Catalog;
import com.asiaproxy.trader.model.Catalyst;
import com.asiaproxy.trader.util.Views;

import java.util.Calendar;
import java.util.TimeZone;

public class CalendarActivity extends Activity {

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_list);
        if (getActionBar() != null) getActionBar().setDisplayHomeAsUpEnabled(true);

        LinearLayout container = (LinearLayout) findViewById(R.id.listContainer);

        int dom = Calendar.getInstance(TimeZone.getTimeZone("Asia/Bangkok")).get(Calendar.DAY_OF_MONTH);

        TextView head = Views.make(this,
                "Today is day " + dom + " of the month.\nCatalysts highlighted are likely to print today.",
                14, Views.color(this, R.color.ink), false);
        Views.margins(head, 0, 0, 0, Views.dp(this, 12));
        container.addView(head);

        for (Catalyst c : Catalog.catalysts()) {
            container.addView(makeCard(c, c.isLikelyToday(dom)));
        }
    }

    private LinearLayout makeCard(Catalyst c, boolean today) {
        LinearLayout col = Views.column(this);
        col.setBackgroundResource(R.drawable.bg_card);
        col.setPadding(Views.dp(this, 14), Views.dp(this, 14), Views.dp(this, 14), Views.dp(this, 14));
        Views.margins(col, 0, 0, 0, Views.dp(this, 10));

        LinearLayout top = Views.row(this);
        TextView title = Views.make(this, c.name, 15, Views.color(this, R.color.ink), true);
        title.setLayoutParams(new LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.WRAP_CONTENT, 1));
        top.addView(title);

        TextView pill = Views.make(this, today ? "Today" : c.window, 11,
                Views.color(this, R.color.ink), true);
        pill.setBackgroundResource(today ? R.drawable.bg_pill_bull : R.drawable.bg_pill_muted);
        pill.setPadding(Views.dp(this, 10), Views.dp(this, 4), Views.dp(this, 10), Views.dp(this, 4));
        top.addView(pill);
        col.addView(top);

        TextView win = Views.make(this, "Window: " + c.window, 13,
                Views.color(this, R.color.ink_dim), false);
        Views.margins(win, 0, Views.dp(this, 4), 0, 0);
        col.addView(win);

        TextView impact = Views.make(this, "Affects: " + c.impact, 13,
                Views.color(this, R.color.ink), false);
        Views.margins(impact, 0, Views.dp(this, 2), 0, 0);
        col.addView(impact);

        Button open = new Button(this);
        open.setText("↗  Source");
        open.setTextColor(Views.color(this, R.color.accent));
        open.setBackground(null);
        open.setAllCaps(false);
        open.setPadding(0, Views.dp(this, 6), 0, 0);
        open.setGravity(Gravity.START);
        open.setOnClickListener(v -> Views.openUrl(this, c.url));
        col.addView(open);

        return col;
    }
}
