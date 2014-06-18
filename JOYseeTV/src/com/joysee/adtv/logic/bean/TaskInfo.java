/**
 * =====================================================================
 *
 * @file  TaskInfo.java
 * @Module Name   com.joysee.adtv.logic.bean
 * @author benz
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014-4-24
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
 * benz          2014-4-24           1.0         Check for NULL, 0 h/w
 * =====================================================================
 **/
//

package com.joysee.adtv.logic.bean;

import com.joysee.adtv.logic.JDVBPlayer;

public class TaskInfo {
    public String mUrl;
    public int mStatusCode;// 错误码 UT_ERROR, UT_PREPARED, UT_STARTED, UT_STOPPED
    public int mProgress;// 下载进度
    public int mCBType;

    public TaskInfo() {
    }

    public TaskInfo(String url) {
        this.mUrl = url;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        String idStr = "UNKOWN";
        if (mCBType != 0) {
            switch (mCBType) {
                case JDVBPlayer.CALLBACK_LOCALDATA_UPDATE_PROGRESS:
                    idStr = "UPDATE_PROGRESS";
                    break;
                case JDVBPlayer.CALLBACK_LOCALDATA_UPDATE_STATUS:
                    idStr = "UPDATE_STATUS";
                    break;
                case JDVBPlayer.CALLBACK_LOCALDATA_UPDATE_VERSION_FOUND:
                    idStr = "UPDATE_VERSION_FOUND";
                    break;
            }
        }
        sb.append("mCBType" + idStr + " / ");
        sb.append("mUrl=" + mUrl + " / ");
        sb.append("mErrorCode=" + mStatusCode + " / ");
        sb.append("mProgress=" + mProgress + " / ");
        return sb.toString();
    }
}
