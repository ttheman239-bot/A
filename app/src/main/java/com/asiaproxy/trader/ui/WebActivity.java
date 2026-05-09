package com.asiaproxy.trader.ui;

import android.app.Activity;
import android.os.Bundle;
import android.webkit.WebView;

import com.asiaproxy.trader.R;

public class WebActivity extends Activity {

    public static final String EXTRA_URL = "url";

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        setContentView(R.layout.activity_web);
        if (getActionBar() != null) getActionBar().setDisplayHomeAsUpEnabled(true);

        WebView w = (WebView) findViewById(R.id.web);
        w.getSettings().setJavaScriptEnabled(true);
        w.getSettings().setDomStorageEnabled(true);
        String url = getIntent().getStringExtra(EXTRA_URL);
        if (url != null) w.loadUrl(url);
    }
}
