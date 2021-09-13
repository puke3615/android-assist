package com.puke.assist.core;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.puke.assist.api.Assist;
import com.puke.assist.api.Config;
import com.puke.assist.api.EnumTips;
import com.puke.assist.api.Property;
import com.puke.assist.core.model.ConfigModel;
import com.puke.assist.core.model.PropertyModel;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;

import dalvik.system.DexFile;

/**
 * Config manager, read config type and parse config information
 *
 * @author puke
 * @version 2021/9/9
 */
class ConfigManager {

    private static final String TAG = ConfigManager.class.getSimpleName();

    private static List<ConfigModel> configData;

    /**
     * @return Config information
     */
    static List<ConfigModel> fetchConfigData(Context context) {
        if (configData == null) {
            // Parse config information from class annotation
            configData = parseConfigDataInternal(context);
        }

        // Fill value from SharedPreferences
        for (ConfigModel configData : configData) {
            if (configData.hasProperties()) {
                for (PropertyModel property : configData.properties) {
                    String spValue = SpHelper.getString(context, configData.id, property.id);
                    if (spValue != null) {
                        // Fill shared preferences value only when it's not null
                        property.setValue(spValue);
                    }
                }
            }
        }

        return configData;
    }

    @SuppressWarnings("rawtypes")
    private static List<ConfigModel> parseConfigDataInternal(Context context) {
        List<Class<?>> configTypes = Assist.getConfigTypes();

        if (configTypes == null) {
            // Scan config type if no config type passed
            configTypes = scanAllConfigType(context);
        }

        List<ConfigModel> configModels = new ArrayList<>();

        // Parse config information by annotation
        for (Class<?> configType : configTypes) {
            Config config = configType.getAnnotation(Config.class);
            String configId = configType.getName();
            if (config == null) {
                Log.w(TAG, String.format(
                        "No %s annotation found at %s",
                        Config.class.getName(), configId
                ));
                continue;
            }

            List<PropertyModel> propertyModels = new ArrayList<>();
            for (Method method : configType.getMethods()) {
                Property property = method.getAnnotation(Property.class);
                if (property == null) {
                    Log.w(TAG, String.format(
                            "No %s annotation found at %s#%s",
                            Property.class.getName(), configId, method.getName()
                    ));
                    continue;
                }

                PropertyModel propertyModel = new PropertyModel();
                propertyModel.id = method.getName();
                propertyModel.tips = property.tips();
                String propertyOptions = property.options();
                Class<?> returnType = method.getReturnType();
                String defaultValue = property.defaultValue();
                if (!TextUtils.isEmpty(propertyOptions)) {
                    // Use options if set
                    propertyModel.options = propertyOptions;
                    List<String> options = Arrays.asList(propertyOptions.split(","));
                    int defaultIndex = options.indexOf(defaultValue);
                    propertyModel.setValue(String.valueOf(Math.max(defaultIndex, 0)));
                } else if (Enum.class.isAssignableFrom(returnType)) {
                    try {
                        // Convert enum to options
                        boolean implementEnumTips = EnumTips.class.isAssignableFrom(returnType);
                        List<String> options = new ArrayList<>();
                        Enum[] values = (Enum[]) returnType.getMethod("values").invoke(null);
                        int defaultIndex = 0;
                        if (values != null) {
                            for (int i = 0; i < values.length; i++) {
                                Enum item = values[i];
                                if (TextUtils.equals(item.name(), defaultValue)) {
                                    defaultIndex = i;
                                }
                                if (implementEnumTips) {
                                    options.add(((EnumTips) item).getTips());
                                } else {
                                    options.add(String.valueOf(item));
                                }
                            }
                        }
                        propertyModel.options = TextUtils.join(",", options);
                        propertyModel.setValue(String.valueOf(defaultIndex));
                    } catch (Exception e) {
                        throw new RuntimeException("Parse options from enum failed", e);
                    }
                } else {
                    propertyModel.setValue(defaultValue);
                }
                propertyModel.rebootIfChanged = property.rebootIfChanged();
                propertyModel.type = returnType;

                propertyModels.add(propertyModel);
            }

            ConfigModel configModel = new ConfigModel();
            configModel.id = configId;
            configModel.name = config.value();
            configModel.properties = propertyModels;
            configModels.add(configModel);
        }
        return configModels;
    }

    private static List<Class<?>> scanAllConfigType(Context context) {
        List<Class<?>> result = new ArrayList<>();
        try {
            DexFile dexFile = new DexFile(context.getPackageCodePath());
            Enumeration<String> entries = dexFile.entries();
            while (entries.hasMoreElements()) {
                String className = entries.nextElement();
                try {
                    Class<?> type = Class.forName(className);
                    if (type.isAnnotationPresent(Config.class)) {
                        result.add(type);
                    }
                } catch (ClassNotFoundException ignored) {
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Scan config type failed", e);
        }
        return result;
    }
}
