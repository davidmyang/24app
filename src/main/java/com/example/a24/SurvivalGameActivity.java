package com.example.a24;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Handler;
import android.util.DisplayMetrics;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.AdView;
import com.google.android.gms.ads.MobileAds;
import com.google.android.gms.ads.initialization.InitializationStatus;
import com.google.android.gms.ads.initialization.OnInitializationCompleteListener;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.EmptyStackException;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;
import java.util.Stack;


public class SurvivalGameActivity extends AppCompatActivity {

    // algorithm for questions
    final String[] patterns = {"nnonnoo", "nnonono", "nnnoono", "nnnonoo",
            "nnnnooo"};
    final String ops = "+-*/^";

    String solution;
    List<Integer> digits;

    boolean evaluate(char[] line) throws Exception {
        Stack<Float> s = new Stack<>();
        try {
            for (char c : line) {
                if ('0' <= c && c <= '9')
                    s.push((float) c - '0');
                else
                    s.push(applyOperator(s.pop(), s.pop(), c));
            }
        } catch (EmptyStackException e) {
            throw new Exception("Invalid entry.");
        }
        return (Math.abs(24 - s.peek()) < 0.001F);
    }

    float applyOperator(float a, float b, char c) {
        switch (c) {
            case '+':
                return a + b;
            case '-':
                return b - a;
            case '*':
                return a * b;
            case '/':
                return b / a;
            default:
                return Float.NaN;
        }
    }

    List<Integer> randomDigits() {
        Random r = new Random();
        List<Integer> result = new ArrayList<>(4);
        for (int i = 0; i < 4; i++)
            result.add(r.nextInt(9) + 1);
        return result;
    }

    List<Integer> getSolvableDigits() {
        List<Integer> result;
        do {
            result = randomDigits();
        } while (!isSolvable(result));
        return result;
    }

    boolean isSolvable(List<Integer> digits) {
        Set<List<Integer>> dPerms = new HashSet<>(4 * 3 * 2);
        permute(digits, dPerms, 0);

        int total = 4 * 4 * 4;
        List<List<Integer>> oPerms = new ArrayList<>(total);
        permuteOperators(oPerms, 4, total);

        StringBuilder sb = new StringBuilder(4 + 3);

        for (String pattern : patterns) {
            char[] patternChars = pattern.toCharArray();

            for (List<Integer> dig : dPerms) {
                for (List<Integer> opr : oPerms) {

                    int i = 0, j = 0;
                    for (char c : patternChars) {
                        if (c == 'n')
                            sb.append(dig.get(i++));
                        else
                            sb.append(ops.charAt(opr.get(j++)));
                    }

                    String candidate = sb.toString();
                    try {
                        if (evaluate(candidate.toCharArray())) {
                            solution = postfixToInfix(candidate);
                            return true;
                        }
                    } catch (Exception ignored) {
                    }
                    sb.setLength(0);
                }
            }
        }
        return false;
    }

    String postfixToInfix(String postfix) {
        class Expression {
            String op, ex;
            int prec = 3;

            Expression(String e) {
                ex = e;
            }

            Expression(String e1, String e2, String o) {
                ex = String.format("%s %s %s", e1, o, e2);
                op = o;
                prec = ops.indexOf(o) / 2;
            }
        }

        Stack<Expression> expr = new Stack<>();

        for (char c : postfix.toCharArray()) {
            int idx = ops.indexOf(c);
            if (idx != -1) {

                Expression r = expr.pop();
                Expression l = expr.pop();

                int opPrec = idx / 2;

                if (l.prec < opPrec)
                    l.ex = '(' + l.ex + ')';

                if (r.prec <= opPrec)
                    r.ex = '(' + r.ex + ')';

                expr.push(new Expression(l.ex, r.ex, "" + c));
            } else {
                expr.push(new Expression("" + c));
            }
        }
        return expr.peek().ex;
    }

