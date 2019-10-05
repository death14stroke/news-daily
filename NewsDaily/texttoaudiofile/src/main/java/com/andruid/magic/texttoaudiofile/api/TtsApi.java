package com.andruid.magic.texttoaudiofile.api;

import android.content.Context;
import android.speech.tts.TextToSpeech;
import android.speech.tts.UtteranceProgressListener;
import android.widget.Toast;

import com.andruid.magic.texttoaudiofile.util.FileUtils;

import java.io.File;
import java.util.Locale;

import timber.log.Timber;

import static com.andruid.magic.texttoaudiofile.data.Constants.DIR_TTS;

public class TtsApi {
    private static TtsApi sInstance;
    private static final Object LOCK = new Object();
    private static TextToSpeech tts;
    private static File dir;
    private static boolean ttsInit = false;

    public static void init(Context context){
        dir = new File(context.getCacheDir(), DIR_TTS);
        initTTS(context);
    }

    public static TtsApi getInstance(){
        if(sInstance == null){
            synchronized (LOCK){
                Timber.d("ttsapi instance created");
                sInstance = new TtsApi();
            }
        }
        return sInstance;
    }

    private static void initTTS(final Context context) {
        tts = new TextToSpeech(context, new TextToSpeech.OnInitListener() {
            @Override
            public void onInit(int status) {
                if(status == TextToSpeech.SUCCESS){
                    int result = tts.setLanguage(Locale.getDefault());
                    if(result == TextToSpeech.LANG_MISSING_DATA || result == TextToSpeech.LANG_NOT_SUPPORTED)
                        Toast.makeText(context, "Text to speech not available", Toast.LENGTH_SHORT).show();
                    else
                        ttsInit = true;
                }
            }
        });
    }

    public void convertToAudioFile(String text, String utteranceId,
                                   final AudioConversionListener mListener){
        if(!dir.exists()) {
            boolean res = dir.mkdir();
            Timber.d("dir created %s", res);
        }
        if(ttsInit) {
            tts.setOnUtteranceProgressListener(new UtteranceProgressListener() {
                @Override
                public void onStart(String utteranceId) {}

                @Override
                public void onDone(String utteranceId) {
                    File file = new File(dir, FileUtils.getFileName(utteranceId));
                    mListener.onAudioCreated(file);
                }

                @Override
                public void onError(String utteranceId) {
                    mListener.onFailure("Failed creating audio for utterance id "+utteranceId);
                }
            });
            File file = new File(dir, FileUtils.getFileName(utteranceId));
            tts.synthesizeToFile(text, null, file, utteranceId);
        }
        else
            mListener.onFailure("TTS init failed");
    }

    public boolean isReady() {
        return ttsInit;
    }

    public static void release(){
        tts.shutdown();
    }

    public interface AudioConversionListener {
        void onAudioCreated(File file);
        void onFailure(String msg);
    }
}