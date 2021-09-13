package com.puke.assist;

import android.app.Application;
import android.content.Context;

import com.puke.assist.api.Assist;

/**
 * @author puke
 * @version 2021/5/8
 */
public class App extends Application {

    private static final String TAG = App.class.getSimpleName();

    private static App sApp;

    @Override
    public void onCreate() {
        super.onCreate();
        sApp = this;
        Assist.init(this);
    }

    public static App getApplication() {
        return sApp;
    }

    public static Context getAppContext() {
        return sApp.getApplicationContext();
    }
}