    char[] infixToPostfix(char[] infix) throws Exception {
        StringBuilder sb = new StringBuilder();
        Stack<Integer> s = new Stack<>();
        try {
            for (char c : infix) {
                int idx = ops.indexOf(c);
                if (idx != -1) {
                    if (s.isEmpty())
                        s.push(idx);
                    else {
                        while (!s.isEmpty()) {
                            int prec2 = s.peek() / 2;
                            int prec1 = idx / 2;
                            if (prec2 >= prec1)
                                sb.append(ops.charAt(s.pop()));
                            else
                                break;
                        }
                        s.push(idx);
                    }
                } else if (c == '(') {
                    s.push(-2);
                } else if (c == ')') {
                    while (s.peek() != -2)
                        sb.append(ops.charAt(s.pop()));
                    s.pop();
                } else {
                    sb.append(c);
                }
            }
            while (!s.isEmpty())
                sb.append(ops.charAt(s.pop()));

        } catch (EmptyStackException e) {
            throw new Exception("Invalid entry.");
        }
        return sb.toString().toCharArray();
    }

    void permute(List<Integer> lst, Set<List<Integer>> res, int k) {
        for (int i = k; i < lst.size(); i++) {
            Collections.swap(lst, i, k);
            permute(lst, res, k + 1);
            Collections.swap(lst, k, i);
        }
        if (k == lst.size())
            res.add(new ArrayList<>(lst));
    }

    void permuteOperators(List<List<Integer>> res, int n, int total) {
        for (int i = 0, npow = n * n; i < total; i++)
            res.add(Arrays.asList((i / npow), (i % npow) / n, i % n));
    }

    // declaring variables
    String a, b;

    boolean usedNum1, usedNum2, usedNum3, usedNum4;
    boolean num1Clicked, num2Clicked, num3Clicked, num4Clicked;
    boolean addClicked, subClicked, mulClicked, divClicked;

    TextView fill, incorrect, correct, score, timer, numAnswersText, addTime;
    Button num1, num2, num3, num4, add, sub, mul, div;
    ImageButton reset, rules, home, solutionBtn;

    CountDownTimer mTimer = null;
    long timeRemaining;
    boolean timerStarted = false;

