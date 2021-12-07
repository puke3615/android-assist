package com.puke.assist.core;

import android.content.Context;
import android.text.TextUtils;
import android.util.Log;

import com.puke.assist.api.Config;
import com.puke.assist.api.EnumTips;
import com.puke.assist.api.Order;
import com.puke.assist.api.Property;
import com.puke.assist.core.model.ConfigModel;
import com.puke.assist.core.model.PropertyModel;

import java.io.IOException;
import java.lang.reflect.Method;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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
                    String spValue = SpHelper.getString(context, configData.id, property.id, property.defaultValue);
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
        // Scan config type if no config type passed
        List<Class<?>> configTypes = scanAllConfigType(context);

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
            Method[] methods = configType.getMethods();

            // Sort method
            List<Method> sortedMethods = sortMethod(Arrays.asList(methods));

            for (Method method : sortedMethods) {
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
                propertyModel.defaultValue = property.defaultValue();
                String propertyOptions = property.options();
                Class<?> returnType = method.getReturnType();
                String defaultValue = property.defaultValue();
                if (!TextUtils.isEmpty(propertyOptions)) {
                    // Use options if set
                    List<String> options = Arrays.asList(propertyOptions.split(","));
                    propertyModel.options = options;
                    final String propertyValue;
                    if (!TextUtils.isEmpty(defaultValue) && options.contains(defaultValue)) {
                        propertyValue = defaultValue;
                    } else {
                        propertyValue = options.get(0);
                    }
                    propertyModel.setValue(propertyValue);
                } else if (Enum.class.isAssignableFrom(returnType)) {
                    try {
                        // Convert enum to options
                        boolean implementEnumTips = EnumTips.class.isAssignableFrom(returnType);
                        List<String> options = new ArrayList<>();
                        List<String> enumTipsOptions = new ArrayList<>();
                        Enum[] values = (Enum[]) returnType.getMethod("values").invoke(null);
                        String propertyValue = null;
                        if (values != null) {
                            for (Enum item : values) {
                                if (TextUtils.equals(item.name(), defaultValue)) {
                                    propertyValue = defaultValue;
                                }
                                options.add(String.valueOf(item));
                                if (implementEnumTips) {
                                    enumTipsOptions.add(((EnumTips) item).getTips());
                                }
                            }
                            if (propertyValue == null) {
                                propertyValue = values[0].name();
                            }
                        }
                        propertyModel.options = options;
                        if (implementEnumTips) {
                            propertyModel.enumTipsOptions = enumTipsOptions;
                        }
                        propertyModel.setValue(propertyValue);
                    } catch (Exception e) {
                        throw new RuntimeException("Parse options from enum failed", e);
                    }
                } else {
                    propertyModel.setValue(defaultValue);
                }
                propertyModel.rebootIfChanged = property.rebootIfChanged();
                propertyModel.hideDefaultText = property.hideDefaultText();
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

    private static List<Method> sortMethod(List<Method> methods) {
        List<Method> result = new ArrayList<>(methods);
        Map<Method, Integer> method2Order = new HashMap<>();
        for (Method method : methods) {
            Order order = method.getAnnotation(Order.class);
            if (order != null) {
                method2Order.put(method, order.value());
            }
        }

        // Sort by order
        Collections.sort(result, new Comparator<Method>() {
            @Override
            public int compare(Method o1, Method o2) {
                // Get method's order
                int order1 = getOrDefault(method2Order, o1, 0);
                int order2 = getOrDefault(method2Order, o2, 0);
                return order1 - order2;
            }
        });

        return result;
    }

    private static List<Class<?>> scanAllConfigType(Context context) {
        List<Class<?>> result = new ArrayList<>();
        Map<Class<?>, Integer> type2Order = new HashMap<>();
        try {
            DexFile dexFile = new DexFile(context.getPackageCodePath());
            Enumeration<String> entries = dexFile.entries();
            while (entries.hasMoreElements()) {
                String className = entries.nextElement();
                try {
                    ClassLoader classLoader = ConfigManager.class.getClassLoader();
                    Class<?> type = Class.forName(className, false, classLoader);
                    if (type.isAnnotationPresent(Config.class)) {
                        result.add(type);
                        Order order = type.getAnnotation(Order.class);
                        if (order != null) {
                            // Remember type's order
                            type2Order.put(type, order.value());
                        }
                    }
                } catch (ClassNotFoundException ignored) {
                }
            }
        } catch (IOException e) {
            throw new RuntimeException("Scan config type failed", e);
        }

        // Sort by order
        Collections.sort(result, new Comparator<Class<?>>() {
            @Override
            public int compare(Class<?> o1, Class<?> o2) {
                // Get type's order
                int order1 = getOrDefault(type2Order, o1, 0);
                int order2 = getOrDefault(type2Order, o2, 0);
                return order1 - order2;
            }
        });

        return result;
    }

    private static <K, V> V getOrDefault(Map<K, V> map, K key, V defaultValue) {
        V value = map.get(key);
        return value == null ? defaultValue : value;
    }
}
