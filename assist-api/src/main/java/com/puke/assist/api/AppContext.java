package com.puke.assist.api;

import android.annotation.SuppressLint;
import android.content.Context;

/**
 * @author puke
 * @version 2021/11/30
 */
public class AppContext {

    @SuppressLint("StaticFieldLeak")
    private static Context context;

    public static void setContext(Context context) {
        if (AppContext.context != null) {
            throw new RuntimeException("Context already set.");
        }
        AppContext.context = context;
    }

    public static Context getContext() {
        return context;
    }
}
