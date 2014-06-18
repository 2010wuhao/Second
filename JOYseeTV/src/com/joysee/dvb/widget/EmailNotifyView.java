/**
 * =====================================================================
 *
 * @file  EmailNotifyView.java
 * @Module Name   com.joysee.dvb.widget
 * @author yl
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年3月3日
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
 * yl         2014年3月3日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.joysee.common.utils.JLog;

public class EmailNotifyView extends View {

    private static final String TAG = JLog.makeTag(EmailNotifyView.class);

    public EmailNotifyView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

}
