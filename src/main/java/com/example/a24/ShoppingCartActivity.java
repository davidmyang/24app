package com.example.a24;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.ads.AdError;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.FullScreenContentCallback;
import com.google.android.gms.ads.LoadAdError;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.OnUserEarnedRewardListener;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;
import com.google.android.gms.ads.rewarded.RewardItem;
import com.google.android.gms.ads.rewarded.RewardedAd;
import com.google.android.gms.ads.rewarded.RewardedAdLoadCallback;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class ShoppingCartActivity extends AppCompatActivity {

    // declaring variables
    private RewardedAd mRewardedAd;
    private final String TAG = "ShoppingCartActivity";
    AdView mAdView;
    Button adButton;
    TextView numAnswersText;
    MediaPlayer buttonSoundEffect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_shopping_cart);

        // initializing variables
        adButton = findViewById(R.id.answerAdButton);
        numAnswersText = findViewById(R.id.totalAnswers);
        buttonSoundEffect = MediaPlayer.create(this, R.raw.sound);

        // retrieving user data for number of solutions available
        SharedPreferences prefs = this.getSharedPreferences("AD_GAME_DATA", Context.MODE_PRIVATE);
        int numAnswers = prefs.getInt("NUM_ANSWERS", 0);
        numAnswersText.setText("Number of Solutions Remaining: " + numAnswers);

        // initializing banner ad at bottom of screen
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // initializing reward ad - earn three solutions for every ad watched
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {

            }
        });

        loadRewardedAd();

        // ad button - plays ad video
        adButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showRewardAd();
                playButtonSoundEffect();
            }
        });

    }

    // function to load reward ad
    private void loadRewardedAd() {
        AdRequest adRequest = new AdRequest.Builder().build();

        RewardedAd.load(this, "ca-app-pub-3940256099942544/5224354917",
                adRequest, new RewardedAdLoadCallback() {
                    @Override
                    public void onAdFailedToLoad(@NonNull LoadAdError loadAdError) {
                        // Handle the error.
                        Log.d(TAG, loadAdError.getMessage());
                        mRewardedAd = null;
                    }

                    @Override
                    public void onAdLoaded(@NonNull RewardedAd rewardedAd) {
                        mRewardedAd = rewardedAd;
                        Log.d(TAG, "Ad was loaded.");

                        mRewardedAd.setFullScreenContentCallback(new FullScreenContentCallback() {
                            @Override
                            public void onAdShowedFullScreenContent() {
                                // Called when ad is shown.
                                Log.d(TAG, "Ad was shown.");
                            }

                            @Override
                            public void onAdFailedToShowFullScreenContent(AdError adError) {
                                // Called when ad fails to show.
                                Log.d(TAG, "Ad failed to show.");
                            }

                            @Override
                            public void onAdDismissedFullScreenContent() {
                                // Called when ad is dismissed.
                                // Set the ad reference to null so you don't show the ad a second time.
                                Log.d(TAG, "Ad was dismissed.");
                                mRewardedAd = null;
                                loadRewardedAd();
                                Context context = getApplicationContext();
                                CharSequence text = "Earned 3 ads!";
                                int duration = Toast.LENGTH_LONG;

                                Toast toast = Toast.makeText(context, text, duration);
                                toast.show();

                            }
                        });
                    }
                });
    }

    // function to show reward ad
    private void showRewardAd() {
        if (mRewardedAd != null) {
            Activity activityContext = ShoppingCartActivity.this;
            mRewardedAd.show(activityContext, new OnUserEarnedRewardListener() {
                @Override
                public void onUserEarnedReward(@NonNull RewardItem rewardItem) {
                    // Handle the reward.
                    Log.d(TAG, "The user earned the reward.");
                    int rewardAmount = rewardItem.getAmount();
                    String rewardType = rewardItem.getType();

                    setSolutionsRemaining();
                }
            });
        } else {
            Log.d(TAG, "The rewarded ad wasn't ready yet.");
        }
    }

    // function to show amount of solutions remaining for user
    private void setSolutionsRemaining() {
        SharedPreferences prefs = this.getSharedPreferences("AD_GAME_DATA", Context.MODE_PRIVATE);
        int numSolutions = prefs.getInt("NUM_ANSWERS", 0);

        SharedPreferences.Editor editor = prefs.edit();
        editor.putInt("NUM_ANSWERS", numSolutions + 3);
        editor.commit();

        numSolutions = prefs.getInt("NUM_ANSWERS", 0);
        numAnswersText.setText("Number of Answers Remaining: " + numSolutions);
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