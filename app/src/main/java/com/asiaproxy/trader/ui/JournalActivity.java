package com.asiaproxy.trader.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.asiaproxy.trader.R;
import com.asiaproxy.trader.engine.JournalStore;
import com.asiaproxy.trader.model.JournalEntry;
import com.asiaproxy.trader.util.Views;

import java.util.List;

public class JournalActivity extends Activity {

    private JournalStore store;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_journal);
        if (getActionBar() != null) getActionBar().setDisplayHomeAsUpEnabled(true);

        store = new JournalStore(this);

        EditText proxyIn = (EditText) findViewById(R.id.inputProxy);
        EditText usIn = (EditText) findViewById(R.id.inputUs);
        EditText verIn = (EditText) findViewById(R.id.inputVerdict);

        findViewById(R.id.btnSave).setOnClickListener(v -> {
            String p = proxyIn.getText().toString().trim();
            String u = usIn.getText().toString().trim();
            String vd = verIn.getText().toString().trim();
            if (p.isEmpty() && u.isEmpty() && vd.isEmpty()) return;
            store.add(p, u, vd);
            proxyIn.setText("");
            usIn.setText("");
            verIn.setText("");
            Toast.makeText(this, R.string.journal_saved, Toast.LENGTH_SHORT).show();
            renderEntries();
        });

        findViewById(R.id.btnClear).setOnClickListener(v -> {
            proxyIn.setText("");
            usIn.setText("");
            verIn.setText("");
        });

        renderEntries();
    }

    private void renderEntries() {
        LinearLayout list = (LinearLayout) findViewById(R.id.listEntries);
        list.removeAllViews();
        List<JournalEntry> entries = store.all();
        if (entries.isEmpty()) {
            TextView tv = Views.make(this, getString(R.string.journal_empty),
                    14, Views.color(this, R.color.ink_dim), false);
            list.addView(tv);
            return;
        }
        for (JournalEntry e : entries) {
            list.addView(card(e));
        }
    }

    private View card(JournalEntry e) {
        LinearLayout col = Views.column(this);
        col.setBackgroundResource(R.drawable.bg_card);
        col.setPadding(Views.dp(this, 14), Views.dp(this, 14), Views.dp(this, 14), Views.dp(this, 14));
        Views.margins(col, 0, 0, 0, Views.dp(this, 8));

        TextView ts = Views.make(this, e.dateLabel, 11, Views.color(this, R.color.ink_dim), true);
        col.addView(ts);

        if (!e.proxy.isEmpty()) {
            TextView t = Views.make(this, "Asia signal: " + e.proxy, 14, Views.color(this, R.color.ink), false);
            Views.margins(t, 0, Views.dp(this, 4), 0, 0);
            col.addView(t);
        }
        if (!e.us.isEmpty()) {
            TextView t = Views.make(this, "US reaction: " + e.us, 14, Views.color(this, R.color.ink), false);
            Views.margins(t, 0, Views.dp(this, 2), 0, 0);
            col.addView(t);
        }
        if (!e.verdict.isEmpty()) {
            TextView t = Views.make(this, "Verdict: " + e.verdict, 14, Views.color(this, R.color.ink), true);
            Views.margins(t, 0, Views.dp(this, 4), 0, 0);
            col.addView(t);
        }
        return col;
    }
}
