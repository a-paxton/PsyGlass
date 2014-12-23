package com.cognactionlab.PsyGlass.extra;

import android.os.Environment;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

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

public class SensorQueueRunnable implements Runnable {

    private static final int QUEUE_DONE = -1;

    private BlockingQueue<SensorParcel> queue;

    @Override
    public void run() {
        try {
            queue = new LinkedBlockingQueue<SensorParcel>();
            String path = Environment.getExternalStorageDirectory().getAbsolutePath();
            BufferedWriter accelWriter = new BufferedWriter(new FileWriter(path + "/PsyGlass.txt"));

            SensorParcel parcel;
            while ((parcel = queue.take()).type != QUEUE_DONE) {
                accelWriter.write(parcel.message);
                accelWriter.newLine();
            }
            accelWriter.close();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public void addParcel(int type, String message) {
        queue.add(new SensorParcel(type, message));
    }

    public void finish() {
        queue.add(new SensorParcel(QUEUE_DONE, ""));
    }

    private class SensorParcel {
        int type;
        String message;
        public SensorParcel(int type, String message) {
            this.type = type;
            this.message = message;
        }
    }
}
