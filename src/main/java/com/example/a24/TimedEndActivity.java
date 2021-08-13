package com.example.a24;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;


public class TimedEndActivity extends AppCompatActivity {

    // declaring variables
    TextView endScoreText, highScoreText, newHighScore;
    ImageButton home, playAgain, settings, leaderboard, shoppingCart;
    AdView mAdView;
    MediaPlayer buttonSoundEffect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_timed_end);

        // initializing variables
        home = findViewById(R.id.homeButton);
        playAgain = findViewById(R.id.playAgainButton);
        settings = findViewById(R.id.settingsButton);
        leaderboard = findViewById(R.id.leaderboardButton);
        shoppingCart = findViewById(R.id.shoppingCartButton);

        endScoreText = findViewById(R.id.endScoreText);
        highScoreText = findViewById(R.id.highScoreText);
        newHighScore = findViewById(R.id.newHighScore);

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

        // fetching score from just finished timed game mode
        int endScore = getIntent().getIntExtra("SCORE", 0);
        endScoreText.setText("Score: " + endScore);

        // retrieving user timed mode high score
        SharedPreferences prefs = this.getSharedPreferences("TIMED_GAME_DATA", Context.MODE_PRIVATE);
        int highScore = prefs.getInt("TIMED_HIGH_SCORE", 0);

        // checking if current score is greater than timed mode high score
        if (endScore > highScore) {
            highScoreText.setText("High Score: " + endScore);
            newHighScore.setVisibility(View.VISIBLE);

            // save new high score
            SharedPreferences.Editor editor = prefs.edit();
            editor.putInt("TIMED_HIGH_SCORE", endScore);
            editor.commit();

        }
        else {
            highScoreText.setText("High Score: " + highScore);
        }

        // home button - goes to home page
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(TimedEndActivity.this, MainActivity.class);
                startActivity(i);
                playButtonSoundEffect();
            }
        });

        // play again button - restarts timed mode game
        playAgain.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(TimedEndActivity.this, TimedGameActivity.class);
                startActivity(i);
                playButtonSoundEffect();
            }
        });

        // settings button - goes to settings page
        settings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(TimedEndActivity.this, SettingsActivity.class);
                startActivity(i);
                playButtonSoundEffect();
                finish();
            }
        });

        // shopping cart button - goes to ads page
        shoppingCart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(TimedEndActivity.this, ShoppingCartActivity.class);
                startActivity(i);
                playButtonSoundEffect();
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