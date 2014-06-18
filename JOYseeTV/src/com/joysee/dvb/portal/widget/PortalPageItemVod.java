/**
 * =====================================================================
 *
 * @file  PortalPageItemTv.java
 * @Module Name   com.joysee.dvb.portal.widget
 * @author wuhao
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年2月18日
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
 * wuhao         2014年2月18日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.portal.widget;

import android.content.Context;
import android.util.AttributeSet;

public class PortalPageItemVod extends PortalPageViewItem {
    public PortalPageItemVod(Context context) {
        super(context);
    }

    public PortalPageItemVod(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public PortalPageItemVod(Context context, AttributeSet attrs, int defStyle) {
        super(context, attrs, defStyle);
    }

    void init() {
        mID = PortalViewController.PAGE_ID_VOD;
    }

}
