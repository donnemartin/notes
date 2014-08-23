package com.donnemartin.android.notes.notes;

import android.content.Context;
import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

public class AudioPlayer {

    private MediaPlayer mPlayer;
    private boolean mIsPaused;
    private static String mFileName;

    private static final String LOG_TAG = "AudioPlayer";

    public AudioPlayer(String fileName) {
        mFileName = fileName;
    }

    public boolean isPlaying() {
        boolean isPlaying = false;
        if (mPlayer != null && mPlayer.isPlaying()) {
            isPlaying = true;
        }
        return isPlaying;
    }

    public boolean isPaused() {
        return mIsPaused;
    }

    public void stop() {
        if (mPlayer != null)
        {
            // We don't want to hold onto the audio decoder hardware and
            // other system resources, so call release instead of stop
            mPlayer.release();
            mPlayer = null;
        }
        mIsPaused = false;
    }

    public void playOrPause(Context context) {
        if (isPlaying()) {
            mPlayer.pause();
            mIsPaused = true;
        }
        else if (mIsPaused) {
            mPlayer.start();
            mIsPaused = false;
        }
        else {
            // Keep exactly one MediaPlayer around and keep it
            // around only as long as it is playing something
            // Call stop here and set a listener to call stop() when the
            // audio file has finished playing
            stop();

            mPlayer = new MediaPlayer();

            try {
                mPlayer.setDataSource(mFileName);
                mPlayer.prepare();
                mPlayer.start();
            } catch (IOException e) {
                Log.e(LOG_TAG, "prepare() failed");
            }

            mPlayer.setOnCompletionListener(
                new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    stop();
                }
            });

            mPlayer.start();
            mIsPaused = false;
        }
    }
}
