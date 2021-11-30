package com.puke.assist.core;

import android.app.Application;
import android.content.Context;
import android.content.Intent;

import com.puke.assist.api.AppContext;
import com.puke.assist.api.Config;
import com.puke.assist.api.ConfigService;
import com.puke.assist.api.Property;
import com.puke.assist.api.TypeConverter;

import java.lang.reflect.Method;

/**
 * @author puke
 * @version 2021/11/30
 */
class ModifiableConfigService implements ConfigService {

    private final ActivityLifecycleHelper lifecycleHelper;

    private boolean openConfigWhenShake = true;

    public ModifiableConfigService(Application application) {
        lifecycleHelper = new ActivityLifecycleHelper(application);
        application.registerActivityLifecycleCallbacks(new ShakeHelper(application, new ShakeHelper.OnShakeListener() {
            @Override
            public void onShake() {
                if (openConfigWhenShake) {
                    openConfigPage(application);
                }
            }
        }));
    }

    @Override
    public Object readConfigValue(Class<?> configType, Method method, Object[] args) throws Throwable {
        Config config = configType.getAnnotation(Config.class);
        String configName = configType.getName();
        String methodName = method.getName();

        if (config == null) {
            throw new RuntimeException(String.format(
                    "No annotation of %s found at %s", Config.class.getName(), configName));
        }

        Property property = method.getAnnotation(Property.class);
        if (property == null) {
            throw new RuntimeException(String.format(
                    "No annotation of %s found at %s#%s",
                    Property.class.getName(), configName, methodName
            ));
        }

        Context context = AppContext.getContext();
        String value = SpHelper.getString(context, configName, methodName);
        if (value == null) {
            value = property.defaultValue();
        }

        return TypeConverter.convert(value, method.getReturnType());
    }

    @Override
    public void openConfigPage(Context context) {
        if ((!(lifecycleHelper.getCurrentActivity() instanceof AssistConfigActivity))) {
            Intent intent = new Intent(context, AssistConfigActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            context.startActivity(intent);
        }
    }

    @Override
    public void allowOpenConfigWhenShake(boolean enable) {
        openConfigWhenShake = enable;
    }
}
