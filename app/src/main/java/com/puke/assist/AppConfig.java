package com.puke.assist;

import com.puke.assist.api.Assist;
import com.puke.assist.api.Config;
import com.puke.assist.api.Order;
import com.puke.assist.api.Property;

/**
 * @author puke
 * @version 2021/9/9
 */
@Config("App Configuration")
public interface AppConfig {

    AppConfig INSTANCE = Assist.getConfig(AppConfig.class);

    @Order(1)
    @Property(tips = "Number", defaultValue = "300")
    int intValue();

    @Order(2)
    @Property(tips = "Name", defaultValue = "Default name")
    String name();

    @Order(3)
    @Property(tips = "Switch", defaultValue = "true")
    boolean enable();

    @Order(4)
    @Property(tips = "Color", defaultValue = "RED")
    Color color();

    @Order(5)
    @Property(tips = "Environment", defaultValue = "ONLINE")
    Env env();

    @Order(6)
    @Property(tips = "String option", defaultValue = "abc", options = "Jack,Tom,Lucy")
    String option();

    @Order(7)
    @Property(tips = "hideText", defaultValue = "dadad", hideDefaultText = true)
    String hideText();
}
