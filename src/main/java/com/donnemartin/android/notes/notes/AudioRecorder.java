package com.donnemartin.android.notes.notes;

import android.media.MediaRecorder;
import android.util.Log;

import java.io.IOException;

public class AudioRecorder {

    private MediaRecorder mRecorder;
    private static String mFileName;
    private boolean mIsRecording;

    private static final String LOG_TAG = "AudioRecorder";

    public AudioRecorder(String fileName) {
        mFileName = fileName;
    }

    public boolean isRecording() {
        return mIsRecording;
    }

    public void startRecording() {
        mRecorder = new MediaRecorder();
        mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
        mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
        mRecorder.setOutputFile(mFileName);
        mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);

        try {
            mRecorder.prepare();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }

        mRecorder.start();
        mIsRecording = true;
    }

    public void stopRecording() {
        mRecorder.stop();
        mRecorder.release();
        mRecorder = null;
        mIsRecording = false;
    }
}
