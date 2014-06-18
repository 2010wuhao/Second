/**
 * =====================================================================
 *
 * @file  DVBPlayManager.java
 * @Module Name   com.joysee.adtv.logic
 * @author songwenxuan
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2012年11月02日
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
 * songwenxuan       2012年11月02日             1.0         Check for NULL, 0 h/w
 * YueLiang          2012年11月02日             1.1         整理接口
 * =====================================================================
 **/
//

package com.joysee.adtv.logic;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.EpgEvent;
import com.joysee.adtv.logic.bean.MiniEpgNotify;

import java.util.ArrayList;

class DVBPlayManager {

    private static final String TAG = DVBPlayManager.class.getSimpleName();
    private static DVBPlayManager dvbPlayManager;

    private static Handler mHandler = new Handler(Looper.getMainLooper());

    public static final int NO_ERROR = 0;

    public static final int SERVICE_ERROR = -1;

    // accessed by native methods
    int mUSBDongleFd = -1;

    interface OnMonitorListener {
        void onMonitor(int monitorType, Object message);
    }

    static OnMonitorListener onMonitorListener;

    static DVBPlayManager getInstance() {
        if (dvbPlayManager == null) {
            dvbPlayManager = new DVBPlayManager();
        }
        return dvbPlayManager;
    }

    /**
     * DTVService 回调的函数 各种监控 包括pf、搜索的回调 都在这里进行。
     * 
     * @param monitorType
     * @param message
     */
    private synchronized static void onDTVPlayerCallBack(int tunerId, final int monitorType, final Object message) {
        Log.d(TAG, "onDTVPlayerCallback type = " + JDVBPlayer.getMonitorCallbackName(monitorType));
        if (onMonitorListener != null) {
            mHandler.post(new Runnable() {
                public void run() {
                    onMonitorListener.onMonitor(monitorType, message);
                }
            });
        }
    }

    private DVBPlayManager() {
    }

    native int nativeChangeService(int tunerId, DvbService service);

    native int nativeDisableKeepLastFrame(int tunerId, boolean bEnable);

    native int nativeGetEpgDataByDuration(int tunerId, int serviceId, ArrayList<EpgEvent> epglist, long startTime, long endTime);

    native int nativeGetPFEventInfo(int tunerId, int serviceId, MiniEpgNotify epgNotify);

    native int nativeGetPlayState(int tunerId);

    native int nativeGetTunerSignalStatus(int tunerId);

    native int nativeInit();

    native int nativePlay(int tunerId);

    native int nativeStop(int tunerId);

    native int nativeSyncServiceToProgram(int tvIndex, DvbService service);

    native int nativeUninit();

    native boolean nativeVisibleVideoLayer(boolean visible);

}
