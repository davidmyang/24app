package com.example.a24;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.media.MediaPlayer;
import android.os.IBinder;

public class BackgroundPlayer extends AppCompatActivity {
    static MediaPlayer mediaPlayer;

    public void startBackgroundMusic() {
            mediaPlayer = MediaPlayer.create(this, R.raw.music);
            mediaPlayer.setLooping(true);
            mediaPlayer.start();
    }

    public void stopBackgroundMusic() {
        if(mediaPlayer!=null)
            mediaPlayer.stop();
    }

    public boolean isPlaying() {
        return mediaPlayer.isPlaying();
    }

    public void kill() {
        if(mediaPlayer!=null) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;

        }
    }
}
