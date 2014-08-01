/**
 * =====================================================================
 *
 * @file  DvbSettingsDefValues.java
 * @Module Name   com.joysee.adtv.portal.provider
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2013年12月5日
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
 * YueLiang         2013年12月5日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.data;

import com.joysee.adtv.logic.JDVBPlayer;
import com.joysee.dvb.data.DvbSettings.System;

import java.util.HashMap;
import java.util.Set;

public class DvbSettingsDefValues {

    private static final HashMap<String, String> mDefaultValues = new HashMap<String, String>();

    static {
        mDefaultValues.put(System.DEFAULT_FREQUENCY, "602000");
        mDefaultValues.put(System.DEFAULT_SYMBOL_RATE, "6875");
        mDefaultValues.put(System.DEFAULT_MODULATION, "2");

        mDefaultValues.put(System.DEFAULT_CHANNEL_VOLUME, "20");
        mDefaultValues.put(System.VIDEO_ASPECTRATIO_TUNER_0, JDVBPlayer.VIDEOASPECTRATION_16TO9 + "");
        mDefaultValues.put(System.VIDEO_ASPECTRATIO_TUNER_1, JDVBPlayer.VIDEOASPECTRATION_16TO9 + "");
        mDefaultValues.put(System.VIDEO_ASPECTRATIO_TUNER_2, JDVBPlayer.VIDEOASPECTRATION_16TO9 + "");

        mDefaultValues.put(System.PORTAL_USE_ANIMATION, 1 + "");
        mDefaultValues.put(System.DEBUG_LOG, 1 + "");
        mDefaultValues.put(System.DEBUG_MODE, 0 + "");
        mDefaultValues.put(System.VOD_ENABLE, 0 + "");

    }

    public static String[] getSettingPropNames() {
        Set<String> keys = mDefaultValues.keySet();
        return keys.toArray(new String[0]);
    }

    public static String getValue(String key) {
        if (!mDefaultValues.containsKey(key)) {
            throw new RuntimeException("Can't find the corresponding value");
        }
        return mDefaultValues.get(key);
    }

    private DvbSettingsDefValues() {
    }

}
