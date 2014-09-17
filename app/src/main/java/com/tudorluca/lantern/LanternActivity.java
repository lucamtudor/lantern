package com.tudorluca.lantern;

import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.drawable.TransitionDrawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings.System;
import android.support.v7.app.ActionBarActivity;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.WindowManager;
import android.widget.Button;

import com.tudorluca.lantern.utils.SystemUiHider;

import org.jetbrains.annotations.NotNull;

public class LanternActivity extends ActionBarActivity {

    /**
     * Whether or not the system UI should be auto-hidden after {@link #AUTO_HIDE_DELAY_MILLIS} milliseconds.
     */
    private static boolean AUTO_HIDE = false;

    /**
     * If {@link #AUTO_HIDE} is set, the number of milliseconds to wait after user interaction before hiding the system
     * UI.
     */
    private static final int AUTO_HIDE_DELAY_MILLIS = 1800;

    /**
     * The instance of the {@link com.tudorluca.lantern.utils.SystemUiHider} for this activity.
     */
    private SystemUiHider mSystemUiHider;

    private Lantern mLantern;
    private TransitionDrawable mScreenBackground;
    private boolean mIsScreenOn;

    private Button mFlashButton;
    private Button mScreenButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mLantern = new Lantern(this);

        setContentView(R.layout.activity_home);
        setupView();

        SharedPreferences preferences = getSharedPreferences("Lantern", Context.MODE_PRIVATE);
        boolean isScreenOn = preferences.getBoolean("screen", false);
        boolean isLanternOn = preferences.getBoolean("lantern", false);

        powerScreen(isScreenOn);
        if (mLantern.hasLantern()) {
            powerLantern(isLanternOn);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.lantern, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id._action_about) {
            Intent intent = new Intent(this, AboutActivity.class);
            startActivity(intent);
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onResume() {
        super.onResume();
        if (mIsScreenOn) {
            setScreenBrightness(System.SCREEN_BRIGHTNESS_MODE_MANUAL, 255);
        }
    }

    @Override
    protected void onStop() {
        super.onStop();
        // Set screen brightness back to default.
        setScreenBrightness(-1, -1);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mLantern.release();

        SharedPreferences preferences = getSharedPreferences("Lantern", Context.MODE_PRIVATE);
        preferences.edit().putBoolean("screen", mScreenButton.isSelected())
                .putBoolean("lantern", mFlashButton.isSelected())
                .apply();
    }

    private void setupView() {
        final View controlsView = findViewById(R.id.fullscreen_content_controls);
        final View screen = findViewById(R.id.screen);
        mScreenBackground = (TransitionDrawable) screen.getBackground();

        // Set up an instance of SystemUiHider to control the system UI for
        // this activity.
        mSystemUiHider = SystemUiHider.getInstance(this, screen, SystemUiHider.FLAG_HIDE_NAVIGATION);
        mSystemUiHider.setup();
        mSystemUiHider.setOnVisibilityChangeListener(new SystemUiHider.OnVisibilityChangeListener() {
            // Cached values.
            int mControlsHeight;
            int mShortAnimTime;

            @Override
            @TargetApi(Build.VERSION_CODES.HONEYCOMB_MR2)
            public void onVisibilityChange(boolean visible) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB_MR2) {
                    // If the ViewPropertyAnimator API is available
                    // (Honeycomb MR2 and later), use it to animate the
                    // in-layout UI controls at the bottom of the
                    // screen.
                    if (mControlsHeight == 0) {
                        mControlsHeight = controlsView.getHeight();
                    }
                    if (mShortAnimTime == 0) {
                        mShortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);
                    }
                    controlsView.animate().translationY(visible ? 0 : mControlsHeight).setDuration(mShortAnimTime);
                } else {
                    // If the ViewPropertyAnimator APIs aren't
                    // available, simply show or hide the in-layout UI
                    // controls.
                    controlsView.setVisibility(visible ? View.VISIBLE : View.GONE);
                }

                if (visible && AUTO_HIDE) {
                    // Schedule a hide().
                    delayedHide(AUTO_HIDE_DELAY_MILLIS);
                }

                if (visible) {
                    getSupportActionBar().show();
                } else {
                    getSupportActionBar().hide();
                }
            }
        });

        // Set up the user interaction to manually show or hide the system UI.
        screen.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(@NotNull View view) {
                mSystemUiHider.toggle();
            }
        });

        mScreenButton = (Button) findViewById(R.id.screen_button);
        mScreenButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(@NotNull View v) {
                powerScreen(!mIsScreenOn);
            }
        });

        mFlashButton = (Button) findViewById(R.id.lantern_button);
        mFlashButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(@NotNull View v) {
                powerLantern(!mLantern.isOn());
            }
        });

        mFlashButton.setVisibility(mLantern.hasLantern() ? View.VISIBLE : View.GONE);

        AUTO_HIDE = mIsScreenOn;
    }

    private void powerScreen(boolean turnOn) {
        if (mIsScreenOn == turnOn) {
            return;
        }

        if (turnOn) {
            mScreenBackground.startTransition(100);
            setScreenBrightness(System.SCREEN_BRIGHTNESS_MODE_MANUAL, 255);
        } else {
            setScreenBrightness(-1, -1);
            mScreenBackground.reverseTransition(100);
        }

        String screenOnText = getResources().getString(R.string.screen_on);
        String screenOffText = getResources().getString(R.string.screen_off);
        mScreenButton.setSelected(turnOn);
        mScreenButton.setText(turnOn ? screenOnText : screenOffText);

        mScreenButton.setKeepScreenOn(turnOn);

        AUTO_HIDE = turnOn;
        if (AUTO_HIDE) {
            delayedHide(AUTO_HIDE_DELAY_MILLIS);
        } else {
            mHideHandler.removeCallbacks(mHideRunnable);
        }

        mIsScreenOn = turnOn;
    }

    private void powerLantern(boolean turnOn) {
        if (mLantern.isOn() == turnOn) {
            return;
        }

        mLantern.set(turnOn);

        String flashOnText = getResources().getString(R.string.lantern_on);
        String flashOffText = getResources().getString(R.string.lantern_off);
        mFlashButton.setSelected(turnOn);
        mFlashButton.setText(turnOn ? flashOnText : flashOffText);
        AUTO_HIDE = false;
    }

    private void setScreenBrightness(int mode, int brightness) {
        System.putInt(getContentResolver(), System.SCREEN_BRIGHTNESS_MODE, mode);
        System.putInt(getContentResolver(), System.SCREEN_BRIGHTNESS, brightness);
        WindowManager.LayoutParams layoutParams = getWindow().getAttributes();
        layoutParams.screenBrightness = brightness / (float) 255;
        getWindow().setAttributes(layoutParams);
    }

    Handler mHideHandler = new Handler();
    Runnable mHideRunnable = new Runnable() {
        @Override
        public void run() {
            mSystemUiHider.hide();
        }
    };

    /**
     * Schedules a call to hide() in [delay] milliseconds, canceling any previously scheduled calls.
     */
    private void delayedHide(int delayMillis) {
        mHideHandler.removeCallbacks(mHideRunnable);
        mHideHandler.postDelayed(mHideRunnable, delayMillis);
    }
}
