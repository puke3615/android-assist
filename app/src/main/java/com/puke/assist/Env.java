package com.puke.assist;

import com.puke.assist.api.EnumTips;

/**
 * @author puke
 * @version 2021/9/9
 */
public enum Env implements EnumTips {

    PRE("pre environment"),

    ONLINE("online environment"),
    ;

    private final String tips;

    Env(String tips) {
        this.tips = tips;
    }

    @Override
    public String getTips() {
        return tips;
    }
}
