package com.puke.assist.core.model;

import java.util.List;

/**
 * @author puke
 * @version 2021/9/10
 */
public class ConfigModel {

    public String id;

    public String name;

    public List<PropertyModel> properties;

    public boolean hasProperties() {
        return properties != null && properties.size() > 0;
    }
}
