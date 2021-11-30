package com.puke.assist.core;

import android.app.Activity;
import android.app.Application;

import java.lang.ref.WeakReference;

/**
 * A helper class for activity's lifecycle
 *
 * @author puke
 * @version 2021/11/30
 */
class ActivityLifecycleHelper extends SimpleActivityLifecycleCallbacks {

    private WeakReference<Activity> currentActivityRef;

    ActivityLifecycleHelper(Application application) {
        application.registerActivityLifecycleCallbacks(this);
    }


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

    Activity getCurrentActivity() {
        return currentActivityRef == null ? null : currentActivityRef.get();
    }
}
