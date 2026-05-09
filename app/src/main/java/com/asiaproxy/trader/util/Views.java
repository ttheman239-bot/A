package com.asiaproxy.trader.util;

import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.Typeface;
import android.net.Uri;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

public final class Views {

    private Views() {}

    public static int dp(Context ctx, int v) {
        float d = ctx.getResources().getDisplayMetrics().density;
        return (int) (v * d + 0.5f);
    }

    public static TextView make(Context ctx, String text, int sizeSp, int color, boolean bold) {
        TextView tv = new TextView(ctx);
        tv.setText(text);
        tv.setTextSize(sizeSp);
        tv.setTextColor(color);
        if (bold) tv.setTypeface(tv.getTypeface(), Typeface.BOLD);
        return tv;
    }

    public static LinearLayout row(Context ctx) {
        LinearLayout l = new LinearLayout(ctx);
        l.setOrientation(LinearLayout.HORIZONTAL);
        l.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return l;
    }

    public static LinearLayout column(Context ctx) {
        LinearLayout l = new LinearLayout(ctx);
        l.setOrientation(LinearLayout.VERTICAL);
        l.setLayoutParams(new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT));
        return l;
    }

    public static void margins(View v, int l, int t, int r, int b) {
        ViewGroup.LayoutParams lp = v.getLayoutParams();
        if (!(lp instanceof ViewGroup.MarginLayoutParams)) {
            lp = new LinearLayout.LayoutParams(
                    lp == null ? ViewGroup.LayoutParams.MATCH_PARENT : lp.width,
                    lp == null ? ViewGroup.LayoutParams.WRAP_CONTENT : lp.height);
        }
        ViewGroup.MarginLayoutParams m = (ViewGroup.MarginLayoutParams) lp;
        m.setMargins(l, t, r, b);
        v.setLayoutParams(m);
    }

    public static void openUrl(Context ctx, String url) {
        try {
            Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(url));
            i.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            ctx.startActivity(i);
        } catch (Throwable ignored) {
        }
    }

    @SuppressWarnings("deprecation")
    public static int color(Context ctx, int resId) {
        return ctx.getResources().getColor(resId);
    }
}
