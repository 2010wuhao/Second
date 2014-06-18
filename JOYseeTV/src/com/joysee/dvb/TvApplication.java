/**
 * =====================================================================
 *
 * @file  TvApplication.java
 * @Module Name   com.joysee.dvb
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年1月7日
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
 * YueLiang         2014年1月7日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.dvb;

import android.app.Activity;
import android.app.Application;
import android.content.Intent;
import android.content.IntentFilter;
import android.database.ContentObserver;
import android.hardware.usb.UsbManager;
import android.os.Handler;

import com.joysee.adtv.logic.JDVBPlayer;
import com.joysee.adtv.logic.JDVBPlayer.PlayerType;
import com.joysee.common.data.JHttpHelper;
import com.joysee.common.utils.JLog;
import com.joysee.dvb.data.DvbSettings.System;
import com.joysee.dvb.player.DvbPlayerFactory;
import com.joysee.dvb.portal.PortalActivity;
import com.joysee.dvb.portal.PortalModle;

import java.util.ArrayList;

public class TvApplication extends Application {
    public enum DestPlatform {
        MITV_QCOM, SKYWORTH_AMLOGIC, MStar, COMMON, MITV_2
    }

    private static final String TAG = JLog.makeTag(TvApplication.class);

    ArrayList<Activity> mActiveActivitys = new ArrayList<Activity>();
    public static boolean DEBUG_MODE = false;
    public static boolean DEBUG_LOG = false;
    public static boolean PORTAL_USE_ANIMATION = true;
    public static boolean FORCE_TS_EPG = false;
    public static boolean FORCE_TS_CHANNELTYPE = false;
    public static boolean AS_LAUNCHER = false;
    public static final int OFF_VALUE = 0;
    public static final int ON_VALUE = 1;

    public static final String ACTION_DONGLE_PERM_CHANGED = "portal.action_dongle_perm_changed";

    public static DestPlatform sDestPlatform = DestPlatform.SKYWORTH_AMLOGIC;
    public static PlayerType sDestPlayerType = PlayerType.DTV;

    static {
//        String dtvServiceLevel = SystemProperties.get("debug.adtvservice.level");
//        JLog.d(TAG, "dtvServiceLevel = " + dtvServiceLevel);
//        if (TextUtils.isEmpty(dtvServiceLevel)) {
//            sDestPlayerType = PlayerType.COMMON;
//        }
        if (sDestPlayerType == PlayerType.COMMON) {
            sDestPlatform = DestPlatform.COMMON;
        } else {
            JLog.d(TAG, "android.os.Build.MODEL = " + android.os.Build.MODEL);
            if (android.os.Build.MODEL.equals("MiTV")) {
                sDestPlatform = DestPlatform.MITV_QCOM;
            } else if (android.os.Build.MODEL.startsWith("MiTV2")) {
                sDestPlatform = DestPlatform.MITV_2;
            } else if (android.os.Build.MODEL.startsWith("MStar")) {
                sDestPlatform = DestPlatform.MStar;
            }
        }
        JDVBPlayer.setPlayerType(sDestPlayerType);
    }

    private PortalModle mModle;
    private ContentObserver mContentObserver;

    public void addActiveActivity(Activity activity) {
        mActiveActivitys.add(activity);
    }

    public ArrayList<Activity> getAllActiveActivitys() {
        return mActiveActivitys;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        final long begin = JLog.methodBegin(TAG);

        JHttpHelper.setJsonTimeout(10000);
        DvbPlayerFactory.setDestPlatform(sDestPlatform);

        mModle = new PortalModle(this);

        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_DONGLE_PERM_CHANGED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_ATTACHED);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mModle, filter);

        filter = new IntentFilter();
        filter.addAction(Intent.ACTION_PACKAGE_ADDED);
        filter.addAction(Intent.ACTION_PACKAGE_REMOVED);
        filter.addAction(Intent.ACTION_PACKAGE_CHANGED);
        filter.addDataScheme("package");
        registerReceiver(mModle, filter);

        filter = new IntentFilter();
        filter.addAction(PortalModle.ACTION_GET_RECOMMEND);
        registerReceiver(mModle, filter);

        PORTAL_USE_ANIMATION = System.getInt(getContentResolver(), System.PORTAL_USE_ANIMATION, ON_VALUE) == ON_VALUE;
        DEBUG_LOG = System.getInt(getContentResolver(), System.DEBUG_LOG, ON_VALUE) == ON_VALUE;
        DEBUG_MODE = System.getInt(getContentResolver(), System.DEBUG_MODE, ON_VALUE) == ON_VALUE;
        FORCE_TS_EPG = System.getInt(getContentResolver(), System.FORCE_TS_EPG, OFF_VALUE) == ON_VALUE;
        FORCE_TS_CHANNELTYPE = System.getInt(getContentResolver(), System.FORCE_TS_CHANNELTYPE, OFF_VALUE) == ON_VALUE;

        JLog.d(TAG, " useAnimation = " + PORTAL_USE_ANIMATION + " debug_mode = " + DEBUG_MODE + " debug_log = " + DEBUG_LOG);
        JDVBPlayer.setStrictMode(DEBUG_MODE);

        mContentObserver = new ContentObserver(new Handler()) {
            @Override
            public void onChange(boolean selfChange) {
                PORTAL_USE_ANIMATION = System.getInt(getContentResolver(), System.PORTAL_USE_ANIMATION, ON_VALUE) == ON_VALUE;
                DEBUG_MODE = System.getInt(getContentResolver(), System.DEBUG_MODE, ON_VALUE) == ON_VALUE;
                DEBUG_LOG = System.getInt(getContentResolver(), System.DEBUG_LOG, ON_VALUE) == ON_VALUE;
                FORCE_TS_EPG = System.getInt(getContentResolver(), System.FORCE_TS_EPG, OFF_VALUE) == ON_VALUE;
                FORCE_TS_CHANNELTYPE = System.getInt(getContentResolver(), System.FORCE_TS_CHANNELTYPE, OFF_VALUE) == ON_VALUE;

                if (TvApplication.DEBUG_LOG) {
                    JLog.d(TAG, " onChange selfChange = " + selfChange + " useAnimation = " + PORTAL_USE_ANIMATION
                            + " debug_mode = " + DEBUG_MODE + " debug_log = " + DEBUG_LOG + " FORCE_TS_EPG = " + FORCE_TS_EPG
                            + " FORCE_TS_CHANNELTYPE = " + FORCE_TS_CHANNELTYPE);
                }
                JDVBPlayer.setStrictMode(DEBUG_MODE);
            }
        };
        getContentResolver().registerContentObserver(System.CONTENT_URI, true, mContentObserver);

        JLog.methodEnd(TAG, begin);
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
        unregisterReceiver(mModle);
    }

    public void removeActiveActivity(Activity activity) {
        this.mActiveActivitys.remove(activity);
    }

    public PortalModle setPortal(PortalActivity portalActivity) {
        mModle.initialize(portalActivity);
        return mModle;
    }

    public static String getRepairInstructions() {
        StringBuilder sb = new StringBuilder();
        sb.append("1. 支持按区域推荐节目. \n");
        return sb.toString();
    }
}
