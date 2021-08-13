package com.example.a24;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.res.AssetFileDescriptor;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.IBinder;
import android.provider.MediaStore;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Switch;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.io.IOException;


public class SettingsActivity extends AppCompatActivity {

    // declaring variables
    TextView infiniteMode, timedMode, survivalMode;
    private RadioGroup radioGroup;
    RadioButton radioLight, radioDark;
    Switch musicSwitch, soundEffectsSwitch;
    AdView mAdView;
    MediaPlayer buttonSoundEffect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        // initializing variables
        infiniteMode = findViewById(R.id.infiniteModeText);
        timedMode = findViewById(R.id.timedModeText);
        survivalMode = findViewById(R.id.survivalModeText);

        radioGroup = findViewById(R.id.idRGgroup);
        radioLight = findViewById(R.id.idRBLight);
        radioDark = findViewById(R.id.idRBDark);

        musicSwitch = findViewById(R.id.musicSwitch);
        soundEffectsSwitch = findViewById(R.id.soundEffectsSwitch);

        buttonSoundEffect = MediaPlayer.create(this, R.raw.sound);

        // initializing banner ad at bottom of screen
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // retrieving user preferences for high scores, theme, and sounds
        SharedPreferences prefs = this.getSharedPreferences("TIMED_GAME_DATA", Context.MODE_PRIVATE);
        int timedHighScore = prefs.getInt("TIMED_HIGH_SCORE", 0);

        SharedPreferences prefs2 = this.getSharedPreferences("SURVIVAL_GAME_DATA", Context.MODE_PRIVATE);
        int survivalHighScore = prefs2.getInt("SURVIVAL_HIGH_SCORE", 0);

        SharedPreferences prefs3 = this.getSharedPreferences("INFINITE_GAME_DATA", Context.MODE_PRIVATE);
        int numSolved = prefs3.getInt("INFINITE_HIGH_SCORE", 0);

        SharedPreferences prefs4 = this.getSharedPreferences("THEME_GAME_DATA", Context.MODE_PRIVATE);
        final int[] themeMode = {prefs4.getInt("THEME_MODE", 0)};

        SharedPreferences prefs5 = this.getSharedPreferences("MUSIC_GAME_DATA", Context.MODE_PRIVATE);
        final int[] musicMode = {prefs5.getInt("MUSIC_MODE", 0)};

        SharedPreferences prefs6 = this.getSharedPreferences("SOUND_GAME_DATA", Context.MODE_PRIVATE);
        final int[] soundMode = {prefs6.getInt("SOUND_MODE", 0)};

        // setting theme button
        if (themeMode[0] == 0) {
            radioLight.setChecked(true);
        }
        else {
            radioDark.setChecked(true);
        }

        // setting background music button
        if (musicMode[0] == 0) {
            musicSwitch.setChecked(true);
        }
        else {
            musicSwitch.setChecked(false);
        }

        // setting sound effects button
        if (soundMode[0] == 0) {
            soundEffectsSwitch.setChecked(true);
        }
        else {
            soundEffectsSwitch.setChecked(false);
        }

        // setting high scores for each mode
        infiniteMode.setText("Infinite Mode Questions Solved: " + numSolved);
        timedMode.setText("Timed Mode High Score: " + timedHighScore);
        survivalMode.setText("Survival Mode High Score: " + survivalHighScore);

        // radio buttons for choosing light or dark theme
        radioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                // on radio button check change
                switch (checkedId) {
                    case R.id.idRBLight:
                        // changing theme to light theme
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
                        if (MainActivity.mediaPlayer.isPlaying()) {
                            MainActivity.mediaPlayer.stop();
                        }
                        themeMode[0] = 0;
                        SharedPreferences.Editor editor = prefs4.edit();
                        editor.putInt("THEME_MODE", themeMode[0]);
                        editor.commit();
                        break;
                    case R.id.idRBDark:
                        // changing theme to dark theme
                        AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
                        if (MainActivity.mediaPlayer.isPlaying()) {
                            MainActivity.mediaPlayer.stop();
                        }
                        themeMode[0] = 1;
                        editor = prefs4.edit();
                        editor.putInt("THEME_MODE", themeMode[0]);
                        editor.commit();
                        break;
                }
                playButtonSoundEffect();
            }
        });

        // switch button for turning background music on or off
        musicSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (musicSwitch.isChecked() && !MainActivity.mediaPlayer.isPlaying())
                {
                    MainActivity.mediaPlayer.start();
                    SharedPreferences.Editor editor = prefs5.edit();
                    musicMode[0] = 0;
                    editor.putInt("MUSIC_MODE", musicMode[0]);
                    editor.commit();
                }
                else
                {
                    MainActivity.mediaPlayer.pause();
                    SharedPreferences.Editor editor = prefs5.edit();
                    musicMode[0] = 1;
                    editor.putInt("MUSIC_MODE",  musicMode[0]);
                    editor.commit();
                }
                playButtonSoundEffect();
            }
        });

        // switch button for turning sound effects on or off
        soundEffectsSwitch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playButtonSoundEffect();
                if (soundEffectsSwitch.isChecked())
                {
                    SharedPreferences.Editor editor = prefs6.edit();
                    soundMode[0] = 0;
                    editor.putInt("SOUND_MODE", soundMode[0]);
                    editor.commit();
                }
                else
                {
                    SharedPreferences.Editor editor = prefs6.edit();
                    soundMode[0] = 1;
                    editor.putInt("SOUND_MODE", soundMode[0]);
                    editor.commit();
                }
            }
        });
    }

    // function for button sound effects
    public void playButtonSoundEffect() {
        SharedPreferences prefs6 = this.getSharedPreferences("SOUND_GAME_DATA", Context.MODE_PRIVATE);
        int soundMode = prefs6.getInt("SOUND_MODE", 0);

        if (soundMode == 0) {
            if (buttonSoundEffect.isPlaying()) {
                buttonSoundEffect.stop();
                buttonSoundEffect.release();
                buttonSoundEffect = MediaPlayer.create(this, R.raw.sound);
                buttonSoundEffect.start();
            } else {
                buttonSoundEffect.start();
            }
        }
    }

}