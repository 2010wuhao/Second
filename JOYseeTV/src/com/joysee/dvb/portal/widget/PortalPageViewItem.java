/**
 * =====================================================================
 *
 * @file  PortalPagerViewItem.java
 * @Module Name   com.example.viewpagersample.view
 * @author wuhao
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月8日
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
 * wuhao         2014年2月8日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.portal.widget;

import android.content.Context;
import android.graphics.Canvas;
import android.util.AttributeSet;
import android.view.KeyEvent;
import android.widget.FrameLayout;

import com.joysee.common.utils.JLog;

public abstract class PortalPageViewItem extends FrameLayout {
    public String TAG = JLog.makeTag(PortalPageViewItem.class);
    private int mNextFocusIndex = 0;
    private int mLastFocusIndex = 0;
    protected int mID = -1;

    abstract void init();

    public PortalPageViewItem(Context context) {
        this(context, null);
    }

    public PortalPageViewItem(Context context, AttributeSet attrs) {
        this(context, attrs, 0);
    }

    public PortalPageViewItem(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
        setChildrenDrawingOrderEnabled(true);
        init();
    }

    @Override
    protected void dispatchDraw(Canvas canvas) {
        mNextFocusIndex = getFocusedIndex();
        try {
            super.dispatchDraw(canvas);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Override
    public boolean dispatchKeyEvent(KeyEvent event) {
        int keyCode = event.getKeyCode();
        int action = event.getAction();
        boolean result = false;
        switch (action) {
            case KeyEvent.ACTION_DOWN:
                switch (keyCode) {
                    case KeyEvent.KEYCODE_DPAD_LEFT:
                    case KeyEvent.KEYCODE_DPAD_RIGHT:
                    case KeyEvent.KEYCODE_DPAD_DOWN:
                    case KeyEvent.KEYCODE_DPAD_UP:
                        mLastFocusIndex = getFocusedIndex();
                        break;
                }
                break;
        }
        return result ? true : super.dispatchKeyEvent(event);
    }

    @Override
    protected int getChildDrawingOrder(int childCount, int i) {
        int bigIndex = Math.max(mLastFocusIndex, mNextFocusIndex);
        int smallIndex = Math.min(mLastFocusIndex, mNextFocusIndex);
        if (smallIndex == bigIndex) {
            if (i >= smallIndex) {
                i = i + 1;
            }
            if (i == childCount) {
                return smallIndex;
            }
        } else {
            if (i >= smallIndex) {
                i = i + 1;
            }
            if (i >= bigIndex) {
                i = i + 1;
            }
            if (i == childCount) {
                return mLastFocusIndex;
            }
            if (i == childCount + 1) {
                return mNextFocusIndex;
            }
        }
        return i;
    }

    private int getFocusedIndex() {
        int childCount = getChildCount();
        for (int i = 0; i < childCount; i++) {
            if (getChildAt(i).hasFocus()) {
                return i;
            }
        }
        return 0;
    }
}
