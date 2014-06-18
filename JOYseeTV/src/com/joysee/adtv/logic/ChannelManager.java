/**
 * =====================================================================
 *
 * @file  ChannelManager.java
 * @Module Name   com.joysee.adtv.logic
 * @author songwenxuan
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2013年11月24日
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
 * songwenxuan      2012年11月02日                    1.0         Check for NULL, 0 h/w
 * YueLiang         2013年11月23日                    1.1         封装接口
 * =====================================================================
 **/
//

package com.joysee.adtv.logic;

import com.joysee.adtv.logic.bean.DvbService;
import com.joysee.adtv.logic.bean.ProgramCatalog;
import com.joysee.adtv.logic.bean.Transponder;

import java.util.ArrayList;

class ChannelManager {

    private static ChannelManager sChannelManager = new ChannelManager();

    public static ChannelManager getInstance() {
        return sChannelManager;
    }

    private ChannelManager() {
    }

    native int nativeCancelSearchTV(boolean isSave);

    native int nativedelAllService();

    native int nativeGetAllService(ArrayList<DvbService> serviceList);

    native int nativeGetCurrentService(DvbService service);

    native int nativeGetLastDVBService(int index, int filter);

    native int nativeGetLastTVChlNum();

    native int nativeGetNextDVBService(int index, int filter);

    native int nativeGetProgramCatalogs(ArrayList<ProgramCatalog> catalogs);

    native int nativeGetService(int channelNumber, DvbService service, int type);

    native int nativeGetServiceByIndex(int channelIndex, DvbService service);

    native int nativeGetServiceCount();

    native int nativeSetCurrentService(int tunerId, DvbService service);

    native int nativeSetSearchAreaInfo(String areaInfo);

    native int nativeStartSearchTV(int searchMode, Transponder transponder);
}
