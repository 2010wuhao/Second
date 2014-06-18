/**
 * =====================================================================
 *
 * @file  DVBSurfaceViewParent.java
 * @Module Name   com.joysee.dvb.widget
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月13日
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
 * yueliang         2014年2月13日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.Gravity;
import android.widget.LinearLayout;

public class DVBSurfaceViewParent extends LinearLayout {

    private DVBSurfaceView mSurfaceView;

    private int mWidth = 0;
    private int mHeight = 0;

    public DVBSurfaceViewParent(Context context) {
        this(context, null);
    }

    public DVBSurfaceViewParent(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public DVBSurfaceViewParent(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        this.setGravity(Gravity.CENTER);
    }

    public void adjustSize(int width, int height) {
        this.mWidth = width;
        this.mHeight = height;
        if (mSurfaceView != null) {
            mSurfaceView.adjustSize(mWidth, mHeight);
        }
    }

    public void setSurfaceView(DVBSurfaceView surface) {
        mSurfaceView = surface;
    }

}
