/**
 * =====================================================================
 *
 * @file  SearchParamsReader.java
 * @Module Name   com.joysee.dvb.data
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-1-9
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
 * benz          2014-1-9           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb.data;

import android.content.Context;

import com.joysee.adtv.logic.bean.Transponder;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.data.DvbSettings.System;

public class SearchParamsReader {

    public static final class ModulationType {
        public static final int MODULATION_64QAM = 0x02;
        public static final int MODULATION_128QAM = 0x03;
        public static final int MODULATION_256QAM = 0x04;
        public static final int MODULATION_DTMB = 0x07;
    }

    public static final class SearchParameterRange {
        public static final int FREQUENCY_MIN = 47;
        public static final int FREQUENCY_MAX = 862;
        public static final int SYMBOLRATE_MIN = 1500;
        public static final int SYMBOLRATE_MAX = 7200;
    }

    private static final String TAG = JLog.makeTag(SearchParamsReader.class);
    public static final int FAST_SEARCH = 0;

    public static final int FULL_SEARCH = 1;

    public static final int MANUAL_SEARCH = 2;

    public static Transponder getDefaultTransponder(Context ctx) {
        Transponder tp = new Transponder();
        tp.setFrequency(System.getInt(ctx.getContentResolver(), System.DEFAULT_FREQUENCY, 602000));
        tp.setSymbolRate(System.getInt(ctx.getContentResolver(), System.DEFAULT_SYMBOL_RATE, 6875));
        tp.setModulation(System.getInt(ctx.getContentResolver(), System.DEFAULT_MODULATION, 2));
        return tp;
    }

    public static Transponder getTransponderBySearchMode(Context ctx, int mode) {
        String tempParams = null;
        String attr = "";
        switch (mode) {
            case FAST_SEARCH:
                tempParams = System.getString(ctx.getContentResolver(), System.FAST_SEARCH_PARAMS);
                attr = "FAST_SEARCH";
                break;
            case FULL_SEARCH:
                tempParams = System.getString(ctx.getContentResolver(), System.FULL_SEARCH_PARAMS);
                attr = "FULL_SEARCH";
                break;
            case MANUAL_SEARCH:
                attr = "MANUAL_SEARCH";
                tempParams = System.getString(ctx.getContentResolver(), System.MANUAL_SEARCH_PARAMS);
                break;
        }

        if (tempParams == null || "".equals(tempParams)) {
            JLog.d(TAG, "getTransponderBySearchMode  mode = " + attr + "  is null");
            return getDefaultTransponder(ctx);
        }

        String[] params = tempParams.split(":");

        Transponder tp = new Transponder();
        tp.setFrequency(Integer.valueOf(params[0]));
        tp.setSymbolRate(Integer.valueOf(params[1]));
        tp.setModulation(Integer.valueOf(params[2]));
        JLog.d(TAG,
                "getTransponderBySearchMode " + attr + " ---> " + tp.getFrequency() + "--" + tp.getSymbolRate() + "--" + tp.getModulation());
        return tp;
    }

    /**
     * save rule ---> frequency:sybolrate:modulation
     * 
     * @param ctx
     * @param mode
     */
    public static void updateTransponder(Context ctx, int mode, Transponder tp) {

        StringBuffer sb = new StringBuffer();
        sb.append(tp.getFrequency() + ":" + tp.getSymbolRate() + ":" + tp.getModulation());

        switch (mode) {
            case FAST_SEARCH:
                System.putString(ctx.getContentResolver(), System.FAST_SEARCH_PARAMS, sb.toString());
                break;
            case FULL_SEARCH:
                System.putString(ctx.getContentResolver(), System.FULL_SEARCH_PARAMS, sb.toString());
                break;
            case MANUAL_SEARCH:
                System.putString(ctx.getContentResolver(), System.MANUAL_SEARCH_PARAMS, sb.toString());
                break;
        }
        JLog.d(TAG, "updateTransponder " + mode + " ---> " + sb.toString());
    }
}
