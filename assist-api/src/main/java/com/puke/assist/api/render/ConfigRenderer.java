package com.puke.assist.api.render;

import android.view.View;

/**
 * @author puke
 * @version 2021/11/3
 */
public interface ConfigRenderer<V extends View, T> {

    void render(V view, T data);

    abstract class None<V extends View, T> implements ConfigRenderer<V, T> {
    }
}
