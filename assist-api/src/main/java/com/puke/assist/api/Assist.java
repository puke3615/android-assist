package com.puke.assist.api;

import android.app.Application;
import android.content.Context;

import java.lang.reflect.InvocationHandler;
import java.lang.reflect.Method;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @author puke
 * @version 2021/8/30
 */
public class Assist {

    private static final Map<Class<?>, Object> configInstances = new HashMap<>();

    private static List<Class<?>> configTypes;
    private static Method dynamicImplMethod;
    private static Class<?> dynamicImplType;

    public static void init(Application application) {
        init(application, null);
    }

    public static void init(Application application, List<Class<?>> configTypes) {
        Assist.configTypes = configTypes;
        try {
            dynamicImplType = Class.forName("com.puke.assist.core.AssistDynamicImpl");
        } catch (ClassNotFoundException ignored) {
            // Ignore if no core library found
            return;
        }

        try {
            dynamicImplType.getMethod("init", Application.class).invoke(null, application);
            dynamicImplMethod = dynamicImplType.getMethod("invoke", Class.class, Method.class, Object[].class);
        } catch (Exception e) {
            throw new RuntimeException("Assist framework internal error.", e);
        }
    }

    @SuppressWarnings("unchecked")
    public static <T> T getConfig(Class<T> configType) {
        Object instance = configInstances.get(configType);
        if (instance != null) {
            return (T) instance;
        }

        ClassLoader classLoader = configType.getClassLoader();
        T configInstance = (T) Proxy.newProxyInstance(classLoader, new Class[]{configType},
                new InvocationHandler() {
                    @Override
                    public Object invoke(Object proxy, Method method, Object[] args) throws Throwable {
                        if (dynamicImplMethod == null) {
                            // return default value if no dynamic implementation found
                            return getDefaultValue(configType, method);
                        } else {
                            // return dynamic implementation if dynamic found
                            try {
                                return dynamicImplMethod.invoke(null, configType, method, args);
                            } catch (Exception e) {
                                String message = String.format(
                                        "Dynamic invoke error at %s#%s",
                                        configType.getName(), method.getName()
                                );
                                throw new RuntimeException(message, e);
                            }
                        }
                    }
                }
        );
        configInstances.put(configType, configInstance);
        return configInstance;
    }

    public static boolean openConfigPage(Context context) {
        if (dynamicImplType != null) {
            String methodName = "openConfigPage";
            try {
                dynamicImplType.getMethod(methodName, Context.class).invoke(null, context);
                return true;
            } catch (Exception ignored) {
            }
        }
        return false;
    }

    private static Object getDefaultValue(Class<?> configType, Method method) {
        Property property = method.getAnnotation(Property.class);
        if (property == null) {
            throw new RuntimeException(String.format(
                    "No %s annotation found at %s#%s",
                    Property.class.getSimpleName(), configType.getName(), method.getName()
            ));
        }

        String defaultValue = property.defaultValue();
        return parseObject(configType, method, defaultValue);
    }

    public static Object parseObject(Class<?> configType, Method method, String value) {
        Class<?> returnType = method.getReturnType();
        if (returnType == String.class) {
            return value;
        } else if (returnType == boolean.class || returnType == Boolean.class) {
            return Boolean.parseBoolean(value);
        } else if (returnType == int.class || returnType == Integer.class) {
            return Integer.parseInt(value);
        } else if (returnType == double.class || returnType == Double.class) {
            return Double.parseDouble(value);
        } else if (returnType == long.class || returnType == Long.class) {
            return Long.parseLong(value);
        } else if (Enum.class.isAssignableFrom(returnType)) {
            try {
                Method valueOfMethod = returnType.getMethod("valueOf", String.class);
                return valueOfMethod.invoke(null, value);
            } catch (Exception e) {
                String message = String.format(
                        "Read default value of %s#%s error", configType.getName(), method.getName()
                );
                throw new RuntimeException(message, e);
            }
        } else {
            throw new RuntimeException(String.format(
                    "Not supported return type %s at %s#%s",
                    returnType.getName(), configType.getName(), method.getName()
            ));
        }
    }

    public static List<Class<?>> getConfigTypes() {
        return configTypes;
    }
}
