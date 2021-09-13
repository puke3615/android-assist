package com.puke.assist.core;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * @author puke
 * @version 2021/9/9
 */
class SpHelper {

    public static void putString(Context context, String spName, String key, String value) {
        getSp(context, spName).edit()
                .putString(key, value)
                .apply();
    }

    public static String getString(Context context, String spName, String key) {
        return getString(context, spName, key, null);
    }

    public static String getString(Context context, String spName, String key, String defaultValue) {
        return getSp(context, spName).getString(key, defaultValue);
    }

    private static SharedPreferences getSp(Context context, String spName) {
        return context.getSharedPreferences(spName, Context.MODE_PRIVATE);
    }
}
