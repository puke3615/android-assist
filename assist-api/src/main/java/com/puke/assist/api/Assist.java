package com.puke.assist.api;

import android.content.Context;
import android.widget.Toast;

/**
 * @author puke
 * @version 2021/8/30
 */
public class Assist {

    public static int plus(int a, int b) {
        return a + b;
    }

    public static void showToast(Context context, String message) {
        Toast.makeText(context, message, Toast.LENGTH_SHORT).show();
    }
}
