package com.puke.assist.core;

import java.util.Collection;

/**
 * @author puke
 * @version 2021/9/14
 */
class Util {

    static <E> boolean isNotEmpty(Collection<E> collection) {
        return !isEmpty(collection);
    }

    static <E> boolean isEmpty(Collection<E> collection) {
        return collection == null || collection.isEmpty();
    }
}
