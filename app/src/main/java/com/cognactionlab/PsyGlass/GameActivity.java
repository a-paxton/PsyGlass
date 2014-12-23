package com.cognactionlab.PsyGlass;

import com.cognactionlab.PsyGlass.extra.SensorQueueRunnable;
import com.cognactionlab.PsyGlass.server.ServerConnect;
import com.cognactionlab.PsyGlass.server.ServerEvent;
import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.View;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

/**
 *
 * PsyGlass: An Open-Source Framework for Implementing Google Glass in Research Settings
 *
 * For more, see "PsyGlass: Capitalizing on Google Glass for Naturalistic Data Collection"
 * (Paxton, Rodriguez, & Dale, submitted).
 *
 */


public class GameActivity extends Activity implements ServerEvent, SensorEventListener {

    // change string to match target network address
    public static final String ADDRESS = "http://...";

    private static final int CONNECT_MESSAGE_DELAY = 5000; // milliseconds

    private static final int HANDLER_FAILURE = 0;
    private static final int HANDLER_CONNECT = 1;

    private ServerConnect server;

    private SensorManager sensorManager;
    private long startAccelEvent = -1; // nanoseconds

    private long timestamp = -1;

    Thread thread;
    SensorQueueRunnable queue;

    private View sessionColorView;
    private TextView sessionTextView;
    private View statusView;
    private ImageView imageView;
    private TextView textView;

    Handler postConnectHandler = new Handler() {
        public void handleMessage(Message msg) {
            switch(msg.what) {
                case HANDLER_FAILURE:
                    finish();
                    break;
                case HANDLER_CONNECT:
                    statusView.setVisibility(View.INVISIBLE);
                    break;
                default:
                    // Should never happen
            }
        }
    };

    // Gesture detector used to present the options menu
    private GestureDetector gestureDetector;
    private final GestureDetector.BaseListener baseListener = new GestureDetector.BaseListener() {
        @Override
        public boolean onGesture(Gesture gesture) {
            if (gesture == Gesture.TAP) {
                // Audio manager used to play system sound effects
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.playSoundEffect(Sounds.TAP);
                openOptionsMenu();
            }
            return true;
        }
    };



    @Override
    protected void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        View view = getLayoutInflater().inflate(R.layout.activity_game, null);
        setContentView(view);

        sessionColorView = view;
        sessionTextView = (TextView) view.findViewById(R.id.sessionTextView);
        statusView = view.findViewById(R.id.statusView);
        imageView = (ImageView) view.findViewById(R.id.imageView);
        textView = (TextView) view.findViewById(R.id.textView);

        gestureDetector = new GestureDetector(this);
        gestureDetector.setBaseListener(baseListener);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.uploading);
        imageView.startAnimation(animation);

        server = new ServerConnect(ADDRESS, this);
        server.connect();
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return gestureDetector.onMotionEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.game, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        endExperiment();
        return true;
    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        float screenX = event.values[0];
        float screenY = event.values[1];
        float screenZ = event.values[2];

        if (startAccelEvent == -1) {
            startAccelEvent = event.timestamp;
        }

        String message = (int) ((event.timestamp - startAccelEvent) / 1000 / 1000) +
                "," + screenX + "," + screenY + "," + screenZ;
        queue.addParcel(Sensor.TYPE_LINEAR_ACCELERATION, message);
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {
        // DO NOTHING
    }



    @Override
    public void onFailure() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.playSoundEffect(Sounds.ERROR);
        imageView.clearAnimation();
        imageView.setImageResource(R.drawable.ic_warning_50);
        textView.setText("No connection");
        statusView.setVisibility(View.VISIBLE);
        postConnectHandler.sendEmptyMessageDelayed(HANDLER_FAILURE, CONNECT_MESSAGE_DELAY);
        if (sensorManager != null) {
            endExperiment();
        }
    }

    @Override
    public void onConnect(int deviceId) {
        setPrefDeviceId(deviceId);
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.playSoundEffect(Sounds.SUCCESS);
        imageView.clearAnimation();
        imageView.setImageResource(R.drawable.ic_done_50);
        textView.setText("Connected");
        postConnectHandler.sendEmptyMessageDelayed(HANDLER_CONNECT, CONNECT_MESSAGE_DELAY);
    }

    @Override
    public void onRun(long timestamp) {
        setPrefTimestamp(timestamp);
        this.timestamp = timestamp;
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);
        Sensor accelerometer = sensorManager.getSensorList(Sensor.TYPE_LINEAR_ACCELERATION).get(0);

        queue = new SensorQueueRunnable();
        thread = new Thread(queue);
        thread.start();

        sensorManager.registerListener(this, accelerometer, SensorManager.SENSOR_DELAY_UI); // microseconds
        // SENSOR_DELAY_FASTEST = 0ms
        // SENSOR_DELAY_GAME = 20ms
        // SENSOR_DELAY_UI = 60ms
        // SENSOR_DELAY_NORMAL = 200ms
    }

    @Override
    public void onColorChange(String color) {
        sessionColorView.setBackgroundColor(Color.parseColor("#" + color));
    }

    @Override
    public void onTextChange(String text) {
        sessionTextView.setText(text);
    }

    @Override
    public void onFinish(long duration) {
        setPrefDuration(duration);
        sessionColorView.setBackgroundColor(Color.parseColor("#000000"));
        sessionTextView.setText("");
        sensorManager.unregisterListener(this); // unregisters ALL listeners
        sensorManager = null;
        queue.finish();
    }

    @Override
    public void onDisconnect() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.playSoundEffect(Sounds.SUCCESS);
        imageView.setImageResource(R.drawable.ic_done_50);
        textView.setText("Disconnected");
        statusView.setVisibility(View.VISIBLE);
        finish();
    }



    private void endExperiment() {
        AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
        audioManager.playSoundEffect(Sounds.DISMISSED);
        if (sensorManager != null) {
            setPrefDuration((System.currentTimeMillis() / 1000) - timestamp);
            sensorManager.unregisterListener(this);
            sensorManager = null;
            queue.finish();
            try {
                thread.join();
            } catch (Exception e) {
                // Something went wrong!
                e.printStackTrace();
            }
        }
        server.cancel();
        finish();
    }

    private void setPrefTimestamp(long timestamp) {
        SharedPreferences preferences = getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
        );
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("Timestamp", timestamp);
        editor.apply();
    }

    private void setPrefDuration(long duration) {
        SharedPreferences preferences = getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
        );
        SharedPreferences.Editor editor = preferences.edit();
        editor.putLong("Duration", duration);
        editor.apply();
    }

    private void setPrefDeviceId(int deviceId) {
        SharedPreferences preferences = getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
        );
        SharedPreferences.Editor editor = preferences.edit();
        editor.putInt("DeviceId", deviceId);
        editor.apply();
    }
}