    AdView mAdView;
    MediaPlayer buttonSoundEffect, correctSoundEffect;
    private RelativeLayout back_dim_layout;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_survival_game);

        // initializing variables
        fill = findViewById(R.id.fillOut);
        correct = findViewById(R.id.correctText);
        incorrect = findViewById(R.id.incorrectText);
        score = findViewById(R.id.scoreText);
        numAnswersText = findViewById(R.id.numAnswersText);
        timer = findViewById(R.id.timerText);
        addTime = findViewById(R.id.addTimeText);

        num1 = findViewById(R.id.buttonNum1);
        num2 = findViewById(R.id.buttonNum2);
        num3 = findViewById(R.id.buttonNum3);
        num4 = findViewById(R.id.buttonNum4);

        add = findViewById(R.id.buttonOperatorAdd);
        sub = findViewById(R.id.buttonOperatorSub);
        mul = findViewById(R.id.buttonOperatorMul);
        div = findViewById(R.id.buttonOperatorDiv);

        reset = findViewById(R.id.resetButton);
        home = findViewById(R.id.gameBackButton);
        rules = findViewById(R.id.rulesButton);
        back_dim_layout = findViewById(R.id.bg_dim_layout);
        solutionBtn = findViewById(R.id.solutionButton);

        buttonSoundEffect = MediaPlayer.create(this, R.raw.sound);
        correctSoundEffect = MediaPlayer.create(this, R.raw.correct);

        usedNum1 = false;
        usedNum2 = false;
        usedNum3 = false;
        usedNum4 = false;

        num1Clicked = false;
        num2Clicked = false;
        num3Clicked = false;
        num4Clicked = false;

        addClicked = false;
        subClicked = false;
        mulClicked = false;
        divClicked = false;

        timerStarted = true;

        // setting button colors
        num1.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
        num1.setTextColor(getResources().getColor(R.color.purple_200));
        num2.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
        num2.setTextColor(getResources().getColor(R.color.purple_200));
        num3.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
        num3.setTextColor(getResources().getColor(R.color.purple_200));
        num4.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
        num4.setTextColor(getResources().getColor(R.color.purple_200));

        add.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
        sub.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
        mul.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
        div.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));

        // setting the four numbers
        digits = getSolvableDigits();
        num1.setText(String.valueOf(digits.get(0)));
        num2.setText(String.valueOf(digits.get(1)));
        num3.setText(String.valueOf(digits.get(2)));
        num4.setText(String.valueOf(digits.get(3)));

        // retrieving user data for number of solutions
        SharedPreferences prefs = this.getSharedPreferences("AD_GAME_DATA", Context.MODE_PRIVATE);
        final int[] numAnswers = {prefs.getInt("NUM_ANSWERS", 0)};
        numAnswersText.setText("× " + numAnswers[0]);

        // initializing banner ad at bottom of screen
        MobileAds.initialize(this, new OnInitializationCompleteListener() {
            @Override
            public void onInitializationComplete(InitializationStatus initializationStatus) {
            }
        });

        mAdView = findViewById(R.id.adView);
        AdRequest adRequest = new AdRequest.Builder().build();
        mAdView.loadAd(adRequest);

        // home button - goes back to main activity
        home.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AlertDialog.Builder(SurvivalGameActivity.this)
                        .setTitle("Confirm Exit")
                        .setMessage("Are you sure you want to exit this mode?")
                        .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                        {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent i = new Intent(SurvivalGameActivity.this, MainActivity.class);
                                startActivity(i);
                                playButtonSoundEffect();
                                finish();
                            }

                        })
                        .setNegativeButton("No", null)
                        .show();
                playButtonSoundEffect();
            }
        });

        // solution button - shows solution
        solutionBtn.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // checks if user has enough remaining solutions
                if (numAnswers[0] > 0 && fill.length() == 0) {
                    fill.setText(solution);
                    fill.setVisibility(View.VISIBLE);

                    SharedPreferences.Editor editor = prefs.edit();
                    editor.putInt("NUM_ANSWERS", numAnswers[0] - 1);
                    editor.commit();

                    numAnswers[0] = prefs.getInt("NUM_ANSWERS", 0);

                    numAnswersText.setText("× " + numAnswers[0]);
                }
                else if (fill.length() > 0){
                    Context context = getApplicationContext();
                    CharSequence text = "Solution is already shown!";
                    int duration = Toast.LENGTH_SHORT;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                else {
                    Context context = getApplicationContext();
                    CharSequence text = "You have run out of solutions. Watch a quick ad to earn more!";
                    int duration = Toast.LENGTH_LONG;

                    Toast toast = Toast.makeText(context, text, duration);
                    toast.show();
                }
                playButtonSoundEffect();
            }
        });

        // reset button - resets everything in current question
        reset.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                resetQuestion();
                playButtonSoundEffect();
            }
        });

        // rules button - shows rules popup window
        rules.setOnClickListener(new View.OnClickListener() {
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

        // num1 button - button for top left number
        num1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playButtonSoundEffect();
                num1Clicked = true;
                num1.setBackgroundColor(getResources().getColor(R.color.purple_200));
                num1.setTextColor(getResources().getColor(R.color.buttonBGColor));
                num2.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                num2.setTextColor(getResources().getColor(R.color.purple_200));
                num3.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                num3.setTextColor(getResources().getColor(R.color.purple_200));
                num4.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                num4.setTextColor(getResources().getColor(R.color.purple_200));

                if ((num2Clicked || num3Clicked || num4Clicked) && (addClicked || subClicked || mulClicked || divClicked)) {
                    b = String.valueOf(num1.getText());

                    if (num2Clicked) {
                        num2.setVisibility(View.INVISIBLE);
                        usedNum2 = true;
                    }
                    else if (num3Clicked) {
                        num3.setVisibility(View.INVISIBLE);
                        usedNum3 = true;
                    }
                    else {
                        num4.setVisibility(View.INVISIBLE);
                        usedNum4 = true;
                    }
                    num1.setText(calculate());
                    a = String.valueOf(num1.getText());
                    addClicked = false;
                    subClicked = false;
                    mulClicked = false;
                    divClicked = false;

                    add.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                    sub.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                    mul.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                    div.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                }
                else {
                    a = String.valueOf(num1.getText());
                }
                num2Clicked = false;
                num3Clicked = false;
                num4Clicked = false;
                num2.setPressed(false);
                num3.setPressed(false);
                num4.setPressed(false);
                checkEqualTo24();

            }
        });

        // num2 button - button for top right number
        num2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playButtonSoundEffect();
                num2Clicked = true;
                num2.setBackgroundColor(getResources().getColor(R.color.purple_200));
                num2.setTextColor(getResources().getColor(R.color.buttonBGColor));
                num1.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                num1.setTextColor(getResources().getColor(R.color.purple_200));
                num3.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                num3.setTextColor(getResources().getColor(R.color.purple_200));
                num4.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                num4.setTextColor(getResources().getColor(R.color.purple_200));

                if ((num1Clicked || num3Clicked || num4Clicked) && (addClicked || subClicked || mulClicked || divClicked)) {
                    b = String.valueOf(num2.getText());

                    if (num1Clicked) {
                        num1.setVisibility(View.INVISIBLE);
                        usedNum1 = true;
                    }
                    else if (num3Clicked) {
                        num3.setVisibility(View.INVISIBLE);
                        usedNum3 = true;
                    }
                    else {
                        num4.setVisibility(View.INVISIBLE);
                        usedNum4 = true;
                    }
                    num2.setText(calculate());
                    a = String.valueOf(num2.getText());
                    addClicked = false;
                    subClicked = false;
                    mulClicked = false;
                    divClicked = false;

                    add.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                    sub.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                    mul.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                    div.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                }
                else {
                    a = String.valueOf(num2.getText());
                }
                num1Clicked = false;
                num3Clicked = false;
                num4Clicked = false;
                num1.setPressed(false);
                num3.setPressed(false);
                num4.setPressed(false);
                checkEqualTo24();
            }
        });

        // num3 button - button for bottom left number
        num3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playButtonSoundEffect();
                num3Clicked = true;
                num3.setBackgroundColor(getResources().getColor(R.color.purple_200));
                num3.setTextColor(getResources().getColor(R.color.buttonBGColor));
                num1.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                num1.setTextColor(getResources().getColor(R.color.purple_200));
                num2.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                num2.setTextColor(getResources().getColor(R.color.purple_200));
                num4.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                num4.setTextColor(getResources().getColor(R.color.purple_200));

                if ((num1Clicked || num2Clicked || num4Clicked) && (addClicked || subClicked || mulClicked || divClicked)) {
                    b = String.valueOf(num3.getText());
                    if (num1Clicked) {
                        num1.setVisibility(View.INVISIBLE);
                        usedNum1 = true;
                    }
                    else if (num2Clicked) {
                        num2.setVisibility(View.INVISIBLE);
                        usedNum2 = true;
                    }
                    else {
                        num4.setVisibility(View.INVISIBLE);
                        usedNum4 = true;
                    }
                    num3.setText(calculate());
                    a = String.valueOf(num3.getText());
                    addClicked = false;
                    subClicked = false;
                    mulClicked = false;
                    divClicked = false;

                    add.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                    sub.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                    mul.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                    div.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                }
                else {
                    a = String.valueOf(num3.getText());
                }
                num1Clicked = false;
                num2Clicked = false;
                num4Clicked = false;
                num1.setPressed(false);
                num2.setPressed(false);
                num4.setPressed(false);
                checkEqualTo24();
            }
        });

        // num4 button - button for bottom right number
        num4.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playButtonSoundEffect();
                num4Clicked = true;
                num4.setBackgroundColor(getResources().getColor(R.color.purple_200));
                num4.setTextColor(getResources().getColor(R.color.buttonBGColor));
                num1.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                num1.setTextColor(getResources().getColor(R.color.purple_200));
                num2.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                num2.setTextColor(getResources().getColor(R.color.purple_200));
                num3.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                num3.setTextColor(getResources().getColor(R.color.purple_200));

                if ((num1Clicked || num2Clicked || num3Clicked) && (addClicked || subClicked || mulClicked || divClicked)) {
                    b = String.valueOf(num4.getText());

                    if (num1Clicked) {
                        num1.setVisibility(View.INVISIBLE);
                        usedNum1 = true;
                    }
                    else if (num2Clicked) {
                        num2.setVisibility(View.INVISIBLE);
                        usedNum2 = true;
                    }
                    else {
                        num3.setVisibility(View.INVISIBLE);
                        usedNum3 = true;
                    }
                    num4.setText(calculate());
                    a = String.valueOf(num4.getText());
                    addClicked = false;
                    subClicked = false;
                    mulClicked = false;
                    divClicked = false;

                    add.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                    sub.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                    mul.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                    div.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                }
                else {
                    a = String.valueOf(num4.getText());
                }
                num2Clicked = false;
                num3Clicked = false;
                num1Clicked = false;
                num2.setPressed(false);
                num3.setPressed(false);
                num1.setPressed(false);
                checkEqualTo24();
            }
        });

        // addition button
        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playButtonSoundEffect();
                addClicked = true;
                add.setBackgroundColor(getResources().getColor(R.color.purple_200));
                sub.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                mul.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                div.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));

                subClicked = false;
                mulClicked = false;
                divClicked = false;
                sub.setPressed(false);
                mul.setPressed(false);
                div.setPressed(false);
            }
        });

        // subtraction button
        sub.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playButtonSoundEffect();
                subClicked = true;
                add.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                sub.setBackgroundColor(getResources().getColor(R.color.purple_200));
                mul.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                div.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));

                addClicked = false;
                mulClicked = false;
                divClicked = false;
                add.setPressed(false);
                mul.setPressed(false);
                div.setPressed(false);
            }
        });

        // multiplication button
        mul.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playButtonSoundEffect();
                mulClicked = true;
                add.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                sub.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                mul.setBackgroundColor(getResources().getColor(R.color.purple_200));
                div.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));

                subClicked = false;
                addClicked = false;
                divClicked = false;
                sub.setPressed(false);
                add.setPressed(false);
                div.setPressed(false);
            }
        });

        // division button
        div.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                playButtonSoundEffect();
                divClicked = true;
                add.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                sub.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                mul.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
                div.setBackgroundColor(getResources().getColor(R.color.purple_200));

                subClicked = false;
                mulClicked = false;
                addClicked = false;
                sub.setPressed(false);
                mul.setPressed(false);
                add.setPressed(false);
            }
        });

        // function for countdown timer on the top of screen
        mTimer = new CountDownTimer(60000, 1000) {

            public void onTick(long millisUntilFinished) {
                timer.setText(String.format("%d:%02d",(millisUntilFinished / 60000), (millisUntilFinished % 60000 / 1000)));
            }

            // calls getDetails functions when timer reaches 0:00
            public void onFinish() {
                getDetails();
            }
        }.start();
    }

    // function calculates expression everytime two numbers and an operator are pressed
    public String calculate() {
        if (addClicked) {

            if (a.contains("/")) {
                int divIndex = a.indexOf("/");
                int denominator = Integer.parseInt(a.substring(divIndex + 1));
                int convert = denominator * Integer.parseInt(b);
                int numerator = Integer.parseInt(a.substring(0, divIndex)) + convert;
                int gcd = gcdFunction(numerator, denominator);
                if (gcd < 0) {
                    gcd = Math.abs(gcd);
                }
                numerator /= gcd;
                denominator /= gcd;
                return Integer.toString(numerator) + "/" + Integer.toString(denominator);
            }
            else if (b.contains("/")) {
                int divIndex = b.indexOf("/");
                int denominator = Integer.parseInt(b.substring(divIndex + 1));
                int convert = denominator * Integer.parseInt(a);
                int numerator = Integer.parseInt(b.substring(0, divIndex)) + convert;
                int gcd = gcdFunction(numerator, denominator);
                if (gcd < 0) {
                    gcd = Math.abs(gcd);
                }
                numerator /= gcd;
                denominator /= gcd;
                return Integer.toString(numerator) + "/" + Integer.toString(denominator);
            }
            else {
                return Integer.toString(Integer.parseInt(a) + Integer.parseInt(b));
            }
        }
        else if (mulClicked) {
            if (a.contains("/")) {
                int divIndex = a.indexOf("/");
                int denominator = Integer.parseInt(a.substring(divIndex + 1));
                int numerator = Integer.parseInt(a.substring(0, divIndex)) * Integer.parseInt(b);
                int gcd = gcdFunction(numerator, denominator);
                if (gcd < 0) {
                    gcd = Math.abs(gcd);
                }
                numerator /= gcd;
                denominator /= gcd;
                if (denominator == 1) {
                    return Integer.toString(numerator);
                }
                else {
                    return (numerator + "/" + denominator);
                }
            }
            else if (b.contains("/")) {
                int divIndex = b.indexOf("/");
                int denominator = Integer.parseInt(b.substring(divIndex + 1));
                int numerator = Integer.parseInt(b.substring(0, divIndex)) * Integer.parseInt(a);
                int gcd = gcdFunction(numerator, denominator);
                if (gcd < 0) {
                    gcd = Math.abs(gcd);
                }
                numerator /= gcd;
                denominator /= gcd;
                if (denominator == 1) {
                    return Integer.toString(numerator);
                }
                else {
                    return (numerator + "/" + denominator);
                }
            }
            else {
                return Integer.toString(Integer.parseInt(a) * Integer.parseInt(b));
            }
        }
        else if (divClicked) {
            if (a.contains("/")) {
                int divIndex = a.indexOf("/");
                int numerator = Integer.parseInt(a.substring(0, divIndex));
                int denominator = Integer.parseInt(a.substring(divIndex + 1)) * Integer.parseInt(b);
                int gcd = gcdFunction(numerator, denominator);
                if (gcd < 0) {
                    gcd = Math.abs(gcd);
                }
                numerator /= gcd;
                denominator /= gcd;
                return (numerator + "/" + denominator);
            }
            else if (b.contains("/")) {
                int divIndex = b.indexOf("/");
                int numerator = Integer.parseInt(b.substring(0, divIndex));
                int denominator = Integer.parseInt(b.substring(divIndex + 1)) * Integer.parseInt(a);
                int gcd = gcdFunction(numerator, denominator);
                if (gcd < 0) {
                    gcd = Math.abs(gcd);
                }
                numerator /= gcd;
                denominator /= gcd;
                return (numerator + "/" + denominator);
            }
            else if ((Integer.parseInt(a) % Integer.parseInt(b) != 0)) {
                int numerator = Integer.parseInt(a);
                int denominator = Integer.parseInt(b);
                int gcd = gcdFunction(numerator, denominator);
                if (gcd < 0) {
                    gcd = Math.abs(gcd);
                }
                numerator /= gcd;
                denominator /= gcd;
                return (numerator + "/" + denominator);
            }
            else {
                return Integer.toString(Integer.parseInt(a) / Integer.parseInt(b));
            }
        }
        else if (subClicked) {
            if (a.contains("/")) {
                int divIndex = a.indexOf("/");
                int denominator = Integer.parseInt(a.substring(divIndex + 1));
                int convert = denominator * Integer.parseInt(b);
                int numerator = Integer.parseInt(a.substring(0, divIndex)) - convert;
                int gcd = gcdFunction(numerator, denominator);
                if (gcd < 0) {
                    gcd = Math.abs(gcd);
                }
                numerator /= gcd;
                denominator /= gcd;
                return (numerator + "/" + denominator);
            } else if (b.contains("/")) {
                int divIndex = b.indexOf("/");
                int denominator = Integer.parseInt(b.substring(divIndex + 1));
                int convert = denominator * Integer.parseInt(a);
                int numerator = convert - Integer.parseInt(b.substring(0, divIndex));
                int gcd = gcdFunction(numerator, denominator);
                if (gcd < 0) {
                    gcd = Math.abs(gcd);
                }
                numerator /= gcd;
                denominator /= gcd;
                return (numerator + "/" + denominator);
            } else {
                return Integer.toString(Integer.parseInt(a) - Integer.parseInt(b));
            }
        }
        return null;
    }

    // gcd function to simply fractions
    public int gcdFunction (int a, int b) {
        if (b == 0) {
            return a;
        }
        return gcdFunction(b, a%b);
    }

    // function to check if answer is 24
    public void checkEqualTo24() {
        if ((usedNum1 && usedNum2 && usedNum3) || (usedNum1 && usedNum2 && usedNum4) || (usedNum1 && usedNum3 && usedNum4) || (usedNum2 && usedNum3 && usedNum4)) {
            if (num1Clicked) {
                if (String.valueOf(num1.getText()).equals("24")) {
                    int numCorrect = Integer.parseInt(String.valueOf(score.getText())) + 1;
                    playCorrectSoundEffect();
                    score.setText(Integer.toString(numCorrect));
                    correct.setVisibility(View.VISIBLE);
                    addTime.setVisibility(View.VISIBLE);
                    newTimer();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            newQuestion();
                        }
                    }, 2000);
                }
                else {
                    incorrect.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            resetQuestion();
                        }
                    }, 2000);
                }
            }
            if (num2Clicked) {
                if (String.valueOf(num2.getText()).equals("24")) {
                    int numCorrect = Integer.parseInt(String.valueOf(score.getText())) + 1;
                    playCorrectSoundEffect();
                    score.setText(Integer.toString(numCorrect));
                    correct.setVisibility(View.VISIBLE);
                    addTime.setVisibility(View.VISIBLE);
                    newTimer();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            newQuestion();
                        }
                    }, 2000);
                }
                else {
                    incorrect.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            resetQuestion();
                        }
                    }, 2000);
                }
            }
            if (num3Clicked) {
                if (String.valueOf(num3.getText()).equals("24")) {
                    int numCorrect = Integer.parseInt(String.valueOf(score.getText())) + 1;
                    playCorrectSoundEffect();
                    score.setText(Integer.toString(numCorrect));
                    correct.setVisibility(View.VISIBLE);
                    addTime.setVisibility(View.VISIBLE);
                    newTimer();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            newQuestion();
                        }
                    }, 2000);
                }
                else {
                    incorrect.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            resetQuestion();
                        }
                    }, 2000);
                }
            }
            if (num4Clicked) {
                if (String.valueOf(num4.getText()).equals("24")) {
                    int numCorrect = Integer.parseInt(String.valueOf(score.getText())) + 1;
                    playCorrectSoundEffect();
                    score.setText(Integer.toString(numCorrect));
                    correct.setVisibility(View.VISIBLE);
                    addTime.setVisibility(View.VISIBLE);
                    newTimer();
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            newQuestion();
                        }
                    }, 2000);
                }
                else {
                    incorrect.setVisibility(View.VISIBLE);
                    new Handler().postDelayed(new Runnable() {
                        @Override
                        public void run() {
                            resetQuestion();
                        }
                    }, 2000);
                }
            }
        }
    }

    // function to set new question
    public void newQuestion() {
        digits = getSolvableDigits();
        num1.setText(String.valueOf(digits.get(0)));
        num2.setText(String.valueOf(digits.get(1)));
        num3.setText(String.valueOf(digits.get(2)));
        num4.setText(String.valueOf(digits.get(3)));

        num1.setVisibility(View.VISIBLE);
        num2.setVisibility(View.VISIBLE);
        num3.setVisibility(View.VISIBLE);
        num4.setVisibility(View.VISIBLE);

        correct.setVisibility(View.INVISIBLE);
        incorrect.setVisibility(View.INVISIBLE);
        fill.setText("");
        fill.setVisibility(View.INVISIBLE);
        addTime.setVisibility(View.INVISIBLE);

        usedNum1 = false;
        usedNum2 = false;
        usedNum3 = false;
        usedNum4 = false;

        num1Clicked = false;
        num2Clicked = false;
        num3Clicked = false;
        num4Clicked = false;

        addClicked = false;
        subClicked = false;
        mulClicked = false;
        divClicked = false;

        num1.setEnabled(true);
        num2.setEnabled(true);
        num3.setEnabled(true);
        num4.setEnabled(true);

        num1.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
        num1.setTextColor(getResources().getColor(R.color.purple_200));
        num2.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
        num2.setTextColor(getResources().getColor(R.color.purple_200));
        num3.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
        num3.setTextColor(getResources().getColor(R.color.purple_200));
        num4.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
        num4.setTextColor(getResources().getColor(R.color.purple_200));

        add.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
        sub.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
        mul.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
        div.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));

    }

    // function to reset current question
    public void resetQuestion() {
        num1.setText(String.valueOf(digits.get(0)));
        num2.setText(String.valueOf(digits.get(1)));
        num3.setText(String.valueOf(digits.get(2)));
        num4.setText(String.valueOf(digits.get(3)));

        num1.setVisibility(View.VISIBLE);
        num2.setVisibility(View.VISIBLE);
        num3.setVisibility(View.VISIBLE);
        num4.setVisibility(View.VISIBLE);

        num1.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
        num1.setTextColor(getResources().getColor(R.color.purple_200));
        num2.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
        num2.setTextColor(getResources().getColor(R.color.purple_200));
        num3.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
        num3.setTextColor(getResources().getColor(R.color.purple_200));
        num4.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
        num4.setTextColor(getResources().getColor(R.color.purple_200));

        add.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
        sub.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
        mul.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));
        div.setBackgroundColor(getResources().getColor(R.color.buttonBGColor));

        usedNum1 = false;
        usedNum2 = false;
        usedNum3 = false;
        usedNum4 = false;

        num1Clicked = false;
        num2Clicked = false;
        num3Clicked = false;
        num4Clicked = false;

        addClicked = false;
        subClicked = false;
        mulClicked = false;
        divClicked = false;

        num1.setEnabled(true);
        num2.setEnabled(true);
        num3.setEnabled(true);
        num4.setEnabled(true);

        correct.setVisibility(View.INVISIBLE);
        incorrect.setVisibility(View.INVISIBLE);
        addTime.setVisibility(View.INVISIBLE);
    }

    // function to create new timer with +15 seconds everytime user solves a question
    public void newTimer() {
        mTimer.cancel();
        getTimerText();
        mTimer = new CountDownTimer(timeRemaining + 15000, 1000) {

            public void onTick(long millisUntilFinished) {

                timer.setText(String.format("%d:%02d",(millisUntilFinished / 60000), (millisUntilFinished % 60000 / 1000)));
            }

            // calls getDetails functions when timer reaches 0:00
            public void onFinish() {
                getDetails();
            }
        }.start();
    }

    // function to get current amount of time remaining on timer
    public void getTimerText() {
        String timerText = String.valueOf(timer.getText());
        int colonIndex = timerText.indexOf(":");
        timeRemaining = (Long.parseLong(timerText.substring(0, colonIndex)) * 60000) + (Long.parseLong(timerText.substring(colonIndex + 1)) * 1000);

    }

    // function to send user's ending score to end activity
    public void getDetails() {
        // saving score to string
        int endScore = Integer.parseInt(score.getText().toString());

        // creating intent
        Intent i = new Intent(SurvivalGameActivity.this, SurvivalEndActivity.class);
        i.putExtra("SCORE", endScore);
        startActivity(i);
    }

    // cancels current timer and saves timer text when user leaves app
    @Override
    public void onPause() {
        super.onPause();
        mTimer.cancel();
        getTimerText();
        timerStarted = false;
    }

    // creates new timer with previous timer text when user goes back to app
    public void onResume() {
        super.onResume();
        if (timerStarted == false) {
            mTimer = new CountDownTimer(timeRemaining, 1000) {
                public void onTick(long millisUntilFinished) {

                    timer.setText(String.format("%d:%02d", (millisUntilFinished / 60000), (millisUntilFinished % 60000 / 1000)));
                }

                // calls getDetails functions when timer reaches 0:00
                public void onFinish() {
                    getDetails();
                }
            }.start();
        }
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

    // function for correct answer sound effect
    public void playCorrectSoundEffect() {
        SharedPreferences prefs6 = this.getSharedPreferences("SOUND_GAME_DATA", Context.MODE_PRIVATE);
        int soundMode = prefs6.getInt("SOUND_MODE", 0);

        if (soundMode == 0) {
            correctSoundEffect.start();
        }
    }

    // function to exit to main activity when back button pressed
    public void onBackPressed() {
        // creates popup window to confirm exit
        new AlertDialog.Builder(this)
                .setTitle("Confirm Exit")
                .setMessage("Are you sure you want to exit this mode?")
                .setPositiveButton("Yes", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        playButtonSoundEffect();
                        finish();
                    }

                })
                .setNegativeButton("No", null)
                .show();
        playButtonSoundEffect();
    }
}
