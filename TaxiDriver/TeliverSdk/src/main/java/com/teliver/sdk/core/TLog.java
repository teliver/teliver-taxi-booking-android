package com.teliver.sdk.core;

import android.util.Log;

public class TLog {

    private static boolean isVisible = false;

    private static final String TAG = "TELIVER::";

    public static void setVisible(boolean isVisible) {
        TLog.isVisible = isVisible;
    }

    public static void log(String msg) {
        if (isVisible)
            Log.v(TAG, msg);
    }
}
