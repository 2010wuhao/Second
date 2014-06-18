/**
 * =====================================================================
 *
 * @file  DVBSurfaceMask.java
 * @Module Name   com.joysee.dvb.widget
 * @author yl
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年3月12日
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
 * yl         2014年3月12日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.widget;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import com.joysee.dvb.controller.DvbMessage;
import com.joysee.dvb.controller.IDvbBaseView;

public class DVBSurfaceMask extends View implements IDvbBaseView {

    public DVBSurfaceMask(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    @Override
    public void processMessage(DvbMessage msg) {
        switch (msg.what) {
            case DvbMessage.START_PLAY_TV:
                this.setVisibility(View.INVISIBLE);
                break;
            case DvbMessage.START_PLAY_BC:
                this.setVisibility(View.VISIBLE);
                break;
            default:
                break;
        }
    }

}
