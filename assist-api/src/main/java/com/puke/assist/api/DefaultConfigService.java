package com.puke.assist.api;

import android.content.Context;

import java.lang.reflect.Method;

/**
 * @author puke
 * @version 2021/11/30
 */
public class DefaultConfigService implements ConfigService {

    @Override
    public Object readConfigValue(Class<?> configType, Method method, Object[] args) throws Throwable {
        Property property = method.getAnnotation(Property.class);
        if (property == null) {
            throw new RuntimeException(String.format(
                    "No %s annotation found at %s#%s",
                    Property.class.getSimpleName(), configType.getName(), method.getName()
            ));
        }

        String defaultValue = property.defaultValue();
        return TypeConverter.convert(defaultValue, method.getReturnType());
    }

    @Override
    public void openConfigPage(Context context) {
        AssistLog.e("You can't open config page by default config service.");
    }

    @Override
    public void allowOpenConfigWhenShake(boolean enable) {
        AssistLog.e("You can't call allowOpenConfigWhenShake by default config service.");
    }
}
