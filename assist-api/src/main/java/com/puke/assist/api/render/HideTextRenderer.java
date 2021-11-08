package com.puke.assist.api.render;

import android.text.TextUtils;
import android.widget.TextView;

/**
 * @author puke
 * @version 2021/11/8
 */
public class HideTextRenderer implements TextViewRenderer {

    @Override
    public void render(TextView view, String data) {
        if (TextUtils.isEmpty(data)) {
            view.setText(null);
            return;
        }

        StringBuilder builder = new StringBuilder();
        for (int i = 0; i < data.length(); i++) {
            builder.append("*");
        }
        view.setText(builder);
    }
}
