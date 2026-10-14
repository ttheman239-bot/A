package com.avatartalk.app.speech;

import android.content.Context;
import android.os.Build;
import android.os.Bundle;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;

import java.util.HashMap;
import java.util.Locale;

public final class SpeechController {

    public interface Listener {
        void onReady(boolean ok, String message);
        void onSpeechStarted();
        void onSpeechEnded();
    }

    private final Context ctx;
    private final Listener listener;
    private TextToSpeech tts;
    private boolean ready;
    private int utterId = 0;

    public SpeechController(Context ctx, Listener l) {
        this.ctx = ctx.getApplicationContext();
        this.listener = l;
    }

    public void init() {
        try {
            tts = new TextToSpeech(ctx, new TextToSpeech.OnInitListener() {
                @Override public void onInit(int status) {
                    if (status != TextToSpeech.SUCCESS) {
                        ready = false;
                        if (listener != null) listener.onReady(false, "TTS init failed (" + status + ")");
                        return;
                    }
                    Locale[] candidates = {
                            Locale.getDefault(),
                            new Locale("th", "TH"),
                            Locale.US,
                            Locale.ENGLISH
                    };
                    boolean langOk = false;
                    for (Locale l : candidates) {
                        int r = tts.isLanguageAvailable(l);
                        if (r == TextToSpeech.LANG_AVAILABLE
                                || r == TextToSpeech.LANG_COUNTRY_AVAILABLE
                                || r == TextToSpeech.LANG_COUNTRY_VAR_AVAILABLE) {
                            tts.setLanguage(l);
                            langOk = true;
                            break;
                        }
                    }
                    tts.setSpeechRate(1.0f);
                    tts.setPitch(1.0f);
                    tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                        @Override public void onStart(String utteranceId) {
                            if (listener != null) listener.onSpeechStarted();
                        }
                        @Override public void onDone(String utteranceId) {
                            if (listener != null) listener.onSpeechEnded();
                        }
                        @Override public void onError(String utteranceId) {
                            if (listener != null) listener.onSpeechEnded();
                        }
                    });

                    ready = true;
                    if (listener != null) {
                        listener.onReady(true, langOk ? "TTS ready" : "TTS language fallback");
                    }
                }
            });
        } catch (Throwable t) {
            ready = false;
            if (listener != null) listener.onReady(false, "TTS unavailable: " + t.getMessage());
        }
    }

    public boolean isReady() { return ready && tts != null; }

    public void speak(String text) {
        if (!isReady() || text == null || text.length() == 0) return;
        String id = "u" + (++utterId);
        if (Build.VERSION.SDK_INT >= 21) {
            Bundle b = new Bundle();
            b.putFloat(TextToSpeech.Engine.KEY_PARAM_VOLUME, 1f);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, b, id);
        } else {
            HashMap<String, String> params = new HashMap<String, String>();
            params.put(TextToSpeech.Engine.KEY_PARAM_UTTERANCE_ID, id);
            tts.speak(text, TextToSpeech.QUEUE_FLUSH, params);
        }
    }

    public void stop() {
        if (tts != null) {
            try { tts.stop(); } catch (Throwable ignored) {}
        }
    }

    public void shutdown() {
        try {
            if (tts != null) {
                tts.stop();
                tts.shutdown();
            }
        } catch (Throwable ignored) {}
        tts = null;
        ready = false;
    }
}
