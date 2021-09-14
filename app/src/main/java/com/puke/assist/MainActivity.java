package com.puke.assist;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.widget.Toast;

import com.puke.assist.api.Assist;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onTest(View view) {
        AppConfig appConfig = Assist.getConfig(AppConfig.class);
        Toast.makeText(this, "AppId = " + appConfig.name(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if (keyCode == KeyEvent.KEYCODE_VOLUME_DOWN) {
            Assist.openConfigPage(this);
            return true;
        }
        return super.onKeyDown(keyCode, event);
    }
}