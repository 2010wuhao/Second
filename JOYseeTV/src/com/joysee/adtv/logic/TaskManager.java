/**
 * =====================================================================
 *
 * @file  TaskManager.java
 * @Module Name   com.joysee.adtv.logic
 * @author YueLiang
 * @OS version  1.0
 * @Product type: JoySee
 * @date   2014年4月16日
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
 * YueLiang         2014年4月16日            1.0          Check for NULL, 0 h/w
 * =====================================================================
 **/

package com.joysee.adtv.logic;

class TaskManager {
    
    private static TaskManager sTaskManager = new TaskManager();

    TaskManager() {
    }

    protected static TaskManager getInstance() {
        return sTaskManager;
    }
    
    native int nativeStartTask(String url);
    native int nativeCancleTask(String url);
    native int nativeGetTaskStatus(String url);
    native int nativeGetTaskProgress(String url);
    native int nativeGetIntLocalPacketVer(String url);
    native String nativeGetDomainCode();
    native String nativeGetLocalPacketVer(String url);
    native String nativeGetServerPacketVer(String url);
}
