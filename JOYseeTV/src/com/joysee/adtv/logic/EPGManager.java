/**
 * =====================================================================
 *
 * @file  EPGManager.java
 * @Module Name   com.joysee.adtv.logic
 * @author songwenxuan
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年3月29日
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
 * songwenxuan      2014年3月29日           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.adtv.logic;

import com.joysee.adtv.logic.bean.NETDetailEventInfo;
import com.joysee.adtv.logic.bean.NETEventInfo;
import com.joysee.adtv.logic.bean.ProgramType;
import com.joysee.adtv.logic.bean.Transponder;

import java.util.ArrayList;

class EPGManager {

    private static final String TAG = EPGManager.class.getSimpleName();

    private static EPGManager sEpgManater = new EPGManager();

    public static EPGManager getInstance() {
        return sEpgManater;
    }

    private EPGManager() {
    }

    native int nativeCancelEPGSearch();

    native int nativeGetPFEvent(int serviceId, long startTime, ArrayList<Integer> programIdList);

    native int nativeGetProgramDetail(int programId, NETDetailEventInfo detailEventInfo);

    native int nativeGetProgramIdListBySid(int serviceId, long startTime, long endTime, ArrayList<Integer> programIdList);

    native int nativeGetProgramIdListByType(int programType, long startTime, long endTime, ArrayList<Integer> programIdList);

    native int nativeGetProgramInfo(int programId, NETEventInfo eventInfo);

    native int nativeGetProgramTypes(int id, ArrayList<ProgramType> programTypeList);

    native String nativeGetTVIcons(int serviceId);

    native int nativeSetEPGSourceMode(boolean isTSMode);

    native int nativeStartEPGSearch(int tunerId, Transponder param, int type);
}
