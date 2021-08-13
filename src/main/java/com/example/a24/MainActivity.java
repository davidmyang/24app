package com.example.a24;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.LifecycleObserver;
import androidx.lifecycle.OnLifecycleEvent;
import androidx.lifecycle.ProcessLifecycleOwner;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;

import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;

public class MainActivity extends AppCompatActivity implements LifecycleObserver {

    // declaring variables
    private ImageButton playButton, rulesButton, settingsButton, shoppingButton;
    private Button infiniteMode, timedMode, survivalMode;
    MediaPlayer buttonSoundEffect;
    static MediaPlayer mediaPlayer;
    int musicMode;
    AdView mAdView;
    private RelativeLayout back_dim_layout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // initializing variables
        playButton = findViewById(R.id.playButton);
        rulesButton = findViewById(R.id.rulesButton);
        settingsButton = findViewById(R.id.settingsButton);
        shoppingButton = findViewById(R.id.shoppingCartButton);
        infiniteMode = findViewById(R.id.infinitePlayButton);
        timedMode = findViewById(R.id.timedPlayButton);
        survivalMode = findViewById(R.id.survivalPlayButton);
        back_dim_layout = findViewById(R.id.bg_dim_layout);
        mediaPlayer = MediaPlayer.create(this, R.raw.music);
        mediaPlayer.setLooping(true);
        buttonSoundEffect = MediaPlayer.create(this, R.raw.sound);

        // observes lifecycle of app
        ProcessLifecycleOwner.get().getLifecycle().addObserver(this);

        // retrieving user preference for theme and music
        SharedPreferences prefs = this.getSharedPreferences("THEME_GAME_DATA", Context.MODE_PRIVATE);
        int themeMode = prefs.getInt("THEME_MODE", 0);

        SharedPreferences prefs2 = this.getSharedPreferences("MUSIC_GAME_DATA", Context.MODE_PRIVATE);
        musicMode = prefs2.getInt("MUSIC_MODE", 0);

        // setting theme
        if (themeMode == 0) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
        else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        }

        // starting background music
        if (musicMode == 0) {
            mediaPlayer.start();
        }

        // initializing banner ad at bottom of screen
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);


        // Play button - shows all three game modes (infinite, timed, and survival modes)
        playButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playButton.setVisibility(View.INVISIBLE);
                infiniteMode.setVisibility(View.VISIBLE);
                timedMode.setVisibility(View.VISIBLE);
                survivalMode.setVisibility(View.VISIBLE);

                playButtonSoundEffect();
            }
        });

        // Infinite mode button - goes to infinite mode game
        infiniteMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, InfiniteGameActivity.class);
                startActivity(i);
                playButtonSoundEffect();
            }
        });

        // Timed mode button - goes to timed mode game
        timedMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, TimedGameActivity.class);
                startActivity(i);
                playButtonSoundEffect();
            }
        });

        // Survival mode button - goes to survival mode game
        survivalMode.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, SurvivalGameActivity.class);
                startActivity(i);
                playButtonSoundEffect();
            }
        });

        
        // Rules button - opens rule popup window
        rulesButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                playButtonSoundEffect();
                // inflate the layout of the popup window
                LayoutInflater inflater = (LayoutInflater)
                        getSystemService(LAYOUT_INFLATER_SERVICE);
                View popupView = inflater.inflate(R.layout.activity_rules, null);

                // create the popup window
                DisplayMetrics displayMetrics = new DisplayMetrics();
                getWindowManager().getDefaultDisplay().getMetrics(displayMetrics);
                int height = displayMetrics.heightPixels;
                int width = displayMetrics.widthPixels;


                boolean focusable = true; // lets taps outside the popup also dismiss it
                final PopupWindow popupWindow = new PopupWindow(popupView, (int)(width*0.8), height/2, focusable);

                // dim background
                back_dim_layout.setVisibility(View.VISIBLE);

                // show the popup window
                // which view you pass in doesn't matter, it is only used for the window tolken
                popupWindow.showAtLocation(view, Gravity.CENTER, 0, 0);



                // dismiss the popup window when touched
                popupWindow.setOnDismissListener(new PopupWindow.OnDismissListener() {
                    @Override
                    public void onDismiss() {
                        back_dim_layout.setVisibility(View.GONE);
                        popupWindow.dismiss();
                    }
                });

            }
        });

        // Settings button - goes to settings page
        settingsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, SettingsActivity.class);
                startActivity(i);
                playButtonSoundEffect();
            }
        });

        // shopping button - goes to ads page
        shoppingButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, ShoppingCartActivity.class);
                startActivity(i);
                playButtonSoundEffect();
            }
        });
    }


    // overrides back button to exit app when pressed
    @Override
    public void onBackPressed() {
        Intent intent = new Intent(Intent.ACTION_MAIN);
        intent.addCategory(Intent.CATEGORY_HOME);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        startActivity(intent);
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

    // stops music when app goes to background
    @OnLifecycleEvent(Lifecycle.Event.ON_STOP)
    public void onAppBackgrounded() {
        if (mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
        }
    }

    // starts music again when app comes to foreground
    @OnLifecycleEvent(Lifecycle.Event.ON_START)
    public void onAppForegrounded () {
        SharedPreferences prefs2 = this.getSharedPreferences("MUSIC_GAME_DATA", Context.MODE_PRIVATE);
        musicMode = prefs2.getInt("MUSIC_MODE", 0);
        if (musicMode == 0) {
            mediaPlayer.start();
        }
    }

}
