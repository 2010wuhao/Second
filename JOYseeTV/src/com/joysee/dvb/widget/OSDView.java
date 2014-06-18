/**
 * =====================================================================
 *
 * @file  OSDView.java
 * @Module Name   com.joysee.dvb.widget
 * @author yl
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年3月20日
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
 * yl         2014年3月20日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.View;

import com.joysee.adtv.logic.bean.OsdInfo;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.R;
import com.joysee.dvb.controller.DvbMessage;
import com.joysee.dvb.controller.IDvbBaseView;

public class OSDView extends View implements IDvbBaseView {
    private static final String TAG = JLog.makeTag(OSDView.class);

    private float mTopTextLength;
    private float mTopTextStartX;
    private float mTopTextStartY;
    private float mTopTextCurX;
    private String mTopText;
    private boolean isTopRunning = false;

    private float mBottomTextLength;
    private float mBottomTextStartX;
    private float mBottomTextStartY;
    private float mBottomTextCurX;
    private String mBottomText;
    private boolean isBottomRunning = false;

    private Paint mPaint;

    private float mStep = 2F;

    public OSDView(Context context, AttributeSet attrs) {
        super(context, attrs);
        mPaint = new Paint();
        mPaint.setColor(Color.WHITE);
        mPaint.setTextSize(getResources().getDimensionPixelSize(R.dimen.playback_osd_textsize));
        mTopTextStartX = getResources().getDimensionPixelSize(R.dimen.playback_osd_top_start_X);
        mTopTextStartY = getResources().getDimensionPixelSize(R.dimen.playback_osd_top_start_Y);
        mBottomTextStartX = getResources().getDimensionPixelSize(R.dimen.playback_osd_bottom_start_X);
        mBottomTextStartY = getResources().getDimensionPixelSize(R.dimen.playback_osd_bottom_start_Y);
    }

    public void setText(String text, int location) {
        if (location == OsdInfo.OSD_POSITION_TOP) {
            mTopText = text;
            mTopTextLength = mPaint.measureText(mTopText);
            mTopTextCurX = mTopTextStartX;
        } else {
            mBottomText = text;
            mBottomTextLength = mPaint.measureText(mBottomText);
            mBottomTextCurX = mBottomTextStartX;
        }
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        boolean needValid = false;
        if (isTopRunning) {
            canvas.drawText(mTopText, mTopTextCurX, mTopTextStartY, mPaint);
            mTopTextCurX -= mStep;
            if (mTopTextCurX < -mTopTextLength) {
                mTopTextCurX = mTopTextStartX;
            }
            needValid = true;
        }
        if (isBottomRunning) {
            canvas.drawText(mBottomText, mBottomTextCurX, mBottomTextStartY, mPaint);
            mBottomTextCurX -= mStep;
            if (mBottomTextCurX < -mBottomTextLength) {
                mBottomTextCurX = mBottomTextStartX;
            }
            needValid = true;
        }
        if (needValid) {
            invalidate();
        }
    }

    public void startScroll(int location) {
        if (location == OsdInfo.OSD_POSITION_TOP) {
            this.isTopRunning = true;
        } else {
            this.isBottomRunning = true;
        }
        invalidate();
    }

    public void stopScroll(int location) {
        if (location == OsdInfo.OSD_POSITION_TOP) {
            this.isTopRunning = false;
        } else {
            this.isBottomRunning = false;
        }
        invalidate();
    }

    @Override
    public void processMessage(DvbMessage msg) {
        switch (msg.what) {
            case DvbMessage.SHOW_OSD:
                setText(msg.obj.toString(), msg.arg1);
                startScroll(msg.arg1);
                break;
            case DvbMessage.DISMISS_OSD:
                stopScroll(msg.arg1);
                break;
            default:
                break;
        }
    }
}
