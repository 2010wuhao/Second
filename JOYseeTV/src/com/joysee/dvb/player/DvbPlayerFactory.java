/**
 * =====================================================================
 *
 * @file  DvbPlayerFactory.java
 * @Module Name   com.joysee.dvb.player
 * @author yueliang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年1月24日
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
 * yueliang         2014年1月24日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.player;

import android.content.Context;

import com.joysee.dvb.TvApplication.DestPlatform;

public class DvbPlayerFactory {
    private static DestPlatform mPlatform;

    public static AbsDvbPlayer getPlayer(Context context) {
        AbsDvbPlayer player = null;
        if (mPlatform == DestPlatform.SKYWORTH_AMLOGIC) {
            player = new DvbPlayer_Amlogic(context);
        } else if (mPlatform == DestPlatform.MITV_QCOM) {
            player = new DvbPlayer_MiTV(context);
        } else if (mPlatform == DestPlatform.MITV_2) {
            player = new DvbPlayer_MiTV2(context);
        } else if (mPlatform == DestPlatform.MStar) {
            player = new DvbPlayer_MStar(context);
        } else if (mPlatform == DestPlatform.COMMON) {
            player = new DvbPlayer_Common(context);
        } else {
            throw new RuntimeException();
        }
        return player;
    }

    public static final void setDestPlatform(DestPlatform platform) {
        mPlatform = platform;
    }

    private DvbPlayerFactory() {
    }
}
