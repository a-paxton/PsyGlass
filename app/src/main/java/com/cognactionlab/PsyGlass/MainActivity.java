package com.cognactionlab.PsyGlass;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;

/**
 *
 * PsyGlass: An Open-Source Framework for Implementing Google Glass in Research Settings
 *
 * For more, see "PsyGlass: Capitalizing on Google Glass for Naturalistic Data Collection"
 * (Paxton, Rodriguez, & Dale, submitted).
 *
 * Written by K. Rodriguez, A. Paxton, & R. Dale.
 * Created on 13 October 2014.
 * Last modified on 16 December 2014.
 *
 */

public class MainActivity extends Activity {

    // Gesture detector used to present the options menu
    private GestureDetector gestureDetector;

    // Gesture detector used to present the options menu
    private final GestureDetector.BaseListener baseListener = new GestureDetector.BaseListener() {
        @Override
        public boolean onGesture(Gesture gesture) {
            if (gesture == Gesture.TAP) {
                // Audio manager used to play system sound effects
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.playSoundEffect(Sounds.TAP);
                openOptionsMenu();
                return true;
            } else {
                return false;
            }
        }
    };

    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        setContentView(R.layout.activity_main);

        gestureDetector = new GestureDetector(this);
        gestureDetector.setBaseListener(baseListener);
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return gestureDetector.onMotionEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.start_experiment:
                start();
                return true;
            case R.id.settings:
                settings();
                return true;
            default:
                return false;
        }
    }

    private void start() {
        Intent intent = new Intent(this, GameActivity.class);
        startActivity(intent);
    }

    private void settings() {
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }

}
