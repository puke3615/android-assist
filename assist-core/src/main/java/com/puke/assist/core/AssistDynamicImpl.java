package com.puke.assist.core;

import android.app.Application;

import com.puke.assist.api.AppContext;
import com.puke.assist.api.Assist;
import com.puke.assist.api.AssistLog;

/**
 * @author puke
 * @version 2021/11/30
 */
class AssistDynamicImpl {

    static {
        AssistLog.i("AssistDynamicImpl setup.");
        Application application = (Application) AppContext.getContext().getApplicationContext();
        Assist.setConfigService(new ModifiableConfigService(application));
    }
}
