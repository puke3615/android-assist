package com.puke.assist;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.view.View;

import com.puke.assist.api.Assist;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
    }

    public void onTest(View view) {
        int a = 1;
        int b = 2;
        int result = Assist.plus(a, b);
        Assist.showToast(this, String.format("Compute: %s + %s = %s", a, b, result));
    }
}