/**
 * =====================================================================
 *
 * @file   SettingsTitleView.java
 * @Module Name   com.joysee.tvbox.settings.base
 * @author wumingjun
 * @OS version  1.0
 * @Product type: JoySee
 * @date  Apr 21, 2014
 * @brief  This file is the http **** implementation.
 * @This file is responsible by ANDROID TEAM.
 * @Comments:
 * =====================================================================
 * Revision History:
 *
 *                   Modification  Tracking
 *
 * Author            Date            OS version        Reason
 * ----------      ------------     -------------     -----------
 * wumingjun         @Apr 21, 2014            1.0      Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.tvbox.settings.base;

import com.joysee.common.widget.JTextViewWithTTF;
import com.joysee.tvbox.settings.R;

import android.content.Context;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class SettingsTitleView extends LinearLayout {

    public SettingsTitleView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        initView(attrs);
    }

    public SettingsTitleView(Context context, AttributeSet attrs) {
        super(context, attrs);
        initView(attrs);
    }

    public SettingsTitleView(Context context) {
        super(context);
        initView(null);
    }

    private void initView(AttributeSet attrs) {
        if (attrs != null) {
            LayoutInflater inflater = (LayoutInflater) getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            TypedArray type = getContext().obtainStyledAttributes(attrs, R.styleable.settings);
            int textRes = type.getResourceId(R.styleable.settings_text, 0);
            int textDescRes = type.getResourceId(R.styleable.settings_description, 0);
            boolean isNarrow = type.getBoolean(R.styleable.settings_narrow, false);
            View view;
            if (isNarrow) {
                view = inflater.inflate(R.layout.view_settings_title_narrow, null);
            } else {
                view = inflater.inflate(R.layout.view_settings_title, null);
            }
            this.addView(view);
            JTextViewWithTTF text = (JTextViewWithTTF) findViewById(R.id.txt_settings_title);
            JTextViewWithTTF textDesc = (JTextViewWithTTF) findViewById(R.id.txt_settings_title_description);

            if (textRes != 0) {
                text.setText(textRes);
            } else {
                String str = type.getString(R.styleable.settings_text);
                if (str != null && !str.equals("")) {
                    text.setText(str);
                }
            }

            if (textDescRes != 0) {
                textDesc.setText(textDescRes);
            } else {
                String str = type.getString(R.styleable.settings_description);
                if (str != null && !str.equals("")) {
                    textDesc.setText(str);
                }
            }

            type.recycle();
        }
    }
}
