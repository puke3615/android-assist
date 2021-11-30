package com.puke.assist.api;

import android.util.Log;

/**
 * @author puke
 * @version 2021/11/30
 */
public class AssistLog {

    private static final String TAG = AssistLog.class.getSimpleName();

    public static int i(String msg) {
        return Log.i(TAG, msg);
    }

    public static int w(String msg) {
        return Log.w(TAG, msg);
    }

    public static int e(String msg) {
        return Log.e(TAG, msg);
    }

    public static int e(String msg, Throwable tr) {
        return Log.e(TAG, msg, tr);
    }
}
