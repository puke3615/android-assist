package com.puke.assist;

import android.app.Activity;
import android.os.Bundle;
import android.view.KeyEvent;
import android.widget.TextView;

import com.puke.assist.api.Assist;

public class MainActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);
        TextView config = findViewById(R.id.main_config);

        AppConfig appConfig = AppConfig.INSTANCE;
        config.setText(new StringBuilder()
                .append("\nNumber: ").append(appConfig.intValue())
                .append("\nName: ").append(appConfig.name())
                .append("\nSwitch: ").append(appConfig.enable())
                .append("\nColor: ").append(appConfig.color())
                .append("\nEnvironment: ").append(appConfig.env())
                .append("\nOption: ").append(appConfig.option())
        );
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