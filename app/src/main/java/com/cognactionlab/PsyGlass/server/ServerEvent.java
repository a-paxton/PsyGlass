package com.cognactionlab.PsyGlass.server;

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

public interface ServerEvent {
    public void onFailure();
    public void onConnect(int deviceId);
    public void onRun(long timestamp);
    public void onColorChange(String color);
    public void onTextChange(String text);
    public void onFinish(long duration);
    public void onDisconnect();
}
