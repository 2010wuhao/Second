/**
 * =====================================================================
 *
 * @file  PortalScroller.java
 * @Module Name   com.joysee.dvb.portal.widget
 * @author wuhao
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
 * wuhao         2014年2月13日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.portal.widget;

import android.content.Context;
import android.view.animation.Interpolator;
import android.widget.Scroller;

public class PortalScroller extends Scroller {

    private final int mDuration = 600;// 设置移动速度

    public PortalScroller(Context context) {
        super(context);
    }

    public PortalScroller(Context context, Interpolator interpolator) {
        super(context, interpolator);
    }

    public PortalScroller(Context context, Interpolator interpolator, boolean flywheel) {
        super(context, interpolator, flywheel);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy) {
        // Ignore received duration, use fixed one instead
        super.startScroll(startX, startY, dx, dy, mDuration);
    }

    @Override
    public void startScroll(int startX, int startY, int dx, int dy, int duration) {
        // Ignore received duration, use fixed one instead
        super.startScroll(startX, startY, dx, dy, mDuration);
    }
}
