/**
 * =====================================================================
 *
 * @file  JDVBStopTimeoutException.java
 * @Module Name   com.joysee.adtv.logic
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年5月21日
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
 * YueLiang         2014年5月21日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.adtv.logic;

public class JDVBStopTimeoutException extends RuntimeException {

    private static final long serialVersionUID = 1L;
    private long mElapsedTime = 0L;

    public JDVBStopTimeoutException(long tElapsedTime) {
        mElapsedTime = tElapsedTime;
    }

    public long getElapsedTime() {
        return mElapsedTime;
    }

}
