package com.puke.assist;

import com.puke.assist.api.Config;
import com.puke.assist.api.Property;

/**
 * @author puke
 * @version 2021/9/9
 */
@Config("App配置信息")
public interface AppConfig {

    @Property(tips = "运行环境", defaultValue = "ONLINE")
    Env env();

    @Property(tips = "颜色", defaultValue = "RED")
    Color color();

    @Property(defaultValue = "[appId]")
    String appId();

    @Property(defaultValue = "[appKey]")
    String appKey();

    @Property(defaultValue = "[appServer]")
    String appServer();

    @Property(defaultValue = "abc", options = "fsef,abc,yui,fsf,sfas")
    String serverSecret();

    @Property(tips = "展示样板间拓展能力", defaultValue = "true")
    boolean showCustomView();

    @Property(tips = "禁言时长 (单位:秒)", defaultValue = "300")
    int muteSeconds();
}
