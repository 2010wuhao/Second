/**
 * =====================================================================
 *
 * @file  DVBSurfaceView.java
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
import android.view.SurfaceView;

import com.joysee.adtv.logic.JDVBPlayer;
import com.joysee.common.utils.JLog;

public class DVBSurfaceView extends SurfaceView {

    private static final String TAG = JLog.makeTag(DVBSurfaceView.class);

    private int mAdjustWidth;
    private int mAdjustHeight;

    public DVBSurfaceView(Context context) {
        super(context);
    }

    public DVBSurfaceView(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public DVBSurfaceView(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    public void adjustSize(int width, int height) {
        this.mAdjustWidth = width;
        this.mAdjustHeight = height;
        getHolder().setFixedSize(mAdjustWidth, mAdjustHeight);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        if (mAdjustWidth == 0) {
            super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        } else {
            setMeasuredDimension(mAdjustWidth, mAdjustHeight);
        }
    }

    @Override
    protected void onSizeChanged(int w, int h, int oldw, int oldh) {
        super.onSizeChanged(w, h, oldw, oldh);
        int[] location = new int[2];
        getLocationOnScreen(location);
        JLog.d(TAG, "onSizeChanged x = " + location[0] + " y = " + location[1] + " w = " + w + " h = " + h);
        JDVBPlayer.getInstance().setVideoWindow(JDVBPlayer.TUNER_0, location[0], location[1], location[0] + w,
                location[1] + h);
    }
}
