package com.puke.assist.core;

import android.app.Activity;
import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;

/**
 * Listen shake event of device
 */
class ShakeHelper extends SimpleActivityLifecycleCallbacks implements SensorEventListener {

    private static final int SENSOR_VALUE = 32;

    private final SensorManager sensorManager;
    private final OnShakeListener shakeListener;

    private final int sensorThresholdValue;

    public interface OnShakeListener {
        void onShake();
    }

    public ShakeHelper(Context context, OnShakeListener shakeListener) {
        this(context, SENSOR_VALUE, shakeListener);
    }

    public ShakeHelper(Context context, int sensorThresholdValue, OnShakeListener shakeListener) {
        this.sensorThresholdValue = sensorThresholdValue;
        this.sensorManager = (SensorManager) context.getSystemService(Context.SENSOR_SERVICE);
        this.shakeListener = shakeListener;
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {

    }

    @Override
    public void onSensorChanged(SensorEvent event) {
        int sensorType = event.sensor.getType();
        // values[0]:X，values[1]：Y，values[2]：Z
        float[] values = event.values;
        if (sensorType == Sensor.TYPE_ACCELEROMETER) {
            if ((Math.abs(values[0]) > sensorThresholdValue
                    || Math.abs(values[1]) > sensorThresholdValue
                    || Math.abs(values[2]) > sensorThresholdValue)) {
                if (shakeListener != null) {
                    shakeListener.onShake();
                }
            }
        }
    }

    @Override
    public void onActivityResumed(Activity activity) {
        sensorManager.registerListener(this,
                sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER),
                SensorManager.SENSOR_DELAY_NORMAL);
    }

    @Override
    public void onActivityPaused(Activity activity) {
        sensorManager.unregisterListener(this);
    }
}