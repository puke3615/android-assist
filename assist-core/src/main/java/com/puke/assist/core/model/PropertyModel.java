package com.puke.assist.core.model;

import java.io.Serializable;

/**
 * @author puke
 * @version 2021/9/10
 */
public class PropertyModel implements Serializable {

    public String id;

    public String tips;

    public String value;

    public String options;

    public String currentValue;

    public boolean rebootIfChanged;

    public Class<?> type;

    public void setValue(String value) {
        this.value = value;
        this.currentValue = value;
    }
}
