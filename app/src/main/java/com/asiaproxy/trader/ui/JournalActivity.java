package com.asiaproxy.trader.ui;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
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
    private EditText proxyIn, usIn, verIn;

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_journal);

        store = new JournalStore(this);

        proxyIn = (EditText) findViewById(R.id.inputProxy);
        usIn = (EditText) findViewById(R.id.inputUs);
        verIn = (EditText) findViewById(R.id.inputVerdict);

        View btnSave = findViewById(R.id.btnSave);
        if (btnSave != null) btnSave.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String p = proxyIn == null ? "" : proxyIn.getText().toString().trim();
                String u = usIn == null ? "" : usIn.getText().toString().trim();
                String vd = verIn == null ? "" : verIn.getText().toString().trim();
                if (p.isEmpty() && u.isEmpty() && vd.isEmpty()) return;
                store.add(p, u, vd);
                if (proxyIn != null) proxyIn.setText("");
                if (usIn != null) usIn.setText("");
                if (verIn != null) verIn.setText("");
                Toast.makeText(JournalActivity.this, R.string.journal_saved, Toast.LENGTH_SHORT).show();
                renderEntries();
            }
        });

        View btnClear = findViewById(R.id.btnClear);
        if (btnClear != null) btnClear.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                if (proxyIn != null) proxyIn.setText("");
                if (usIn != null) usIn.setText("");
                if (verIn != null) verIn.setText("");
            }
        });

        renderEntries();
    }

    private void renderEntries() {
        LinearLayout list = (LinearLayout) findViewById(R.id.listEntries);
        if (list == null) return;
        list.removeAllViews();
        List<JournalEntry> entries = store.all();
        if (entries.isEmpty()) {
            list.addView(Views.make(this, getString(R.string.journal_empty),
                    14, Views.color(this, R.color.ink_dim), false));
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

        col.addView(Views.make(this, e.dateLabel, 11, Views.color(this, R.color.ink_dim), true));

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
