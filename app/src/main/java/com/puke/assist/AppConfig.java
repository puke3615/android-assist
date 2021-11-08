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

    @Property(tips = "名称", defaultValue="默认名称")
    String name();

    @Property(tips = "隐藏文本", defaultValue="默认内容", hideDefaultText = true)
    String hideText();

    @Property(tips = "字符串选择", defaultValue = "abc", options = "fsef,abc,yui,fsf,sfas")
    String stringOptionValue();

    @Property(tips = "开关控制", defaultValue = "true")
    boolean enable();

    @Property(tips = "整型", defaultValue = "300")
    int intValue();
}
