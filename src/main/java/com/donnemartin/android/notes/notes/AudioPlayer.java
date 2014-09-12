package com.donnemartin.android.notes.notes;

import android.media.MediaPlayer;
import android.util.Log;

import java.io.IOException;

public class AudioPlayer {

    private MediaPlayer mPlayer;
    private static String mFileName;

    private static final String LOG_TAG = "AudioPlayer";
    public static final int PLAY_FROM_START = 0;

    public AudioPlayer(String fileName) {
        mFileName = fileName;
    }

    public boolean validMediaPlayer() {
        boolean isValidMediaPlayer = false;

        if (mPlayer != null) {
            isValidMediaPlayer = true;
        }

        return isValidMediaPlayer;
    }

    public boolean isPlaying() {
        boolean isPlaying = false;

        if (mPlayer != null && mPlayer.isPlaying()) {
            isPlaying = true;
        }

        return isPlaying;
    }

    public void stop() {
        if (mPlayer != null) {
            // We don't want to hold onto the audio decoder hardware and
            // other system resources, so call release instead of stop
            mPlayer.release();
            mPlayer = null;
        }
    }

    public void play(int position) {
        // Keep exactly one MediaPlayer around and keep it
        // around only as long as it is playing something
        // Call stop here and set a listener to call stop() when the
        // audio file has finished playing
        stop();

        mPlayer = new MediaPlayer();

        mPlayer.setOnCompletionListener(
            new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                // TODO: Notify that audio has completed
                // so we can change the button text
                stop();
            }
        });

        try {
            mPlayer.setDataSource(mFileName);
            mPlayer.prepare();
            mPlayer.seekTo(position);
            mPlayer.start();
        } catch (IOException e) {
            Log.e(LOG_TAG, "prepare() failed");
        }
    }

    public int getCurrentPosition()
    {
        int position = 0;

        if (mPlayer != null) {
            position = mPlayer.getCurrentPosition();
        }

        return position;
    }
}
