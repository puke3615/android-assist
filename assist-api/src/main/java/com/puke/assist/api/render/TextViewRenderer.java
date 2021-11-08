package com.puke.assist.api.render;

import android.widget.TextView;

/**
 * @author puke
 * @version 2021/11/3
 */
public interface TextViewRenderer extends ConfigRenderer<TextView, String> {

    class DefaultTextViewRenderer implements TextViewRenderer {

        @Override
        public void render(TextView view, String data) {
            view.setText(data);
        }
    }
}
