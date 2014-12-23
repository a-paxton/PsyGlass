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

public enum State {

    WAITING,
    STARTING,
    RUNNING,
    FINISHING,
    DISCONNECTED;

    public static State getState(String string) {
        if (string.equals("disconnected")) {
            return DISCONNECTED;
        } else if (string.equals("starting")) {
            return STARTING;
        } else if (string.equals("running")) {
            return RUNNING;
        } else if (string.equals("finishing")) {
            return FINISHING;
        }else {
            return null;
        }
    }

    public static State getState(int i) {
        switch(i) {
            case 0:
                return DISCONNECTED;
            case 1:
                return STARTING;
            case 2:
                return RUNNING;
            case 3:
                return FINISHING;
            default:
                return null;
        }
    }
}
