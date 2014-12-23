package com.cognactionlab.PsyGlass.server;

import android.os.Handler;
import android.os.Message;

import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Scanner;

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

public class GetServerRunnable implements Runnable {

    private static final int TIMEOUT = 2000; // milliseconds

    private Handler handler;
    private int handlerId;
    private String address;

    public GetServerRunnable(Handler handler, int handlerId, String address) {
        this.handler = handler;
        this.handlerId = handlerId;
        this.address = address;
    }

    public void run() {
        String result = download();
        Message msg = new Message();
        msg.obj = result;
        msg.what = handlerId;
        handler.sendMessage(msg);
    }

    private String download() {
        String result = "";
        try {
            InputStream is = null;
            URL url = new URL(address);
            HttpURLConnection conn = (HttpURLConnection) url.openConnection();
            conn.setConnectTimeout(TIMEOUT);
            conn.setDoInput(true);
            conn.setRequestMethod("GET");
            is = conn.getInputStream();
            result = readInputStream(is);
            is.close();
        } catch (Exception e) {
            // Something went wrong!
            //e.printStackTrace();
        }
        return result;
    }

    private String readInputStream(InputStream stream) {
        Scanner scanner = new Scanner(stream).useDelimiter("\\A");
        return scanner.hasNext() ? scanner.next() : "";
    }
}
