package com.puke.assist.api;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * @author puke
 * @version 2021/11/3
 */
public final class ObjectHolder {

    private static final Map<Class<?>, Object> INSTANCES = new ConcurrentHashMap<>();

    @SuppressWarnings("unchecked")
    public static <T> T getOrCreateInstance(Class<T> type) {
        Object obj = INSTANCES.get(type);
        if (obj != null) {
            return ((T) obj);
        }

        try {
            T instance = type.newInstance();
            INSTANCES.put(type, instance);
            return instance;
        } catch (IllegalAccessException | InstantiationException e) {
            throw new RuntimeException("Create instance failure", e);
        }
    }

}
