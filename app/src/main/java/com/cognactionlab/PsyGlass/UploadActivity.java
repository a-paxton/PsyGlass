package com.cognactionlab.PsyGlass;

import com.google.android.glass.media.Sounds;
import com.google.android.glass.touchpad.Gesture;
import com.google.android.glass.touchpad.GestureDetector;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Environment;
import android.view.Menu;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.TextView;

import java.io.DataOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.net.HttpURLConnection;
import java.net.URL;

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

public class UploadActivity extends Activity {

    ImageView imageView;
    TextView textView;

    boolean success = false;

    private UploadTask task;

    private GestureDetector gestureDetector;

    private final GestureDetector.BaseListener baseListener = new GestureDetector.BaseListener() {
        public boolean onGesture(Gesture gesture) {
            if (task.isCancelled()) {
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.playSoundEffect(Sounds.DISALLOWED);
                return true;
            } else if (gesture == Gesture.TAP) {
                // Audio manager used to play system sound effects
                AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
                audioManager.playSoundEffect(Sounds.TAP);
                openOptionsMenu();
                return true;
            } else if (gesture == Gesture.SWIPE_DOWN) {
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
        setContentView(R.layout.activity_upload);
        imageView = (ImageView) findViewById(R.id.imageView);
        textView = (TextView) findViewById(R.id.textView);

        Animation animation = AnimationUtils.loadAnimation(this, R.anim.uploading);
        imageView.startAnimation(animation);

        gestureDetector = new GestureDetector(this);
        gestureDetector.setBaseListener(baseListener);

        task = new UploadTask(getPrefDeviceId(), getPrefTimestamp());
        task.execute();
    }

    @Override
    public boolean onGenericMotionEvent(MotionEvent event) {
        return gestureDetector.onMotionEvent(event);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.upload, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        task.cancel(true);
        textView.setText("Canceling...");

        return true;
    }



    private int getPrefDeviceId() {
        SharedPreferences preferences = getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
        );
        return preferences.getInt("DeviceId", -1);
    }

    private long getPrefTimestamp() {
        SharedPreferences preferences = getSharedPreferences(
                getString(R.string.preference_file_key),
                Context.MODE_PRIVATE
        );
        return preferences.getLong("Timestamp", -1);
    }



    private class UploadTask extends AsyncTask<Void, Integer, Void> {

        private int deviceId;
        private long timestamp;

        public UploadTask(int deviceId, long timestamp) {
            this.deviceId = deviceId;
            this.timestamp = timestamp;
        }

        @Override
        protected Void doInBackground(Void... params) {
            // Connect to server and send data
            String address = GameActivity.ADDRESS + "/resources/device_data.php";
            HttpURLConnection connection;
            DataOutputStream outputStream;

            try {
                URL url = new URL(address);
                connection = (HttpURLConnection) url.openConnection();
                connection.setConnectTimeout(4000);
                connection.setDoOutput(true);
                connection.setChunkedStreamingMode(1024);
                connection.setRequestMethod("POST"); // default when setDoOutput is set to true

                outputStream = new DataOutputStream(connection.getOutputStream());
                outputStream.writeBytes(deviceId + "\n");
                outputStream.writeBytes(timestamp + "\n");
                writeFileToStream(outputStream, "PsyGlass.txt");
                outputStream.writeBytes("\n");

                outputStream.flush();
                outputStream.close();
            } catch (Exception e) {
                e.printStackTrace();
                success = false;
                return null;
            }

            success = true;
            return null;
        }

        private void writeFileToStream(DataOutputStream outputStream, String file) throws Exception {
            if (isCancelled()) {
                return;
            }

            String path = Environment.getExternalStorageDirectory().getAbsolutePath() + "/" + file;
            FileInputStream inputStream = new FileInputStream(new File(path));

            int totalAvailable = inputStream.available();
            int bytesAvailable;
            int bufferSize = Math.min(totalAvailable, 1024);
            byte[] buffer = new byte[bufferSize];
            while (inputStream.read(buffer, 0, bufferSize) > 0 && !isCancelled()) {
                outputStream.write(buffer, 0, bufferSize);
                bytesAvailable = inputStream.available();
                bufferSize = Math.min(bytesAvailable, 1024);
                publishProgress(100 - (int) ((double) bytesAvailable / totalAvailable * 100));
            }

            inputStream.close();
        }

        @Override
        protected void onProgressUpdate(Integer... progress) {
            textView.setText("Uploading..." + progress[0] + "%");
        }

        @Override
        protected void onPostExecute(Void result) {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            if (success) {
                audioManager.playSoundEffect(Sounds.SUCCESS);
            } else {
                audioManager.playSoundEffect(Sounds.ERROR);
            }
            finish();
        }

        @Override
        public void onCancelled(Void result) {
            AudioManager audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
            audioManager.playSoundEffect(Sounds.DISMISSED);
            finish();
        }
    }

}
