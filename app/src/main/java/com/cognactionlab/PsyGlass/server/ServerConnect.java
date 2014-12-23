package com.cognactionlab.PsyGlass.server;

import android.os.Handler;
import android.os.Message;

import org.json.JSONObject;

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

public class ServerConnect {

    private static final int CONNECT = 0;
    private static final int SESSION = 1;

    private static final int POLL_DELAY = 250; // milliseconds

    String address;
    ServerEvent listener;

    private State state = State.WAITING;
    private int id = -1;
    private String color = "000000";
    private String text = "";

    private long timestamp; // seconds

    Thread getThread;
    Handler handler = new Handler() {
        public void handleMessage(Message msg) {
            if (state == State.DISCONNECTED) return;
            String response = (String) msg.obj;
            switch(msg.what) {
                case CONNECT:
                    onConnect(response);
                    break;
                case SESSION:
                    onSession(response);
                    break;
                default:
                    // Should never happen!
            }
        }
    };

    Handler timeHandler = new Handler();
    Runnable timeRunnable = new Runnable() {
        public void run() {
            if (!getThread.isAlive()) {
                String fullAddress = address + "/resources/device_server.php?id=" + id;
                getThread = new Thread(new GetServerRunnable(handler, SESSION, fullAddress));
                getThread.start();
            }
            if (state != State.DISCONNECTED) {
                timeHandler.postDelayed(this, POLL_DELAY);
            }
        }
    };

    public ServerConnect(String address, ServerEvent listener) {
        this.address = address;
        this.listener = listener;
    }

    private void fail() {
        state = State.DISCONNECTED;
        timeHandler.removeCallbacks(timeRunnable);
        listener.onFailure();
    }

    public void connect() {
        String fullAddress = address + "/resources/device_server.php?connect=true";
        getThread = new Thread(new GetServerRunnable(handler, CONNECT, fullAddress));
        getThread.start();
    }

    private void poll() {
        timeHandler.postDelayed(timeRunnable, 0);
    }

    private void disconnect() {
        state = State.DISCONNECTED;
        timeHandler.removeCallbacks(timeRunnable);
        listener.onDisconnect();
    }

    public void cancel() {
        state = State.DISCONNECTED;
        timeHandler.removeCallbacks(timeRunnable);
    }

    private void onConnect(String response) {
        if (response.equals("")) {
            fail();
        } else {
            try {
                JSONObject obj = new JSONObject(response);
                id = obj.getInt("id");
                state = State.STARTING;
                listener.onConnect(id);
                poll();
            } catch (Exception e) {
                // Something went wrong!
                e.printStackTrace();
                fail();
            }
        }
    }

    private void onSession(String response) {
        if (response.equals("")) {
            fail();
        } else {
            try {
                JSONObject obj = new JSONObject(response);
                State newState = State.getState(obj.getString("state"));
                if (newState != state) {
                    state = newState;
                    if (state == State.RUNNING) {
                        timestamp = Long.parseLong(obj.getString("timestamp"));
                        listener.onRun(timestamp);
                    } else if (state == State.FINISHING) {
                        listener.onFinish((System.currentTimeMillis() / 1000) - timestamp);
                    } else if (state == State.DISCONNECTED) {
                        disconnect();
                    }
                } else if (state == State.RUNNING) {
                    String color = obj.getString("color");
                    if (!color.equals(this.color)) {
                        this.color = color;
                        listener.onColorChange(color);
                    }
                    String text = obj.getString("text");
                    if (!text.equals(this.text)) {
                        this.text = text;
                        listener.onTextChange(text);
                    }
                }
            } catch (Exception e) {
                // Something went wrong!
                e.printStackTrace();
                fail();
            }
        }
    }
}
