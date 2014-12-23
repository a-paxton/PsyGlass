package com.cognactionlab.PsyGlass.server;

import android.os.Handler;
import android.os.Message;

import java.io.DataOutputStream;
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

public class PostServerRunnable implements Runnable {

    private static final int TIMEOUT = 2000; // milliseconds

    private Handler handler;
    private int handlerId;
    private String address;
    private String data;

    public PostServerRunnable(Handler handler, int handlerId, String address, String data) {
        this.handler = handler;
        this.handlerId = handlerId;
        this.address = address;
        this.data = data;
    }

    public void run() {
        boolean success = upload();
        Message msg = new Message();
        msg.obj = success;
        msg.what = handlerId;
        handler.sendMessage(msg);
    }

    private boolean upload() {
        boolean success = true;
        try {
            URL url = new URL(address);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(TIMEOUT);
            conn.setDoOutput(true);
            conn.setRequestMethod("POST");
            DataOutputStream os = new DataOutputStream(conn.getOutputStream());
            os.writeBytes(data);
            os.flush();
            os.close();
        } catch (Exception e) {
            // Something went wrong!
            //e.printStackTrace();
            success = false;
        }
        return success;
    }
}
