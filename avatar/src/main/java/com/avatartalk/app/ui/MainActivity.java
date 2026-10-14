package com.avatartalk.app.ui;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;

import com.avatartalk.app.R;
import com.avatartalk.app.anim.GestureLibrary;
import com.avatartalk.app.anim.PosePlayer;
import com.avatartalk.app.speech.MouthDriver;
import com.avatartalk.app.speech.SpeechController;
import com.avatartalk.app.view.AvatarView;

public class MainActivity extends Activity {

    private AvatarView avatar;
    private SpeechController speech;
    private MouthDriver mouth = new MouthDriver();
    private final Handler ui = new Handler(Looper.getMainLooper());
    private boolean idleOn = true;
    private boolean mouthMuted = false;
    private boolean manualMouth = false;

    private final Runnable mouthTick = new Runnable() {
        @Override public void run() {
            float v = mouth.sample();
            if (avatar != null && !manualMouth) {
                avatar.getPlayer().setMouthDrive(v);
            }
            ui.postDelayed(this, 33L);
        }
    };

    @Override protected void onCreate(Bundle b) {
        super.onCreate(b);
        try {
            setContentView(R.layout.activity_main);
        } catch (Throwable t) {
            Toast.makeText(this, "Layout error: " + t.getMessage(), Toast.LENGTH_LONG).show();
            return;
        }

        avatar = (AvatarView) findViewById(R.id.avatar);
        final EditText input = (EditText) findViewById(R.id.inputSay);
        final Button btnSpeak = (Button) findViewById(R.id.btnSpeak);
        final Button btnStop = (Button) findViewById(R.id.btnStop);
        final Button btnIdle = (Button) findViewById(R.id.btnIdle);
        final Button btnMute = (Button) findViewById(R.id.btnMute);
        final TextView ttsStatus = (TextView) findViewById(R.id.ttsStatus);
        final SeekBar seek = (SeekBar) findViewById(R.id.seekMouth);

        speech = new SpeechController(this, new SpeechController.Listener() {
            @Override public void onReady(final boolean ok, final String msg) {
                ui.post(new Runnable() {
                    @Override public void run() {
                        if (ttsStatus != null) ttsStatus.setText(ok ? "TTS ready" : msg);
                        if (!ok) {
                            Toast.makeText(MainActivity.this, R.string.tts_missing, Toast.LENGTH_LONG).show();
                        }
                    }
                });
            }
            @Override public void onSpeechStarted() { mouth.setSpeaking(true); }
            @Override public void onSpeechEnded() { mouth.setSpeaking(false); }
        });
        speech.init();

        if (btnSpeak != null) btnSpeak.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                String text = input == null ? "" : input.getText().toString().trim();
                if (text.length() == 0) {
                    text = "Hello! I can wave, point, count, and talk. Try the buttons below.";
                    if (input != null) input.setText(text);
                }
                hideKeyboard(input);
                speech.speak(text);
            }
        });

        if (btnStop != null) btnStop.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                speech.stop();
                mouth.setSpeaking(false);
                avatar.getPlayer().playGesture(GestureLibrary.get("relax"));
            }
        });

        if (btnIdle != null) btnIdle.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                idleOn = !idleOn;
                btnIdle.setText(idleOn ? "Idle: on" : "Idle: off");
            }
        });

        if (btnMute != null) btnMute.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                mouthMuted = !mouthMuted;
                avatar.getPlayer().setMouthMuted(mouthMuted);
                btnMute.setText(mouthMuted ? "Mouth: muted" : "Mute mouth");
            }
        });

        if (seek != null) seek.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override public void onProgressChanged(SeekBar sb, int progress, boolean fromUser) {
                if (!fromUser) return;
                manualMouth = progress > 0 && !mouth.isSpeaking();
                if (avatar != null) avatar.getPlayer().setMouthDrive(progress / 100f);
            }
            @Override public void onStartTrackingTouch(SeekBar sb) {}
            @Override public void onStopTrackingTouch(SeekBar sb) {
                if (sb.getProgress() == 0) manualMouth = false;
            }
        });

        buildGestureRow();
        buildCountRow();

        ui.postDelayed(mouthTick, 33L);
    }

    @Override protected void onDestroy() {
        ui.removeCallbacksAndMessages(null);
        if (speech != null) speech.shutdown();
        super.onDestroy();
    }

    private void buildGestureRow() {
        LinearLayout row = (LinearLayout) findViewById(R.id.rowGestures);
        if (row == null) return;
        String[] ids = {"wave", "point", "thumbs", "clap", "hands_up", "akimbo", "arms_crossed", "relax"};
        for (String id : ids) {
            GestureLibrary.Gesture g = GestureLibrary.get(id);
            if (g == null) continue;
            row.addView(makeGestureButton(g));
        }
    }

    private void buildCountRow() {
        LinearLayout row = (LinearLayout) findViewById(R.id.rowCount);
        if (row == null) return;
        for (int i = 1; i <= 5; i++) {
            GestureLibrary.Gesture g = GestureLibrary.get("count_" + i);
            if (g == null) continue;
            row.addView(makeGestureButton(g));
        }
    }

    private Button makeGestureButton(final GestureLibrary.Gesture g) {
        Button b = new Button(this);
        b.setText(g.label);
        b.setTextSize(13);
        b.setAllCaps(false);
        b.setBackgroundResource(R.drawable.bg_btn_ghost);
        b.setTextColor(resColor(R.color.ink));
        b.setTypeface(b.getTypeface(), Typeface.NORMAL);
        LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT, dp(42));
        lp.setMargins(0, 0, dp(6), 0);
        b.setLayoutParams(lp);
        b.setPadding(dp(12), 0, dp(12), 0);
        b.setMinWidth(dp(56));
        b.setOnClickListener(new View.OnClickListener() {
            @Override public void onClick(View v) {
                PosePlayer p = avatar.getPlayer();
                p.playGesture(g);
            }
        });
        return b;
    }

    private int dp(int v) {
        return (int) (v * getResources().getDisplayMetrics().density + 0.5f);
    }

    @SuppressWarnings("deprecation")
    private int resColor(int resId) {
        return getResources().getColor(resId);
    }

    private void hideKeyboard(View v) {
        if (v == null) return;
        try {
            InputMethodManager imm = (InputMethodManager) getSystemService(INPUT_METHOD_SERVICE);
            if (imm != null) imm.hideSoftInputFromWindow(v.getWindowToken(), 0);
        } catch (Throwable ignored) {}
    }
}
