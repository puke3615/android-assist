package com.puke.assist.core.model;

import com.puke.assist.api.render.ConfigRenderer;

import java.io.Serializable;
import java.util.List;

/**
 * @author puke
 * @version 2021/9/10
 */
public class PropertyModel implements Serializable {

    public String id;

    public String tips;

    public String value;

    public List<String> options;

    public List<String> enumTipsOptions;

    public String currentValue;

    public boolean rebootIfChanged;

    public Class<? extends ConfigRenderer> renderer;

    public Class<?> type;

    public void setValue(String value) {
        this.value = value;
        this.currentValue = value;
    }
}
