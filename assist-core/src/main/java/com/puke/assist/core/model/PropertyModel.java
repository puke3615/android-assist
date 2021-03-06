package com.puke.assist.core.model;

import java.io.Serializable;
import java.util.List;

/**
 * @author puke
 * @version 2021/9/10
 */
public class PropertyModel implements Serializable {

    public String id;

    public String tips;

    public String defaultValue;

    public String value;

    public List<String> options;

    public List<String> enumTipsOptions;

    public String currentValue;

    public boolean rebootIfChanged;

    public boolean hideDefaultText;

    public Class<?> type;

    public void setValue(String value) {
        this.value = value;
        this.currentValue = value;
    }
}
