package com.puke.assist.core;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;

import com.puke.assist.api.Assist;
import com.puke.assist.api.Config;
import com.puke.assist.api.Property;

import java.lang.ref.WeakReference;
import java.lang.reflect.Method;

/**
 * Assist dynamic implementation class
 *
 * @author puke
 * @version 2021/9/9
 */
public class AssistDynamicImpl {

    private static WeakReference<Activity> currentActivityRef;
    private static Application application;

    public static void init(Application application) {
        AssistDynamicImpl.application = application;
        application.registerActivityLifecycleCallbacks(new SimpleActivityLifecycleCallbacks() {
            @Override
            public void onActivityResumed(Activity activity) {
                super.onActivityResumed(activity);
                currentActivityRef = new WeakReference<>(activity);
            }

            @Override
            public void onActivityPaused(Activity activity) {
                super.onActivityPaused(activity);
                Activity currentActivity = getCurrentActivity();
                if (currentActivity == activity) {
                    currentActivityRef = null;
                }
            }
        });
        application.registerActivityLifecycleCallbacks(new ShakeHelper(application, new ShakeHelper.OnShakeListener() {
            @Override
            public void onShake() {
                if ((!(getCurrentActivity() instanceof AssistConfigActivity))) {
                    Intent intent = new Intent(application, AssistConfigActivity.class);
                    intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    application.startActivity(intent);
                }
            }
        }));
    }

    private static Activity getCurrentActivity() {
        return currentActivityRef == null ? null : currentActivityRef.get();
    }

    public static Object invoke(Class<?> configType, Method method, Object[] args) {
        if (application == null) {
            throw new RuntimeException("Must call init method before call invoke method");
        }

        Config config = configType.getAnnotation(Config.class);
        if (config == null) {
            throw new RuntimeException(String.format(
                    "No annotation of %s found at %s",
                    Config.class.getName(), configType.getName()
            ));
        }

        Property property = method.getAnnotation(Property.class);
        if (property == null) {
            throw new RuntimeException(String.format(
                    "No annotation of %s found at %s#%s",
                    Property.class.getName(), configType.getName(), method.getName()
            ));
        }

        String value = SpHelper.getString(application, configType.getName(), method.getName());
        if (value == null) {
            value = property.defaultValue();
        }

        return Assist.parseObject(configType, method, value);
    }
}
