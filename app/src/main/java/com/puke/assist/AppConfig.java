package com.puke.assist;

import com.puke.assist.api.Assist;
import com.puke.assist.api.Config;
import com.puke.assist.api.Property;

/**
 * @author puke
 * @version 2021/9/9
 */
@Config("App Configuration")
public interface AppConfig {

    AppConfig INSTANCE = Assist.getConfig(AppConfig.class);

    @Property(tips = "Number", defaultValue = "300")
    int intValue();

    @Property(tips = "Name", defaultValue = "Default name")
    String name();

    @Property(tips = "Switch", defaultValue = "true")
    boolean enable();

    @Property(tips = "Color", defaultValue = "RED")
    Color color();

    @Property(tips = "Environment", defaultValue = "ONLINE")
    Env env();

    @Property(tips = "String option", defaultValue = "abc", options = "Jack,Tom,Lucy")
    String option();
}
